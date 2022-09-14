package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.SplittingCreeperSpeciesConfig;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class SplittingCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SplittingCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x5F9D22 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithEyes()
                .size( 1.2F, 0.7F, 1.99F )
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SplittingCreeperSpeciesConfig( species, false, false, true,
                1, 3 );
    }
    
    /** @return This entity's species config. */
    public SplittingCreeperSpeciesConfig getConfig() { return (SplittingCreeperSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Splitting Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.CREEPER_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<SplittingCreeperEntity> getVariantFactory() { return SplittingCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SplittingCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of extra mini creepers spawned on explosion (in addition to the amount based on explosion power). */
    private int extraBabies;
    
    public SplittingCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        extraBabies = getConfig().SPLITTING.extraBabies.next( random );
    }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, false, false );
        
        if( !(level instanceof IServerWorld) ) return;
        
        final int babiesToSpawn = extraBabies + (int) (explosionPower * explosionPower) / 2;
        ILivingEntityData groupData = null;
        for( int i = 0; i < babiesToSpawn; i++ ) {
            groupData = spawnBaby( explosionPower / 3.0F, groupData );
        }
        spawnAnim();
        playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private ILivingEntityData spawnBaby( float speed, @Nullable ILivingEntityData groupData ) {
        final MiniCreeperEntity baby = MiniCreeperEntity.SPECIES.entityType.get().create( level );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = yRot;
        baby.yBodyRot = yRot;
        groupData = baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                SpawnReason.MOB_SUMMONED, groupData, null );
        baby.copyChargedState( this );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement(
                (random.nextDouble() - 0.5) * speed,
                0.3 + 0.3 * random.nextDouble(), // Used to cause floor clip bug; remove if it happens again
                (random.nextDouble() - 0.5) * speed );
        baby.setOnGround( false );
        
        level.addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) extraBabies );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            extraBabies = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
}