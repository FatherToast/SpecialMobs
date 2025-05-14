package fathertoast.specialmobs.common.entity.skeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.IBoatRider;
import fathertoast.specialmobs.common.entity.ai.goal.ControlBoatGoal;
import fathertoast.specialmobs.common.entity.ai.goal.PirateSpawnBoatGoal;
import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class PirateSkeletonEntity extends _SpecialSkeletonEntity implements IBoatRider {

    //--------------- Static Special Mob Hooks ----------------

    @SpecialMob.SpeciesReference
    public static MobFamily.Species<PirateSkeletonEntity> SPECIES;

    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        // TODO - change the colors, maybe?
        bestiaryInfo.color( 0xFFF87E )
                .weight(BestiaryInfo.DefaultWeight.DISABLED)
                .modBaseTexture( "textures/entity/skeleton/base_skeleton.png" ).uniqueOverlayTexture()
                .addExperience( 3 )
                .addToAttribute( Attributes.MAX_HEALTH, 15.0 )
                .addToAttribute( Attributes.FOLLOW_RANGE, 40.0D );
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Pirate Skeleton",
                "", "", "", "", "", "" );//TODO
    }

    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.GOLD_NUGGET, 3 );
        loot.addSemicommonDrop( "semicommon", Items.MAP );
        loot.addRareDrop("rare", Items.GOLD_INGOT );
    }

    @SpecialMob.Factory
    public static EntityType.EntityFactory<PirateSkeletonEntity> getVariantFactory() { return PirateSkeletonEntity::new; }

    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends PirateSkeletonEntity> getSpecies() { return SPECIES; }

    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialSkeletonEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement(species, SpawnPlacements.Type.NO_RESTRICTIONS,
                PirateSkeletonEntity::checkPirateSkeletonSpawnRules);
    }

    private static boolean checkPirateSkeletonSpawnRules( EntityType<? extends _SpecialSkeletonEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        boolean isDarkEnough = Monster.isDarkEnoughToSpawn( level, pos, random );

        return isDarkEnough
                && pos.getY() <= level.getSeaLevel()
                && level.getBlockState( pos ).isAir()
                && level.getFluidState( pos.below() ).is( FluidTags.WATER )
                && level.getBlockState( pos.above() ).isAir();
    }


    //--------------- Variant-Specific Implementations ----------------

    /** True if this pirate skeleton has spawned a boat. Used to prevent them from spawning more than one. */
    public static final EntityDataAccessor<Boolean> SPAWNED_BOAT = SynchedEntityData.defineId( PirateSkeletonEntity.class, EntityDataSerializers.BOOLEAN );


    public PirateSkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, Level level ) { super( entityType, level ); }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( SPAWNED_BOAT, false );
    }

    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, MobEffects.GLOWING );
    }

    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new FloatGoal( this ) );
        goalSelector.addGoal( 1, new PirateSpawnBoatGoal( this ) );
        goalSelector.addGoal( 1, new ControlBoatGoal( this, 512.0D, 256.0D ) );

        AIHelper.replaceWaterAvoidingRandomWalking( this, 1.0D );
    }

    /**
     * Make sure the Pirate Skeleton always has a bow so it is not
     * completely helpless out in the ocean.
     */
    @Override
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty,
                                      @Nullable MobSpawnType spawnType, @Nullable SpawnGroupData groupData ) {
        setItemInHand( InteractionHand.MAIN_HAND, new ItemStack( Items.BOW ) );
        enchantSpawnedWeapon( level.getRandom(), difficulty.getSpecialMultiplier() );
    }

    // TODO - This shit doesn't work! Lets fix it, eventually!
    /** Despawn the pirate skelly's boat if it was discarded or despawned naturally. */
    @Override
    public void onRemovedFromWorld() {
        if ( getRemovalReason() == RemovalReason.DISCARDED && getVehicle() instanceof MobBoat mobBoat ) {
            mobBoat.discard();
        }
    }

    /** Make sure the pirate skeleton can despawn naturally while in their boat. */
    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }
}
