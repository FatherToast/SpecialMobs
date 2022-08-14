package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

@SpecialMob
public class BlueberrySlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlueberrySlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x766BBC ).theme( BestiaryInfo.Theme.WATER )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).drownImmune().fluidPushImmune()
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 );
    }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpeciesSpawnPlacement( MobFamily.Species<? extends BlueberrySlimeEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, EntitySpawnPlacementRegistry.PlacementType.IN_WATER,
                _SpecialSlimeEntity::checkFamilySpawnRules );
    }
    
    /** @return True if this entity's position is currently obstructed. */
    @Override
    public boolean checkSpawnObstruction( IWorldReader world ) { return world.isUnobstructed( this ); }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Blueberry Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addRareDrop( "rare", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
        loot.addUncommonDrop( "uncommon", Items.BLUE_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<BlueberrySlimeEntity> getVariantFactory() { return BlueberrySlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BlueberrySlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BlueberrySlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) {
        super( entityType, world );
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, 1 ); // SlimeEntity.FloatGoal
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        
        // Hacky way of attacking submerged targets; drop down on them when directly above
        double floatAccel = 0.06;
        final LivingEntity target = getTarget();
        if( target != null && target.getY( 0.5 ) < getY( 0.5 ) ) {
            //
            final double dX = target.getX() - getX();
            final double dZ = target.getZ() - getZ();
            final float range = (target.getBbWidth() + getBbWidth() + 0.1F) / 2.0F;
            if( dX * dX + dZ * dZ < range * range )
                floatAccel = -0.12;
        }
        if( tickCount > 1 && getFluidHeight( FluidTags.WATER ) > 0.0 ) {
            if( !ISelectionContext.of( this ).isAbove( FlowingFluidBlock.STABLE_SHAPE, blockPosition(), true ) ||
                    level.getFluidState( blockPosition().above() ).is( FluidTags.WATER ) ) {
                setDeltaMovement( getDeltaMovement().scale( 0.5 ).add( 0.0, floatAccel, 0.0 ) );
            }
        }
    }
    
    // The below two methods are here to effectively override the private Entity#isInRain to always return true (always wet)
    @Override
    public boolean isInWaterOrRain() { return true; }
    
    @Override
    public boolean isInWaterRainOrBubble() { return true; }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.9F; }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return ParticleTypes.SPLASH; }
}