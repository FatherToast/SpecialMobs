package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.MotherSpiderSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class MotherSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MotherSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xB300B3 )
                .uniqueTextureWithEyes()
                .size( 1.2F, 1.7F, 1.0F )
                .addExperience( 1 ).regen( 30 )
                .addToAttribute( Attributes.MAX_HEALTH, 16.0 ).addToAttribute( Attributes.ARMOR, 6.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 3.0 ).addToRangedDamage( 1.5 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new MotherSpiderSpeciesConfig( species, DEFAULT_SPIT_CHANCE,
                2, 4, 3, 6 );
    }
    
    /** @return This entity's species config. */
    @Override
    public MotherSpiderSpeciesConfig getConfig() { return (MotherSpiderSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mother Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.SPIDER_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<MotherSpiderEntity> getVariantFactory() { return MotherSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends MotherSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of babies spawned on death. */
    private int babies;
    /** The number of extra babies that can be spawned from hits. */
    private int extraBabies;
    
    public MotherSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) {
        super( entityType, world );
        babies = getConfig().MOTHER.babies.next( random );
        extraBabies = getConfig().MOTHER.extraBabies.next( random );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            // Spawn babies when damaged
            if( extraBabies > 0 && amount > 1.0F && level instanceof IServerWorld && random.nextFloat() < 0.33F ) {
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
    public void remove( boolean keepData ) {
        //noinspection deprecation
        if( isDeadOrDying() && !removed && level instanceof IServerWorld ) { // Same conditions as slime splitting
            // Spawn babies on death
            final int babiesToSpawn = babies + extraBabies;
            ILivingEntityData groupData = null;
            for( int i = 0; i < babiesToSpawn; i++ ) {
                groupData = spawnBaby( 0.33F, groupData );
            }
            spawnAnim();
            playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
        }
        super.remove( keepData );
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private ILivingEntityData spawnBaby( float speed, @Nullable ILivingEntityData groupData ) {
        final BabySpiderEntity baby = BabySpiderEntity.SPECIES.entityType.get().create( level );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = yRot;
        baby.yBodyRot = yRot;
        groupData = baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                SpawnReason.MOB_SUMMONED, groupData, null );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement(
                (random.nextDouble() - 0.5) * speed,
                0.2 + 0.5 * random.nextDouble(), // Used to cause floor clip bug; remove if it happens again
                (random.nextDouble() - 0.5) * speed );
        baby.setOnGround( false );
        
        level.addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_BABIES, (byte) babies );
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) extraBabies );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_BABIES, References.NBT_TYPE_NUMERICAL ) )
            babies = saveTag.getByte( References.TAG_BABIES );
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            extraBabies = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
}