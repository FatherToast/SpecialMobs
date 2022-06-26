package fathertoast.specialmobs.common.entity.magmacube;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class HardenedMagmaCubeEntity extends _SpecialMagmaCubeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 3.06F, 3.06F );
        return new BestiaryInfo( 0xDF7679, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return _SpecialMagmaCubeEntity.createAttributes(); // Slimes define their attributes elsewhere based on size
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
    
    @SpecialMob.Constructor
    public HardenedMagmaCubeEntity( EntityType<? extends _SpecialMagmaCubeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 1.5F );
        slimeExperienceValue += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to modify this slime's base attributes by size. */
    @Override
    protected void modifyVariantAttributes( int size ) {
        addAttribute( Attributes.MAX_HEALTH, 2.0 * size + 8.0 );
        addAttribute( Attributes.ARMOR, 8.0 );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new SpecialLeapAtTargetGoal(
                this, 10, 0.0F, 5.0F, 1.16F, 1.0F ) );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final float forwardPower = 0.8F;
            final float upwardPower = 0.5F;
            final Vector3d vKnockback = new Vector3d( target.getX() - getX(), 0.0, target.getZ() - getZ() )
                    .normalize().scale( forwardPower ).add( getDeltaMovement().scale( 0.2F ) );
            target.setDeltaMovement( vKnockback.x, 0.4 * upwardPower, vKnockback.z );
            target.hurtMarked = true;
            
            setDeltaMovement( getDeltaMovement().multiply( 0.2, 1.0, 0.2 ) );
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "hardened" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}