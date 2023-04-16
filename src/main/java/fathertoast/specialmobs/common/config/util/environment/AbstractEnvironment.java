package fathertoast.specialmobs.common.config.util.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class AbstractEnvironment {
    /** @return The string representation of this environment, as it would appear in a config file. */
    @Override
    public final String toString() { return name() + " " + value(); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    public abstract String name();
    
    /** @return The string value of this environment, as it would appear in a config file. */
    public abstract String value();
    
    /** @return Returns true if this environment matches the provided environment. */
    public abstract boolean matches( Level level, @Nullable BlockPos pos );
}