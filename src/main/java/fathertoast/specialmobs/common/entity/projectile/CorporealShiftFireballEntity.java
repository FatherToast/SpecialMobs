package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class CorporealShiftFireballEntity extends AbstractFireballEntity {

    private static final DataParameter<Boolean> CORPOREAL = EntityDataManager.defineId(CorporealShiftFireballEntity.class, DataSerializers.BOOLEAN);

    public int explosionPower = 1;
    private boolean shouldExplode = false;

    @Nullable
    private LivingEntity target;


    public CorporealShiftFireballEntity(EntityType<? extends AbstractFireballEntity> entityType, World world) {
        super(entityType, world);
    }

    public CorporealShiftFireballEntity(World world, CorporealShiftGhastEntity ghast, double x, double y, double z) {
        super(SMEntities.CORPOREAL_FIREBALL.get(), ghast, x, y, z, world);
        setCorporeal(ghast.isCorporeal());
        target = ghast.getTarget();
        setItem(isCorporeal() ? new ItemStack(Items.FIRE_CHARGE) : new ItemStack(SMItems.INCORPOREAL_FIREBALL.get()));
    }

    public CorporealShiftFireballEntity(World world, PlayerEntity owner, LivingEntity target, double x, double y, double z) {
        super(SMEntities.CORPOREAL_FIREBALL.get(), owner, x, y, z, world);
        setCorporeal(false);
        this.target = target;
        setItem(new ItemStack(SMItems.INCORPOREAL_FIREBALL.get()));
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Corporeal Shift Fireball",
                "", "", "", "", "", "" );//TODO
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CORPOREAL, true);
    }

    public boolean isCorporeal() {
        return entityData.get(CORPOREAL);
    }

    public void setCorporeal(boolean corporeal) {
        entityData.set(CORPOREAL, corporeal);
    }

    @Override
    public void tick() {
        super.tick();

        if ( !level.isClientSide && !isCorporeal() ) {
            // Fizzle out and die when the target is dead or lost,
            // or else the fireball goes bonkers.
            if ( target == null || !target.isAlive() ) {
                playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
                remove();
                return;
            }
            // Follow target
            Vector3d vector3d = new Vector3d( target.getX() - this.getX(), (target.getY() + (target.getEyeHeight() / 2)) - this.getY(), target.getZ() - this.getZ() );
            setDeltaMovement(vector3d.normalize().scale(0.5));
        }

        if ( !level.isClientSide && shouldExplode )
            explode();
    }

    private void explode() {
        boolean mobGrief = ForgeEventFactory.getMobGriefingEvent( level, getOwner() );
        Explosion.Mode mode = mobGrief ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;

        level.explode( null, this.getX(), this.getY(), this.getZ(), (float)explosionPower, mobGrief, mode );
        target = null;
        remove();
    }

    @Override
    protected boolean shouldBurn() {
        // Hee hee hee haw
        return isCorporeal();
    }

    @Override
    protected void onHit( RayTraceResult traceResult ) {
        super.onHit( traceResult );

        // Only go boom if the fireball is corporeal.
        // If not, pass through blocks to be a menace.
        if ( !level.isClientSide && isCorporeal() ) {
            shouldExplode = true;
        }
    }

    @Override
    protected void onHitEntity( EntityRayTraceResult traceResult ) {
        super.onHitEntity(traceResult);

        if ( !this.level.isClientSide ) {
            Entity target = traceResult.getEntity();
            Entity owner = getOwner();

            if (!isCorporeal()) {
                // TODO - Figure out why this is cringe
                // TODO part 2. - What the fuck
                // TODO part 3. - HELP
                SpecialMobs.LOG.info("X={}, XO={}", target.getX(), target.xo);
                SpecialMobs.LOG.info("Z={}, ZO={}", target.getZ(), target.zo);

                if (target.getX() != target.xo || target.getY() != target.yo || target.getZ() != target.zo) {
                    explode();
                }
                else {
                    playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
                    remove();
                }
            }
            else {
                target.hurt(DamageSource.fireball(this, owner), 6.0F);

                if (owner instanceof LivingEntity) {
                    doEnchantDamageEffects((LivingEntity) owner, target);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (!isCorporeal()) {
            if (isInvulnerableTo(damageSource) || damageSource.isFire()) {
                return false;
            }
            shouldExplode = true;
            return true;
        }
        else {
            return super.hurt(damageSource, damage);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compoundNBT) {
        super.addAdditionalSaveData(compoundNBT);
        compoundNBT.putInt("ExplosionPower", explosionPower);
        compoundNBT.putBoolean("Corporeal", isCorporeal());
        compoundNBT.putInt("TargetId", target == null ? -1 : target.getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        if (compoundNBT.contains("ExplosionPower", Constants.NBT.TAG_ANY_NUMERIC)) {
            explosionPower = compoundNBT.getInt("ExplosionPower");
        }
        entityData.set(CORPOREAL, compoundNBT.getBoolean("Corporeal"));

        if (compoundNBT.contains("TargetId", Constants.NBT.TAG_ANY_NUMERIC)) {
            Entity entity = level.getEntity(compoundNBT.getInt("TargetId"));

            if (entity instanceof LivingEntity) {
                target = (LivingEntity) entity;
            }
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
