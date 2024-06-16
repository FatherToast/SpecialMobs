package fathertoast.specialmobs.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Works the same as {@link InfestedBlock} except the type of silverfish
 * that spawns when the block is destroyed can be specified in the constructor.
 */
public class SpecialInfestedBlock extends InfestedBlock {

    /** The EntityType of the mob that spawns when this block is destroyed. */
    private final Supplier<EntityType<? extends Silverfish>> typeToSpawn;

    public SpecialInfestedBlock(Supplier<EntityType<? extends Silverfish>> typeToSpawn, Block parent, Properties properties) {
        super(parent, properties);
        Objects.requireNonNull(typeToSpawn);
        this.typeToSpawn = typeToSpawn;
    }

    @Override
    protected void spawnInfestation(ServerLevel serverLevel, BlockPos pos) {
        Silverfish silverfish = typeToSpawn.get().create(serverLevel);

        if (silverfish != null) {
            silverfish.moveTo((double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D, 0.0F, 0.0F);
            serverLevel.addFreshEntity(silverfish);
            silverfish.spawnAnim();
        }
    }

    public EntityType<? extends LivingEntity> getTypeToSpawn() {
        return typeToSpawn.get();
    }
}
