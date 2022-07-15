package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.WildsWitchSpeciesConfig;
import fathertoast.specialmobs.common.entity.spider.BabySpiderEntity;
import fathertoast.specialmobs.common.entity.spider._SpecialSpiderEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class WildsWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WildsWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xA80E0E ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).spider()
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.7 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new WildsWitchSpeciesConfig( species, 1, 3,
                3, 6, 3, 4 );
    }
    
    /** @return This entity's species config. */
    public WildsWitchSpeciesConfig getConfig() { return (WildsWitchSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch of the Wilds",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.SPIDER_SPAWN_EGG );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<WildsWitchEntity> getVariantFactory() { return WildsWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WildsWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of spider mounts this witch can spawn. */
    private int spiderMounts;
    /** The number of spider swarm attacks this witch can cast. */
    private int spiderSwarms;
    /** The number of baby spiders to spawn in each spider swarm attack. */
    private int spiderSwarmSize;
    
    public WildsWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, World world ) {
        super( entityType, world );
        spiderMounts = getConfig().WILDS.mounts.next( random );
        spiderSwarms = getConfig().WILDS.swarms.next( random );
        spiderSwarmSize = getConfig().WILDS.swarmSize.next( random );
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( spiderSwarms > 0 && random.nextFloat() < 0.33F ) {
            spiderSwarms--;
            
            ILivingEntityData groupData = null;
            for( int i = 0; i < spiderSwarmSize; i++ ) {
                groupData = spawnBaby( groupData );
            }
            spawnAnim();
            playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
            
            return ItemStack.EMPTY;
        }
        if( !target.hasEffect( Effects.POISON ) ) {
            return makeSplashPotion( Potions.STRONG_POISON );
        }
        // Save the spiders
        final Potion originalType = PotionUtils.getPotion( originalPotion );
        if( originalType == Potions.HARMING || originalType == Potions.STRONG_HARMING ) {
            return makeSplashPotion( Potions.STRONG_POISON );
        }
        return originalPotion;
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private ILivingEntityData spawnBaby( @Nullable ILivingEntityData groupData ) {
        final BabySpiderEntity baby = BabySpiderEntity.SPECIES.entityType.get().create( level );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = yRot;
        baby.yBodyRot = yRot;
        groupData = baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                SpawnReason.MOB_SUMMONED, groupData, null );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement(
                (random.nextDouble() - 0.5) * 0.33,
                random.nextDouble() * 0.5, // Used to cause floor clip bug; remove if it happens again
                (random.nextDouble() - 0.5) * 0.33 );
        baby.setOnGround( false );
        
        level.addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to add additional potions this witch can drink if none of the base potions are chosen. */
    @Override
    protected void tryVariantUsingPotion() {
        final LivingEntity mount = getVehicle() instanceof LivingEntity ? (LivingEntity) getVehicle() : null;
        
        if( mount != null && random.nextFloat() < 0.15F && mount.isEyeInFluid( FluidTags.WATER ) &&
                !mount.hasEffect( Effects.WATER_BREATHING ) ) {
            usePotion( makeSplashPotion( Potions.WATER_BREATHING ) );
        }
        else if( mount != null && random.nextFloat() < 0.15F && (mount.isOnFire() || mount.getLastDamageSource() != null &&
                mount.getLastDamageSource().isFire()) && !hasEffect( Effects.FIRE_RESISTANCE ) ) {
            usePotion( makeSplashPotion( Potions.FIRE_RESISTANCE ) );
        }
        else if( mount != null && random.nextFloat() < 0.05F && mount.getMobType() != CreatureAttribute.UNDEAD &&
                mount.getHealth() < mount.getMaxHealth() ) {
            usePotion( makeSplashPotion( Potions.HEALING ) );
        }
        else if( !MobFamily.WITCH.config.WITCHES.useSplashSwiftness.get() && mount != null && random.nextFloat() < 0.5F && getTarget() != null &&
                !mount.hasEffect( Effects.MOVEMENT_SPEED ) && getTarget().distanceToSqr( this ) > 121.0 ) {
            usePotion( makeSplashPotion( Potions.SWIFTNESS ) );
        }
        else if( spiderMounts > 0 && random.nextFloat() < 0.15F && getVehicle() == null && getTarget() != null &&
                getTarget().distanceToSqr( this ) > 100.0 ) {
            final _SpecialSpiderEntity spider = _SpecialSpiderEntity.SPECIES.entityType.get().create( level );
            if( spider != null ) {
                spider.copyPosition( this );
                spider.yHeadRot = yRot;
                spider.yBodyRot = yRot;
                
                if( level.noCollision( spider.getBoundingBox() ) ) {
                    spiderMounts--;
                    potionUseCooldownTimer = 40;
                    
                    spider.setTarget( getTarget() );
                    spider.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                            SpawnReason.MOB_SUMMONED, null, null );
                    level.addFreshEntity( spider );
                    spider.spawnAnim();
                    playSound( SoundEvents.BLAZE_SHOOT, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
                    
                    startRiding( spider, true );
                }
                else {
                    // Cancel spawn; spider is in too small of a space
                    spider.remove();
                }
            }
        }
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        // With changes to mount/rider mechanics, is this still needed?
        //        if( getVehicle() instanceof MobEntity && getTarget() != null && random.nextInt( 10 ) == 0 ) {
        //            ((MobEntity) getVehicle()).setTarget( getTarget() );
        //        }
        super.aiStep();
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_SUMMONS, (byte) spiderMounts );
        saveTag.putByte( References.TAG_BABIES, (byte) spiderSwarms );
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) spiderSwarmSize );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_SUMMONS, References.NBT_TYPE_NUMERICAL ) )
            spiderMounts = saveTag.getByte( References.TAG_SUMMONS );
        if( saveTag.contains( References.TAG_BABIES, References.NBT_TYPE_NUMERICAL ) )
            spiderSwarms = saveTag.getByte( References.TAG_BABIES );
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            spiderSwarmSize = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
}