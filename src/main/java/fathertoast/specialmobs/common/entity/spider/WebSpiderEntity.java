package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.WebSpiderSpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@SpecialMob
public class WebSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WebSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE7E7E7 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithEyes()
                .addExperience( 2 )
                .spitAttackMultiplied( 0.1, 1.0, 2.0F, 1.0 )
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new WebSpiderSpeciesConfig( species, 0.02, 2, 6 );
    }
    
    /** @return This entity's species config. */
    @Override
    public WebSpiderSpeciesConfig getConfig() { return (WebSpiderSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Weaver",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Blocks.COBWEB );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<WebSpiderEntity> getVariantFactory() { return WebSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WebSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of cobwebs this spider can place. */
    private int webCount;
    
    public WebSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, Level level ) {
        super( entityType, level );
        webCount = getConfig().WEB.webCount.next( random );
    }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0xFFFFFF; }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( !level.isClientSide() && webCount > 0 && !(target instanceof Spider) ) {
            final BlockPos pos = target.blockPosition();
            if( !tryPlaceWeb( pos ) && target.getBbHeight() > 1.0F ) {
                tryPlaceWeb( pos.above() );
            }
        }
    }
    
    /** Called when this entity dies to add drops regardless of loot table. */
    @Override
    protected void dropCustomDeathLoot( DamageSource source, int looting, boolean killedByPlayer ) {
        super.dropCustomDeathLoot( source, looting, killedByPlayer );
        tryPlaceWeb( blockPosition() );
    }
    
    /** @return Attempts to place a cobweb at the given position and returns true if successful. */
    private boolean tryPlaceWeb( BlockPos pos ) {
        if( level.getBlockState( pos ).getMaterial().isReplaceable() &&
                MobHelper.placeBlock( this, pos, Blocks.COBWEB.defaultBlockState() ) ) {
            webCount--;
            return true;
        }
        return false;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundTag saveTag ) {
        saveTag.putByte( References.TAG_AMMO, (byte) webCount );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( saveTag.contains( References.TAG_AMMO, References.NBT_TYPE_NUMERICAL ) )
            webCount = saveTag.getByte( References.TAG_AMMO );
    }
}