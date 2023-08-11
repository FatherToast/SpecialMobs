package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends IceWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Ticks before this witch can use its ice wall ability. */
    private int wallDelay;
    
    public IceWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) { super( entityType, level ); }
    
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
        if( !level.isClientSide() && isAlive() && wallDelay-- <= 0 && target != null && random.nextInt( 20 ) == 0 ) {
            
            // Create an ice wall behind the target if they are vulnerable
            final double distanceSq = target.distanceToSqr( this );
            if( distanceSq > 100.0 && distanceSq < 196.0 && target.hasEffect( MobEffects.MOVEMENT_SLOWDOWN ) && hasLineOfSight( target ) ) {
                wallDelay = 200;
                
                //TODO
            }
        }
        super.aiStep();
    }
}