package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.DesiccatedSilverfishSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;

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
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new DesiccatedSilverfishSpeciesConfig( species, DEFAULT_SPIT_CHANCE, 64, 64 );
    }
    
    /** @return This entity's species config. */
    @Override
    public DesiccatedSilverfishSpeciesConfig getConfig() { return (DesiccatedSilverfishSpeciesConfig) getSpecies().config; }
    
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
    
    private int absorbCount;
    
    public DesiccatedSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) {
        super( entityType, world );
        absorbCount = getConfig().DESICCATED.absorbCount.next( random );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() && absorbCount > 0 && spongebob() ) spawnAnim();
        super.aiStep();
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.HUNGER );
    }
    
    /** @return This entity's creature type. */
    @Override
    public CreatureAttribute getMobType() { return CreatureAttribute.UNDEAD; }
    
    /** @return Copy of the sponge absorption method, adapted for use by this entity. Returns true if anything is absorbed. */
    private boolean spongebob() {
        final int initialCount = absorbCount;
        final Queue<Tuple<BlockPos, Integer>> posToCheckAround = new ArrayDeque<>();
        tryAbsorb( posToCheckAround, -1, blockPosition() );
        
        while( !posToCheckAround.isEmpty() && absorbCount > 0 ) {
            final Tuple<BlockPos, Integer> tuple = posToCheckAround.poll();
            final BlockPos rootPos = tuple.getA();
            final int rootDistance = tuple.getB();
            
            for( Direction direction : Direction.values() ) {
                tryAbsorb( posToCheckAround, rootDistance, rootPos.relative( direction ) );
                if( absorbCount <= 0 ) break;
            }
        }
        
        return initialCount > absorbCount;
    }
    
    /** Attempts to absorb a single block position. If successful, marks the position to be checked around if it's in range. */
    private void tryAbsorb( Queue<Tuple<BlockPos, Integer>> posToCheckAround, int rootDistance, BlockPos pos ) {
        if( !level.getFluidState( pos ).is( FluidTags.WATER ) ) return;
        
        // Prioritize bucket handler, then empty water block, then water plants
        final BlockState block = level.getBlockState( pos );
        if( block.getBlock() instanceof IBucketPickupHandler &&
                ((IBucketPickupHandler) block.getBlock()).takeLiquid( level, pos, block ) != Fluids.EMPTY ) {
            onAbsorb( posToCheckAround, rootDistance, pos );
        }
        else if( block.getBlock() instanceof FlowingFluidBlock ) {
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
            onAbsorb( posToCheckAround, rootDistance, pos );
        }
        else if( block.getMaterial() == Material.WATER_PLANT || block.getMaterial() == Material.REPLACEABLE_WATER_PLANT ) {
            final TileEntity tileEntity = block.hasTileEntity() ? level.getBlockEntity( pos ) : null;
            Block.dropResources( block, level, pos, tileEntity );
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
            onAbsorb( posToCheckAround, rootDistance, pos );
        }
    }
    
    /** Called when a block is actually absorbed. Marks the position to be checked around if it's in range. */
    private void onAbsorb( Queue<Tuple<BlockPos, Integer>> posToCheckAround, int rootDistance, BlockPos pos ) {
        if( rootDistance < 6 ) posToCheckAround.add( new Tuple<>( pos, rootDistance + 1 ) );
        
        heal( 1.0F );
        absorbCount--;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_AMMO, (byte) absorbCount );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_AMMO, References.NBT_TYPE_NUMERICAL ) )
            absorbCount = saveTag.getByte( References.TAG_AMMO );
    }
}