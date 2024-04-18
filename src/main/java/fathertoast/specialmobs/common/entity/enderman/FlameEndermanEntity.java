package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

@SpecialMob
public class FlameEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FlameEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDC1A00 ).theme( BestiaryInfo.Theme.FIRE )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).fireImmune()
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Flame Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FIRE_CHARGE );
        loot.addUncommonDrop( "uncommon", Items.COAL );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<FlameEndermanEntity> getVariantFactory() { return FlameEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FlameEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private int flameRingCooldown;
    
    public FlameEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        flameRingCooldown--;
        super.aiStep();
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        target.setSecondsOnFire( 4 );
        for( int i = 0; i < 64; i++ ) {
            if( teleport() ) break;
        }
    }
    
    /** @return Teleports this enderman towards the target; returns true if successful. */
    @Override
    protected boolean teleportTowards( Entity target ) {
        if( super.teleportTowards( target ) ) {
            if( flameRingCooldown <= 0 ) {
                flameRingCooldown = 100 + random.nextInt( 100 );
                makeFireRing( target.blockPosition() );
            }
            return true;
        }
        return false;
    }
    
    /** Creates a ring of fire around the target position. */
    private void makeFireRing( BlockPos center ) {
        if( ExplosionHelper.getMode( this ) == Explosion.BlockInteraction.KEEP ) return;
        
        final int radius = 5;
        final int rMinusOneSq = (radius - 1) * (radius - 1);
        
        for( int x = -radius; x <= radius; x++ ) {
            for( int z = -radius; z <= radius; z++ ) {
                final int distSq = x * x + z * z;
                
                // Attempt to place fire along circumference only
                if( distSq <= radius * radius && distSq > rMinusOneSq ) {
                    placeFireWall( center.offset( x, 0, z ), radius );
                }
            }
        }
    }
    
    /** Try to place a fire wall part at the location. */
    private void placeFireWall( BlockPos pos, @SuppressWarnings( "SameParameterValue" ) int radius ) {
        final BlockPos.MutableBlockPos currentPos = pos.mutable();
        currentPos.setY( Math.max( pos.getY() - radius, 0 ) );
        final int maxY = Math.min( pos.getY() + radius, level().getMaxBuildHeight() - 2 );
        
        while( currentPos.getY() < maxY ) {
            currentPos.move( 0, 1, 0 );
            
            if( shouldSetFire( currentPos ) ) {
                MobHelper.placeBlock( this, currentPos, BaseFireBlock.getState( level(), currentPos ) );
            }
        }
    }
    
    /** @return True if a fire block can be placed at the position. */
    private boolean shouldSetFire( BlockPos pos ) {
        if( !level().getBlockState( pos ).canBeReplaced() ) return false;
        if( ((FireBlock) Blocks.FIRE).canCatchFire( level(), pos, Direction.UP ) ) return true;
        
        final BlockPos posBelow = pos.below();
        return level().getBlockState( posBelow ).isFaceSturdy( level(), posBelow, Direction.UP );
    }
}