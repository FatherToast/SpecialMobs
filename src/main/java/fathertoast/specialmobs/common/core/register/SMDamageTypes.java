package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SMDamageTypes {

	public static final ResourceKey<DamageType> GRAB = register("grab");


	/**
	 * Creates a DamageSource instance with the desired DamageType, using the level's
	 * registry access.
	 */
	public static DamageSource of(Level level, ResourceKey<DamageType> key) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
	}

	/**
	 * Creates a DamageSource instance with the desired DamageType, using the level's
	 * registry access.<br><br>
	 *
	 * @param entity The entity responsible for this damage.
	 */
	public static DamageSource of(Level level, @Nullable Entity entity, ResourceKey<DamageType> key) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), entity);
	}

	private static ResourceKey<DamageType> register(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation( SpecialMobs.MOD_ID, name ));
	}

	public static void bootstrap(BootstapContext<DamageType> context) {
		context.register(SMDamageTypes.GRAB, new DamageType("specialmobs.grab", 0.1F));
	}
}
