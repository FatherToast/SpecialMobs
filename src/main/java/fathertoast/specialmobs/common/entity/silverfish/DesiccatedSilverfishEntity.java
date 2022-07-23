package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.fluid.Fluids;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SpecialMob
public class DesiccatedSilverfishEntity extends _SpecialSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DesiccatedSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE7E7E7 ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).undead()
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 ).addToAttribute( Attributes.ARMOR, 2.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Desiccated Silverfish",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.ZOMBIE.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DesiccatedSilverfishEntity> getVariantFactory() { return DesiccatedSilverfishEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DesiccatedSilverfishEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public DesiccatedSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        clearWater( new BlockPos( position() ) );
        super.aiStep();
    }
    
    private void clearWater( BlockPos pos ) {
        if( !level.getFluidState( pos ).is( FluidTags.WATER ) ) return;
        
        final BlockState block = level.getBlockState( pos );
        
        if( block.getBlock() instanceof IBucketPickupHandler &&
                ((IBucketPickupHandler) block.getBlock()).takeLiquid( level, pos, block ) != Fluids.EMPTY ) {
            // Removed through bucket pickup handler
            heal( 1.0F );
            return;
        }
        
        if( block.getBlock() instanceof FlowingFluidBlock ) {
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SET_BLOCK_FLAGS );
            heal( 1.0F );
            return;
        }
        
        final Material material = block.getMaterial();
        if( material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT ) {
            final TileEntity tileEntity = block.hasTileEntity() ? level.getBlockEntity( pos ) : null;
            Block.dropResources( block, level, pos, tileEntity );
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SET_BLOCK_FLAGS );
            heal( 1.0F );
        }
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.HUNGER, duration ) );
        }
    }
    
    /** @return This entity's creature type. */
    @Override
    public CreatureAttribute getMobType() { return CreatureAttribute.UNDEAD; }
}