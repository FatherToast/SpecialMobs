package fathertoast.specialmobs.common.core.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class SMTags {

	// Damage types
	public static final TagKey<DamageType> IS_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("forge", "is_magic"));


	// Item tags
	public static final TagKey<Item> GARLIC = ItemTags.create(new ResourceLocation("forge", "vegetables/garlic"));
}
