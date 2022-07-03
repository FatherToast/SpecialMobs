package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.projectile.CorporealShiftFireballEntity;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

@SpecialMob
public class CorporealShiftGhastEntity extends _SpecialGhastEntity {

    //--------------- Static Special Mob Hooks ----------------

    @SpecialMob.SpeciesReference
    public static MobFamily.Species<CorporealShiftGhastEntity> SPECIES;

    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo(EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 6.0F, 6.0F );
        return new BestiaryInfo( 0xA7FF9B, BestiaryInfo.BaseWeight.LOW );
    }

    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialGhastEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 20.0 )
                .addAttribute( Attributes.ARMOR, 0.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 0.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.8 )
                .build();
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Corporeal Shift Ghast",
                "", "", "", "", "", "" );//TODO
    }

    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        // TODO - Uh uhm uhhhhh hmmm..
        loot.addSemicommonDrop( "semicommon", Items.POISONOUS_POTATO );
    }

    @SpecialMob.Factory
    public static EntityType.IFactory<CorporealShiftGhastEntity> getVariantFactory() { return CorporealShiftGhastEntity::new; }


    //--------------- Variant-Specific Implementations ----------------

    public static final DataParameter<Boolean> CORPOREAL = EntityDataManager.defineId(CorporealShiftGhastEntity.class, DataSerializers.BOOLEAN);

    private final int maxShiftTime = 600;
    private int shiftTime = maxShiftTime;

    public CorporealShiftGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setRegenerationTime( 80 );
        getSpecialData().setBaseScale( 1.0F );
        xpReward += 2;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CORPOREAL, false);
    }

    @Override
    public void tick() {
        super.tick();

        if ( --shiftTime <= 0 ) {
            if ( !level.isClientSide ) {
                shiftTime = maxShiftTime;
                entityData.set(CORPOREAL, !entityData.get(CORPOREAL));
                spawnShiftSmoke((ServerWorld)level);
            }
        }
    }

    private void spawnShiftSmoke(ServerWorld world) {
        world.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 25, 0.0, 0.0, 0.0, 0.4);
    }

    public boolean isCorporeal() {
        return entityData.get(CORPOREAL);
    }

    /** Override to change this ghast's explosion power multiplier. */
    @Override
    protected int getVariantExplosionPower( int radius ) { return Math.round( radius * 2.5F ); }

    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( !isSilent() ) level.levelEvent( null, References.EVENT_GHAST_SHOOT, blockPosition(), 0 );

        final float accelVariance = MathHelper.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().rangedAttackSpread;
        final Vector3d lookVec = getViewVector( 1.0F ).scale( getBbWidth() );
        double dX = target.getX() - (getX() + lookVec.x) + getRandom().nextGaussian() * accelVariance;
        double dY = target.getY( 0.5 ) - (0.5 + getY( 0.5 ));
        double dZ = target.getZ() - (getZ() + lookVec.z) + getRandom().nextGaussian() * accelVariance;

        final CorporealShiftFireballEntity fireball = new CorporealShiftFireballEntity( level, this, dX, dY, dZ );
        fireball.explosionPower = getVariantExplosionPower( getExplosionPower() );
        fireball.setPos(
                getX() + lookVec.x,
                getY( 0.5 ) + 0.5,
                getZ() + lookVec.z );
        level.addFreshEntity( fireball );
    }

    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "corporeal_shift" ),
            null,
            GET_TEXTURE_PATH( "corporeal_shift_shoot" )
    };

    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }


    //--------------- SpecialMobData Hooks ----------------

    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt(DamageSource source, float amount ) {
        return isCorporeal() && super.hurt(source, amount);
    }

    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        tag.putInt("ShiftTime", shiftTime);
    }

    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );

        if (tag.contains("ShiftTime", Constants.NBT.TAG_ANY_NUMERIC)) {
            shiftTime = tag.getInt("ShiftTime");
        }
    }
}
