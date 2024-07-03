package fathertoast.specialmobs.common.entity.witch;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.UndeadWitchSpeciesConfig;
import fathertoast.specialmobs.common.entity.skeleton._SpecialSkeletonEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@SpecialMob
public class UndeadWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<UndeadWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x799C65 )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).undead();
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new UndeadWitchSpeciesConfig( manager, species, 3, 6 );
    }
    
    /** @return This entity's species config. */
    public UndeadWitchSpeciesConfig getConfig() { return (UndeadWitchSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Lich",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.ZOMBIE.getDefaultLootTable() );
        loot.addUncommonDrop( "uncommon", Items.SKELETON_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<UndeadWitchEntity> getVariantFactory() { return UndeadWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends UndeadWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of skeletons this witch can spawn. */
    private int summons;
    
    public UndeadWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) {
        super( entityType, level );
        summons = getConfig().UNDEAD.summons.next( random );
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( summons > 0 && random.nextFloat() < (isNearSkeletons() ? 0.25F : 0.75F) ) {
            summons--;
            
            final _SpecialSkeletonEntity skeleton = _SpecialSkeletonEntity.SPECIES.entityType.get().create( level() );
            if( skeleton != null ) {
                skeleton.copyPosition( this );
                skeleton.yHeadRot = getYRot();
                skeleton.yBodyRot = getYRot();
                skeleton.finalizeSpawn( (ServerLevelAccessor) level(), level().getCurrentDifficultyAt( blockPosition() ),
                        MobSpawnType.MOB_SUMMONED, null, null );
                skeleton.setItemSlot( EquipmentSlot.MAINHAND, new ItemStack( Items.IRON_SWORD ) );
                skeleton.setItemSlot( EquipmentSlot.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
                skeleton.setTarget( getTarget() );
                
                final double vX = target.getX() - getX();
                final double vZ = target.getZ() - getZ();
                final double vH = Math.sqrt( vX * vX + vZ * vZ );
                skeleton.setDeltaMovement(
                        vX / vH * 0.7 + getDeltaMovement().x * 0.2,
                        0.4, // Used to cause floor clip bug; remove if it happens again
                        vZ / vH * 0.7 + getDeltaMovement().z * 0.2 );
                skeleton.setOnGround( false );

                level().addFreshEntity( skeleton );
                playSound( SoundEvents.BLAZE_SHOOT, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
                skeleton.spawnAnim();
                
                return ItemStack.EMPTY;
            }
        }
        
        // Only throw harming potions - heals self and minions, while probably damaging the target
        return random.nextFloat() < 0.2F ? makeLingeringPotion( Potions.HARMING ) : makeSplashPotion( Potions.HARMING );
    }
    
    /** @return True if there are any skeletons near this entity. */
    private boolean isNearSkeletons() {
        return level().getEntitiesOfClass( AbstractSkeleton.class, getBoundingBox().inflate( 11.0 ) ).size() > 0;
    }
    
    /** @return This entity's creature type. */
    @Override
    public MobType getMobType() { return MobType.UNDEAD; }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundTag saveTag ) {
        saveTag.putByte( References.TAG_SUMMONS, (byte) summons );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( saveTag.contains( References.TAG_SUMMONS, References.NBT_TYPE_NUMERICAL ) )
            summons = saveTag.getByte( References.TAG_SUMMONS );
    }
}