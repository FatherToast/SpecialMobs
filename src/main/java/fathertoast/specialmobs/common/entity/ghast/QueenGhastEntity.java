package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.QueenGhastSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class QueenGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<QueenGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xCE0Aff )
                .uniqueTextureWithAnimation()
                .size( 1.25F, 5.0F, 5.0F )
                .addExperience( 2 ).regen( 20 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.6 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new QueenGhastSpeciesConfig( species, 3, 6, 4, 10 );
    }
    
    /** @return This entity's species config. */
    public QueenGhastSpeciesConfig getConfig() { return (QueenGhastSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Queen Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", Items.GOLD_INGOT );
        loot.addUncommonDrop( "uncommon", Items.GHAST_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<QueenGhastEntity> getVariantFactory() { return QueenGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends QueenGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of babies spawned on death. */
    private int babies;
    /** The number of extra babies that can be spawned by attacks. */
    private int summons;
    
    public QueenGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) {
        super( entityType, world );
        babies = getConfig().QUEEN.babies.next( random );
        summons = getConfig().QUEEN.summons.next( random );
    }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( !level.isClientSide() && summons > 0 && random.nextInt( 2 ) == 0 ) {
            summons--;
            
            final double vX = target.getX() - getX();
            final double vZ = target.getZ() - getZ();
            final double vH = Math.sqrt( vX * vX + vZ * vZ );
            spawnBaby( vX / vH + getDeltaMovement().x * 0.2, vZ / vH + getDeltaMovement().z * 0.2, null );
            spawnAnim();
            References.LevelEvent.GHAST_SHOOT.play( this );
        }
        else {
            super.performRangedAttack( target, damageMulti );
        }
    }
    
    /** Override to change this ghast's explosion power multiplier. */
    @Override
    protected int getVariantExplosionPower( int radius ) { return Math.round( radius * 1.5F ); }
    
    /** Called to remove this entity from the world. Includes death, unloading, interdimensional travel, etc. */
    @Override
    public void remove( boolean keepData ) {
        //noinspection deprecation
        if( isDeadOrDying() && !removed && level instanceof IServerWorld ) { // Same conditions as slime splitting
            // Spawn babies on death
            ILivingEntityData groupData = null;
            for( int i = 0; i < babies; i++ ) {
                groupData = spawnBaby( (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, groupData );
            }
            spawnAnim();
            References.LevelEvent.BLAZE_SHOOT.play( this );
        }
        super.remove( keepData );
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private ILivingEntityData spawnBaby( double vX, double vZ, @Nullable ILivingEntityData groupData ) {
        final BabyGhastEntity baby = BabyGhastEntity.SPECIES.entityType.get().create( level );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = yRot;
        baby.yBodyRot = yRot;
        groupData = baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                SpawnReason.MOB_SUMMONED, groupData, null );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement( vX, 0.0, vZ );
        baby.setOnGround( false );
        
        level.addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_BABIES, (byte) babies );
        saveTag.putByte( References.TAG_SUMMONS, (byte) summons );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_BABIES, References.NBT_TYPE_NUMERICAL ) )
            babies = saveTag.getByte( References.TAG_BABIES );
        if( saveTag.contains( References.TAG_SUMMONS, References.NBT_TYPE_NUMERICAL ) )
            summons = saveTag.getByte( References.TAG_SUMMONS );
    }
}