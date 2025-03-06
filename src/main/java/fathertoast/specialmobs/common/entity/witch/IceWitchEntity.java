package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.Collections;
import java.util.List;

@SpecialMob
public class IceWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<IceWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDEAEA ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).effectImmune( MobEffects.MOVEMENT_SLOWDOWN )
                .addToAttribute( Attributes.ARMOR, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Ice Witch",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Items.SNOWBALL );
        loot.addUncommonDrop( "uncommon", Blocks.BLUE_ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<IceWitchEntity> getVariantFactory() { return IceWitchEntity::new; }

    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return List.of( SMTags.EntityTypes.WITCHES, EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES );
    }

    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends IceWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Ticks before this witch can use its ice wall ability. */
    private int wallDelay;
    
    public IceWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 0.8 );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigation createNavigation( Level level ) {
        return new FluidPathNavigator( this, level, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( FluidState fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.08, ForgeMod.WATER_TYPE.get() );
        MobHelper.hopOnFluid( this );
    }
    
    /** Called whenever this entity's block position changes. */
    @Override
    protected void onChangedBlock( BlockPos pos ) {
        super.onChangedBlock( pos );
        MobHelper.updateFrostWalker( this, pos );
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( !target.hasEffect( MobEffects.MOVEMENT_SLOWDOWN ) ) {
            return makeSplashPotion( Potions.STRONG_SLOWNESS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level().isClientSide() && isAlive() && wallDelay-- <= 0 && target != null && random.nextInt( 20 ) == 0 ) {
            
            // Create an ice wall behind the target if they are vulnerable
            final double distanceSq = target.distanceToSqr( this );
            if( distanceSq > 100.0 && distanceSq < 196.0 && target.hasEffect( MobEffects.MOVEMENT_SLOWDOWN ) && hasLineOfSight( target ) ) {
                wallDelay = 200;
                
                Vec3 targetVec = target.position().subtract( position() );
                if( target.isUnderWater() ) {
                    buildIceSheet( target.blockPosition().mutable().move( 0, (int) Math.ceil( target.getBbHeight() ) + 1, 0 ), 3 );
                }
                else {
                    buildIceWall( target.blockPosition().mutable(),
                            Direction.getNearest( targetVec.x, 0.0, targetVec.z ) );
                }
            }
        }
        super.aiStep();
    }
    
    /** Try to place an ice sheet at the location. */
    private void buildIceSheet( BlockPos.MutableBlockPos currentPos, int maxDiff ) {
        final int yI = currentPos.getY();
        final int maxY = Math.min( yI + maxDiff, level().getMaxBuildHeight() - 2 );
        
        // Try to target the surface
        while( currentPos.getY() < maxY && level().isWaterAt( currentPos ) )
            currentPos.move( 0, 1, 0 );
        currentPos.move( 0, -1, 0 );
        
        final BlockPos center = currentPos.immutable();
        
        final int radius = 5;
        final int radiusSq = radius * radius;
        
        for( int x = -radius; x <= radius; x++ ) {
            for( int z = -radius; z <= radius; z++ ) {
                final int distSq = x * x + z * z;
                
                if( distSq <= radiusSq ) {
                    currentPos.setWithOffset( center, x, 0, z );
                    
                    if( shouldReplace( currentPos ) &&
                            MobHelper.placeBlock( this, currentPos, MeltingIceBlock.getState( level(), currentPos ) ) ) {
                        MeltingIceBlock.scheduleFirstTick( level(), currentPos, random );
                    }
                }
            }
        }
        
        playSpellSound( center );
    }
    
    /** Try to place a line of ice pillars at the location. */
    private void buildIceWall( BlockPos.MutableBlockPos currentPos, Direction forward ) {
        Direction transverse = forward.getClockWise();
        
        currentPos.move( forward, 2 );
        final BlockPos center = currentPos.immutable();
        
        for( int tv = -4; tv < 4; tv++ ) {
            currentPos.set( center ).move( transverse, tv );
            placePillar( currentPos, 4 );
        }
        
        playSpellSound( center );
    }
    
    /** Try to place an ice pillar at the location. */
    private void placePillar( BlockPos pos, int maxDiff ) {
        final BlockPos.MutableBlockPos currentPos = pos.mutable();
        if( shouldReplace( currentPos ) ) findGroundBelow( currentPos, maxDiff );
        else if( findGroundAbove( currentPos, maxDiff ) ) return;
        
        final int maxY = Math.min( currentPos.getY() + 4, level().getMaxBuildHeight() - 2 );
        int height = -2; // This is minimum pillar height
        if( pos.getY() > currentPos.getY() ) height -= (pos.getY() - currentPos.getY()) / 2;
        
        while( currentPos.getY() < maxY && shouldReplace( currentPos ) ) {
            if( MobHelper.placeBlock( this, currentPos, MeltingIceBlock.getState( level(), currentPos ) ) ) {
                MeltingIceBlock.scheduleFirstTick( level(), currentPos, random );
            }
            currentPos.move( 0, 1, 0 );
            
            if( ++height >= 0 && random.nextBoolean() ) break;
        }
    }
    
    /** Attempts to find the ground. Resets the position if none can be found. */
    private void findGroundBelow( BlockPos.MutableBlockPos currentPos, int maxDiff ) {
        final int yI = currentPos.getY();
        final int minY = Math.max( yI - maxDiff, 0 );
        
        while( currentPos.getY() > minY ) {
            currentPos.move( 0, -1, 0 );
            if( !shouldReplace( currentPos ) ) {
                // Move back up one to ensure the current pos is replaceable
                currentPos.move( 0, 1, 0 );
                return;
            }
        }
        // Initial y was replaceable, so we can default to this
        currentPos.setY( yI );
    }
    
    /** @return Attempts to find the ground. Returns true if the pillar should be canceled. */
    private boolean findGroundAbove( BlockPos.MutableBlockPos currentPos, int maxDiff ) {
        final int yI = currentPos.getY();
        final int maxY = Math.min( yI + maxDiff, level().getMaxBuildHeight() - 2 );
        
        while( currentPos.getY() < maxY ) {
            currentPos.move( 0, 1, 0 );
            // Found a replaceable pos
            if( shouldReplace( currentPos ) ) return false;
        }
        // Initial y was not replaceable, so we must cancel the entire operation
        return true;
    }
    
    /** @return True if a generating pillar should replace the block at a particular position. */
    private boolean shouldReplace( BlockPos pos ) {
        final BlockState stateAtPos = level().getBlockState( pos );
        return stateAtPos.canBeReplaced() || stateAtPos.is( BlockTags.LEAVES ) || stateAtPos.is( Blocks.FROSTED_ICE );
    }
    
    /** Plays the sound for this witch's ice wall spell. */
    private void playSpellSound( BlockPos pos ) {
        if( !isSilent() ) {
            level().playSound( null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.GLASS_BREAK, getSoundSource(), 0.4F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
        }
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
}