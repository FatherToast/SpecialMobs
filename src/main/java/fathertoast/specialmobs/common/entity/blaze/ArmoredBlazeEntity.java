package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.BlazeSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ArmoredBlazeEntity extends _SpecialBlazeEntity {


    //--------------- Static Special Mob Hooks ----------------

    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ArmoredBlazeEntity> SPECIES;

    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE5E5E5 )
                .uniqueTextureBaseOnly()
                .addExperience( 4 )
                .fireballAttack( 0, 80, 120, 40.0 )
                .addToAttribute( Attributes.MAX_HEALTH, 15.0 );
    }

    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig(ConfigManager manager, MobFamily.Species<?> species ) {
        return new BlazeSpeciesConfig( manager, species, 5, 10 );
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Armored Blaze",
                "", "", "", "", "", "" );//TODO
    }

    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BLAZE_ROD );
        loot.addSemicommonDrop("semicommon", Items.IRON_INGOT);
    }

    @SpecialMob.Factory
    public static EntityType.EntityFactory<ArmoredBlazeEntity> getVariantFactory() { return ArmoredBlazeEntity::new; }

    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ArmoredBlazeEntity> getSpecies() { return SPECIES; }


    //--------------- Variant-Specific Implementations ----------------

    public static final EntityDataAccessor<Integer> ARMOR_LEVEL = SynchedEntityData.defineId( ArmoredBlazeEntity.class, EntityDataSerializers.INT );

    /** The base explosion strength of this blaze's fireballs. */
    private int explosionPower = 2;

    public ArmoredBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, Level level ) { super( entityType, level ); }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( ARMOR_LEVEL, 7 );
    }

    public boolean hasArmor() {
        return entityData.get( ARMOR_LEVEL ) > 0;
    }

    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if ( entityData.get( ARMOR_LEVEL ) > 0 ) {
            // Make sure invul bypass damage actually does damage before
            // we do anything else
            if ( source.is( DamageTypeTags.BYPASSES_INVULNERABILITY ) ) {
                return super.hurt( source, amount);
            }
            // Check if damage type should damage the blaze's armor
            if ( !source.is( SMTags.IS_MAGIC ) && !source.is( DamageTypeTags.IS_PROJECTILE ) && amount >= 2.0F ) {
                Entity attacker = source.getDirectEntity();
                int newArmorLevel = entityData.get( ARMOR_LEVEL ) - 1;
                entityData.set( ARMOR_LEVEL, newArmorLevel );

                // If armor is depleted, play some cool effects
                if ( newArmorLevel <= 0 ) {
                    if ( !level().isClientSide ) {
                        ((ServerLevel) level()).sendParticles(
                                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.IRON_HELMET)),
                                getX() + 0.5D,
                                getY() + 0.5D,
                                getZ() + 0.5D,
                                5,
                                0.02,
                                0.02,
                                0.02,
                                0
                        );
                    }
                    playSound( SoundEvents.ITEM_BREAK );
                }
                else {
                    // Try knocking back attacker
                    if ( attacker instanceof LivingEntity livingEntity ) {
                        livingEntity.knockback(0.5F,
                                getX() - attacker.getX(), getZ() - attacker.getZ());
                    }
                    playSound( SoundEvents.ANVIL_PLACE, 0.8F, 0.8F + level().random.nextFloat() * 0.4F );
                }
                return true;
            }
            return false;
        }
        else {
            return super.hurt( source, amount );
        }
    }

    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundTag saveTag ) {
        saveTag.putByte( References.TAG_EXPLOSION_POWER, (byte) explosionPower );
        saveTag.putInt( "ArmorLevel", entityData.get( ARMOR_LEVEL ) );
    }

    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( saveTag.contains( References.TAG_EXPLOSION_POWER, References.NBT_TYPE_NUMERICAL ) )
            explosionPower = saveTag.getByte( References.TAG_EXPLOSION_POWER );

        if ( saveTag.contains( "ArmorLevel", Tag.TAG_INT ) ) {
            entityData.set( ARMOR_LEVEL, saveTag.getInt( "ArmorLevel" ) );
        }
    }
}
