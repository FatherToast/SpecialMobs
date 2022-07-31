package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.WebSpiderSpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public static EntityType.IFactory<WebSpiderEntity> getVariantFactory() { return WebSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WebSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of cobwebs this spider can place. */
    private int webCount;
    
    public WebSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) {
        super( entityType, world );
        webCount = getConfig().WEB.webCount.next( random );
    }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0xFFFFFF; }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( !level.isClientSide() && webCount > 0 && !(target instanceof SpiderEntity) ) {
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
        if( level.getBlockState( pos ).getMaterial().isReplaceable() ) {
            level.setBlock( pos, Blocks.COBWEB.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
            webCount--;
            return true;
        }
        return false;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_AMMO, (byte) webCount );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_AMMO, References.NBT_TYPE_NUMERICAL ) )
            webCount = saveTag.getByte( References.TAG_AMMO );
    }
}