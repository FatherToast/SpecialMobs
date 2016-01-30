package toast.specialMobs;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import toast.specialMobs.entity.blaze.EntitySmolderBlaze;
import toast.specialMobs.entity.pigzombie.EntityPlaguePigZombie;
import toast.specialMobs.entity.skeleton.EntityPoisonSkeleton;
import toast.specialMobs.entity.zombie.EntityPlagueZombie;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler
{
    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by World.spawnEntityInWorld().
     * Entity entity = the entity joining the world.
     * World world = the world the entity is joining.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // Special bow effect transfer.
        if (event.entity instanceof EntityArrow && ((EntityArrow)event.entity).shootingEntity instanceof EntityLivingBase) {
            ItemStack heldItem = ((EntityLivingBase)((EntityArrow)event.entity).shootingEntity).getHeldItem();
            if (heldItem != null) {
                int level;
                // Pain
                if (EnchantmentSpecial.painBow != null) {
                    level = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.painBow.effectId, heldItem);
                }
                else {
                    level = 0;
                }
                if (level > 0) {
                    event.entity.getEntityData().setInteger("SM|Pain", level);
                }
                // Plague
                if (EnchantmentSpecial.plagueBow != null) {
                    level = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.plagueBow.effectId, heldItem);
                }
                else {
                    level = ((EntityArrow)event.entity).shootingEntity instanceof EntityPlagueZombie || ((EntityArrow)event.entity).shootingEntity instanceof EntityPlaguePigZombie ? 1 : 0;
                }
                if (level > 0) {
                    event.entity.getEntityData().setInteger("SM|Plague", level);
                }
                // Poison
                if (EnchantmentSpecial.poisonBow != null) {
                    level = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.poisonBow.effectId, heldItem);
                }
                else {
                    level = ((EntityArrow)event.entity).shootingEntity instanceof EntityPoisonSkeleton ? 1 : 0;
                }
                if (level > 0) {
                    event.entity.getEntityData().setInteger("SM|Poison", level);
                }
            }
        }

        // Special mob replacement.
        if (!event.world.isRemote && event.entity instanceof EntityLiving && !Properties.dimensionBlacklist().contains(event.world.provider.dimensionId)) {
            EventHandler.replaceMob(event.world, (EntityLiving)event.entity);
        }
    }

    /**
     * Called by EntityLivingBase.attackEntityFrom().
     * EntityLivingBase entityLiving = the entity being damaged.
     * DamageSource source = the source of the damage.
     * int ammount = the amount of damage to be applied.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.source != null) {
        	if (event.source.getEntity() instanceof EntitySmolderBlaze) {
        		event.entityLiving.addPotionEffect(new PotionEffect(Potion.blindness.id, 80));
        	}

            Entity attacker = null;
            attacker = event.source.getSourceOfDamage();
            int pain = 0;
            int plague = 0;
            int poison = 0;
            if (attacker instanceof EntityArrow) {
                pain = attacker.getEntityData().getInteger("SM|Pain");
                plague = attacker.getEntityData().getInteger("SM|Plague");
                poison = attacker.getEntityData().getInteger("SM|Poison");
            }
            else if (attacker instanceof EntityLivingBase) {
                ItemStack heldItem = ((EntityLivingBase)attacker).getHeldItem();
                // Pain
                if (heldItem != null && EnchantmentSpecial.painSword != null) {
                    pain = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.painSword.effectId, heldItem);
                }
                else {
                    pain = 0;
                }
                // Plague
                if (heldItem != null && EnchantmentSpecial.plagueSword != null) {
                    plague = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.plagueSword.effectId, heldItem);
                }
                else {
                    plague = attacker instanceof EntityPlagueZombie || attacker instanceof EntityPlaguePigZombie ? 1 : 0;
                }
                // Poison
                if (heldItem != null && EnchantmentSpecial.poisonSword != null) {
                    poison = EnchantmentHelper.getEnchantmentLevel(EnchantmentSpecial.poisonSword.effectId, heldItem);
                }
                else {
                    poison = attacker instanceof EntityPoisonSkeleton ? 1 : 0;
                }
            }

            if (pain > 0) {
                event.entityLiving.setHealth(event.entityLiving.getHealth() - pain);
            }
            if (plague > 0) {
                EffectHelper.plagueEffect(event.entityLiving, plague);
            }
            if (poison > 0) {
                EffectHelper.stackEffect(event.entityLiving, Potion.poison, 300, poison - 1, 3);
            }
        }
    }

    // Marks the mob to be replaced with a Special Mobs version, if needed.
    public static void replaceMob(World world, EntityLiving entity) {
        if (MobHelper.canReplace(entity)) {
            TickHandler.markEntityToBeReplaced(entity);
        }
    }
}