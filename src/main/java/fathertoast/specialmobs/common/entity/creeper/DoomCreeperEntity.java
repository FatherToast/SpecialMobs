package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class DoomCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DoomCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x494949 ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithOverlay()
                .addExperience( 1 ).effectImmune( Effects.HARM, Effects.WITHER );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Doom Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.WITCH.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DoomCreeperEntity> getVariantFactory() { return DoomCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DoomCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public DoomCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius / 2.0F ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, false, false );
    }
    
    /**
     * Override to change effects applied by the lingering cloud left by this creeper's explosion.
     * If this list is empty, the lingering cloud is not created.
     */
    @Override
    protected void modifyVariantLingeringCloudEffects( List<EffectInstance> potions ) {
        potions.add( new EffectInstance( Effects.HARM, 100, 1 ) );
        potions.add( new EffectInstance( Effects.WITHER, 200 ) );
    }
    
    /** Override to change stats of the lingering cloud left by this creeper's explosion. */
    @Override
    protected void modifyVariantLingeringCloud( AreaEffectCloudEntity potionCloud ) {
        potionCloud.setRadius( (float) explosionRadius * (isPowered() ? 2.0F : 1.0F) );
    }
}