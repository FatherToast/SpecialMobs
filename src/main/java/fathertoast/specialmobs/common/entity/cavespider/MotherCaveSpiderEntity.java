package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.MotherSpiderSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

@SpecialMob
public class MotherCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MotherCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xB300B3 )
                .uniqueTextureWithEyes()
                .size( 0.8F, 0.9F, 0.6F )
                .addExperience( 1 ).regen( 30 )
                .addToAttribute( Attributes.MAX_HEALTH, 16.0 ).addToAttribute( Attributes.ARMOR, 6.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 3.0 ).addToRangedDamage( 1.5 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new MotherSpiderSpeciesConfig( manager, species, DEFAULT_SPIT_CHANCE,
                2, 4, 2, 4 );
    }
    
    /** @return This entity's species config. */
    @Override
    public MotherSpiderSpeciesConfig getConfig() { return (MotherSpiderSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mother Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.CAVE_SPIDER_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<MotherCaveSpiderEntity> getVariantFactory() { return MotherCaveSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends MotherCaveSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of babies spawned on death. */
    private int babies;
    /** The number of extra babies that can be spawned from hits. */
    private int extraBabies;
    
    public MotherCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, Level level ) {
        super( entityType, level );
        babies = getConfig().MOTHER.babies.next( random );
        extraBabies = getConfig().MOTHER.extraBabies.next( random );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            // Spawn babies when damaged
            if( extraBabies > 0 && amount > 1.0F && level() instanceof ServerLevelAccessor && random.nextFloat() < 0.33F ) {
                extraBabies--;
                spawnBaby( 0.66F, null );
                spawnAnim();
                playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
            }
            return true;
        }
        return false;
    }
    
    /** Called to remove this entity from the world. Includes death, unloading, interdimensional travel, etc. */
    @Override
    public void remove( RemovalReason removalReason ) {
        if( isDeadOrDying() && !isRemoved() && level() instanceof ServerLevelAccessor ) { // Same conditions as slime splitting
            // Spawn babies on death
            final int babiesToSpawn = babies + extraBabies;
            SpawnGroupData groupData = null;
            for( int i = 0; i < babiesToSpawn; i++ ) {
                groupData = spawnBaby( 0.33F, groupData );
            }
            spawnAnim();
            playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
        }
        super.remove( removalReason );
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private SpawnGroupData spawnBaby( float speed, @Nullable SpawnGroupData groupData ) {
        final BabyCaveSpiderEntity baby = BabyCaveSpiderEntity.SPECIES.entityType.get().create( level() );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = getYRot();
        baby.yBodyRot = getYRot();
        groupData = ForgeEventFactory.onFinalizeSpawn( baby, (ServerLevelAccessor) level(), level().getCurrentDifficultyAt( blockPosition() ),
                MobSpawnType.MOB_SUMMONED, groupData, null );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement(
                (random.nextDouble() - 0.5) * speed,
                0.2 + 0.5 * random.nextDouble(), // Used to cause floor clip bug; remove if it happens again
                (random.nextDouble() - 0.5) * speed );
        baby.setOnGround( false );
        
        level().addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundTag saveTag ) {
        saveTag.putByte( References.TAG_BABIES, (byte) babies );
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) extraBabies );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( saveTag.contains( References.TAG_BABIES, References.NBT_TYPE_NUMERICAL ) )
            babies = saveTag.getByte( References.TAG_BABIES );
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            extraBabies = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
}