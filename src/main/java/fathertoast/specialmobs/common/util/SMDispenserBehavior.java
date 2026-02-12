package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import fathertoast.specialmobs.common.entity.projectile.SlabFireballEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class SMDispenserBehavior {
    
    public static void registerBehaviors() {
        DispenserBlock.registerBehavior( SMItems.INCORPOREAL_FIREBALL.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile( Level level, Position pos, ItemStack itemStack ) {
                final double x = pos.x();
                final double y = pos.y();
                final double z = pos.z();
                LivingEntity nearestEntity = level.getNearestEntity( LivingEntity.class, TargetingConditions.DEFAULT, null, x, y, z,
                        new AABB( BlockPos.containing( x + 0.5D, y + 0.5D, z + 0.5D ) ).inflate( 50.0F ) );
                
                return new IncorporealFireballEntity( level, null, nearestEntity, x, y, z );
            }
            
            @Override
            protected void playSound( BlockSource source ) {
                References.LevelEvent.BLAZE_SHOOT.play( source.getLevel(), source.getPos() );
            }
        } );
        
        DispenserBlock.registerBehavior( SMItems.SLAB_FIREBALL.get(), new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute( BlockSource source, ItemStack itemStack ) {
                Direction direction = source.getBlockState().getValue( DispenserBlock.FACING );
                Position pos = DispenserBlock.getDispensePosition( source );
                
                double x = pos.x() + (double) ((float) direction.getStepX() * 0.3F);
                double y = pos.y() + (double) ((float) direction.getStepY() * 0.3F);
                double z = pos.z() + (double) ((float) direction.getStepZ() * 0.3F);
                
                Level level = source.getLevel();
                RandomSource random = level.random;
                
                double dx = random.triangle( direction.getStepX(), 0.1 );
                double dy = random.triangle( direction.getStepY(), 0.1 );
                double dz = random.triangle( direction.getStepZ(), 0.1 );
                
                SlabFireballEntity slabFireball = new SlabFireballEntity( level, x, y, z, dx, dy, dz );
                
                level.addFreshEntity( Util.make( slabFireball, ( fireball ) -> {
                    fireball.setItem( itemStack );
                } ) );
                itemStack.shrink( 1 );
                return itemStack;
            }
            
            @Override
            protected void playSound( BlockSource source ) {
                References.LevelEvent.BLAZE_SHOOT.play( source.getLevel(), source.getPos() );
            }
        } );
    }
}
