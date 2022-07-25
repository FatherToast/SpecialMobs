package fathertoast.specialmobs.common.entity.magmacube;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@SpecialMob
public class HardenedMagmaCubeEntity extends _SpecialMagmaCubeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HardenedMagmaCubeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDF7679 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureBaseOnly()
                .size( 1.5F, 3.06F, 3.06F )
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 8.0 ).addToAttribute( Attributes.ARMOR, 16.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 1.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Hardened Magma Cube",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "uncommon", Blocks.MAGMA_BLOCK );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<HardenedMagmaCubeEntity> getVariantFactory() { return HardenedMagmaCubeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HardenedMagmaCubeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public HardenedMagmaCubeEntity( EntityType<? extends _SpecialMagmaCubeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new SpecialLeapAtTargetGoal(
                this, 10, 0.0F, 5.0F, 1.16F, 1.0F ) );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        final float forwardPower = 0.8F;
        final float upwardPower = 0.5F;
        final Vector3d vKnockback = new Vector3d( target.getX() - getX(), 0.0, target.getZ() - getZ() )
                .normalize().scale( forwardPower ).add( getDeltaMovement().scale( 0.2F ) );
        target.setDeltaMovement( vKnockback.x, 0.4 * upwardPower, vKnockback.z );
        target.hurtMarked = true;
        
        setDeltaMovement( getDeltaMovement().multiply( 0.2, 1.0, 0.2 ) );
    }
}