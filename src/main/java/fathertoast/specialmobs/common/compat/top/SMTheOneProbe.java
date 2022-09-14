package fathertoast.specialmobs.common.compat.top;

import mcjty.theoneprobe.api.ITheOneProbe;

import javax.annotation.Nullable;
import java.util.function.Function;

public class SMTheOneProbe implements Function<ITheOneProbe, Void> {

    @Nullable
    public static ITheOneProbe TOP;

    @Override
    public Void apply(ITheOneProbe probe) {
        TOP = probe;
        setup(probe);
        return null;
    }

    private void setup(ITheOneProbe probe) {
        probe.registerEntityDisplayOverride(new NinjaEntityDisplayOverride());
        probe.registerProbeConfigProvider(new SMProbeConfig());
    }
}
