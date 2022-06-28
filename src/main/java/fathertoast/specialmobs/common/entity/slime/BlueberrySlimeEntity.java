package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class BlueberrySlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlueberrySlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x766BBC );
        //TODO theme - water
    }
    
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
    
    @SpecialMob.Constructor
    public BlueberrySlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setCanBreatheInWater( true );
        getSpecialData().setIgnoreWaterPush( true );
        slimeExperienceValue += 1;
        
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to modify this slime's base attributes by size. */
    @Override
    protected void modifyVariantAttributes( int size ) {
        addAttribute( Attributes.ATTACK_DAMAGE, 1.0 * size );
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, 1 ); // SlimeEntity.FloatGoal
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigator createNavigation( World world ) {
        return new FluidPathNavigator( this, world, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( Fluid fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.06, FluidTags.WATER );
    }
    
    // The below two methods are here to effectively override the private Entity#isInRain to always return true (always wet)
    @Override
    public boolean isInWaterOrRain() { return true; }
    
    @Override
    public boolean isInWaterRainOrBubble() { return true; }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return ParticleTypes.SPLASH; }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "blueberry" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}