package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class FrozenDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FrozenDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDEAEA ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureWithOverlay()
                .addExperience( 2 ).effectImmune( Effects.MOVEMENT_SLOWDOWN )
                .addToAttribute( Attributes.ARMOR, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Frozen Drowned",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Blocks.ICE );
        loot.addRareDrop( "rare", Blocks.BLUE_ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<FrozenDrownedEntity> getVariantFactory() { return FrozenDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FrozenDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final int ICE_SEAL_TICKS = 4;
    
    private int iceSealTimer;
    private BlockPos iceSealPos;
    
    public FrozenDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.MOVEMENT_SLOWDOWN, 2 );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() ) {
            if( iceSealPos != null ) {
                // Currently creating ice seal
                if( iceSealTimer++ % ICE_SEAL_TICKS == 0 ) {
                    final int radius = iceSealTimer / ICE_SEAL_TICKS;
                    makeIceSeal( iceSealPos, radius );
                    
                    if( radius >= 7 ) {
                        iceSealTimer = 100 + random.nextInt( 100 );
                        iceSealPos = null;
                    }
                }
            }
            else if( iceSealTimer-- <= 0 ) {
                // Check if a new ice seal should be created
                final LivingEntity target = getTarget();
                if( target != null && target.isUnderWater() && distanceToSqr( target ) < 144.0 && random.nextInt( 20 ) == 0 ) {
                    final BlockPos pos = findIceSealPos( target.blockPosition(), MathHelper.ceil( target.getBbHeight() ) );
                    if( pos != null && ExplosionHelper.getMode( this ) != Explosion.Mode.NONE ) {
                        iceSealTimer = 0;
                        iceSealPos = pos;
                    }
                }
            }
        }
        super.aiStep();
    }
    
    /** @return The position to create an ice seal at, or null if the target is invalid. */
    @Nullable
    private BlockPos findIceSealPos( BlockPos targetPos, int targetHeight ) {
        // Find the water surface
        final int maxRange = 6 + targetHeight;
        final BlockPos.Mutable pos = targetPos.mutable();
        for( int y = 0; y <= maxRange; y++ ) {
            pos.setY( targetPos.getY() + y );
            if( pos.getY() >= level.getMaxBuildHeight() ) break; // Can't build here
            
            final BlockState block = level.getBlockState( pos );
            if( block.getBlock() != Blocks.WATER || block.getValue( FlowingFluidBlock.LEVEL ) != 0 ) {
                if( y - 1 <= targetHeight ) break; // Don't build inside the target entity
                return pos.below();
            }
        }
        return null;
    }
    
    /** Creates an ice seal centered at the position with a certain size. */
    private void makeIceSeal( BlockPos center, int radius ) {
        if( !isSilent() ) {
            level.playSound( null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    SoundEvents.GLASS_BREAK, getSoundSource(), 0.4F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
        }
        
        if( radius <= 0 ) {
            placeSealBlock( center );
            return;
        }
        
        for( int x = -radius; x <= radius; x++ ) {
            for( int z = -radius; z <= radius; z++ ) {
                final int distSq = x * x + z * z;
                
                // Fill circle
                if( distSq <= radius * radius ) {
                    placeSealBlock( center.offset( x, 0, z ) );
                }
            }
        }
    }
    
    /** Attempts to place a single seal block. */
    private void placeSealBlock( BlockPos pos ) {
        final BlockState block = MeltingIceBlock.getState( level, pos );
        if( level.getBlockState( pos ).getMaterial().isReplaceable() && level.isUnobstructed( block, pos, ISelectionContext.empty() ) &&
                MobHelper.placeBlock( this, pos, block ) ) {
            MeltingIceBlock.scheduleFirstTick( level, pos, random );
        }
    }
}