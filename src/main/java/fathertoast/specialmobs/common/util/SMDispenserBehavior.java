package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SMDispenserBehavior {

    // TODO - Make it so incorporeal fireball entities can be spawned without an owner
    public static void registerBehaviors() {
        DispenserBlock.registerBehavior(SMItems.INCORPOREAL_FIREBALL.get(), new ProjectileDispenseBehavior() {
            protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack itemStack) {
                final double x = pos.x();
                final double y = pos.y();
                final double z = pos.z();
                LivingEntity nearestEntity = world.getNearestEntity(LivingEntity.class, EntityPredicate.DEFAULT, null, x, y, z,
                        new AxisAlignedBB(new BlockPos(x + 0.5D, y + 0.5D, z + 0.5D)).inflate(50.0F));

                return new IncorporealFireballEntity(world, null, nearestEntity, x, y, z);
            }
        });
    }
}
