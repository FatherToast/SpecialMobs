package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SpecialMob
public class FireSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FireSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDFA21B ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FIRE )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).fireImmune().waterSensitive();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Firefang",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FIRE_CHARGE );
        loot.addUncommonDrop( "uncommon", Items.COAL );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<FireSpiderEntity> getVariantFactory() { return FireSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FireSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public FireSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0xDFA21B; } // Fire orange
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( !level.isClientSide() ) {
            final BlockPos pos = target.blockPosition();
            if( level.getBlockState( pos ).getMaterial().isReplaceable() ) {
                MobHelper.placeBlock( this, pos, Blocks.FIRE.defaultBlockState() );
            }
        }
        target.setSecondsOnFire( 5 );
    }
}