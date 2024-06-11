package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class SMDispenserBehavior {

    public static void registerBehaviors() {
        DispenserBlock.registerBehavior(SMItems.INCORPOREAL_FIREBALL.get(), new AbstractProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position pos, ItemStack itemStack) {
                final double x = pos.x();
                final double y = pos.y();
                final double z = pos.z();
                LivingEntity nearestEntity = level.getNearestEntity(LivingEntity.class, TargetingConditions.DEFAULT, null, x, y, z,
                        new AABB(BlockPos.containing(x + 0.5D, y + 0.5D, z + 0.5D)).inflate(50.0F));

                return new IncorporealFireballEntity(level, null, nearestEntity, x, y, z);
            }
        });
    }
}
