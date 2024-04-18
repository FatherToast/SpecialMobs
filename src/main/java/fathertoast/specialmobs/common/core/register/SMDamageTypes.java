package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class SMDamageTypes {
	public static final ResourceKey<DamageType> GRAB = register("grab");

	private static ResourceKey<DamageType> register(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation( SpecialMobs.MOD_ID, name ));
	}

	public static void bootstrap(BootstapContext<DamageType> context) {
		context.register(SMDamageTypes.GRAB, new DamageType("specialmobs.grab", 0.1F));
	}
}
