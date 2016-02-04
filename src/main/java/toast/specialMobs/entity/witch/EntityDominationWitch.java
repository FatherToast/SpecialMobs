package toast.specialMobs.entity.witch;

import java.util.Collection;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityDominationWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/domination.png")
    };

    /// Ticks before this entity can use its pull ability.
    public int pullDelay;

    public EntityDominationWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityDominationWitch.TEXTURES);
    }

    /// Override to set the attack AI to use.
    @Override
	protected void initTypeAI() {
        this.setMeleeAI();
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.8);
    }

    public static boolean canAffectMind(EntityLivingBase entity) {
    	if (entity.isPotionActive(Potion.weakness))
    		return true;
    	ItemStack helmet = entity.getEquipmentInSlot(4);
    	return helmet == null || helmet.stackTagCompound == null || !helmet.stackTagCompound.getBoolean("SM|MindProtect");
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (!this.worldObj.isRemote && this.isEntityAlive() && this.pullDelay-- <= 0 && this.getAttackTarget() != null && this.rand.nextInt(20) == 0) {
            EntityLivingBase target = this.getAttackTarget();
            double distanceSq = target.getDistanceSqToEntity(this);

        	if (distanceSq > 100.0 && distanceSq < 196.0 && EntityDominationWitch.canAffectMind(target) && this.canEntityBeSeen(target)) {
        		this.pullDelay = 80;

	        	double vX = this.posX - target.posX;
	            double vY = this.posY - target.posY;
	            double vZ = this.posZ - target.posZ;
	            double v = Math.sqrt(distanceSq);
	            double mult = 0.26;

	            target.motionX = vX * mult;
	            target.motionY = vY * mult + Math.sqrt(v) * 0.1;
	            target.motionZ = vZ * mult;
	            target.onGround = false;
	            if (target instanceof EntityPlayerMP) {
	                try {
	                    ((EntityPlayerMP) target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
	                }
	                catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }
        	}
        }
        super.onLivingUpdate();
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	if (target instanceof EntityLivingBase) {
    		EntityLivingBase livingTarget = (EntityLivingBase) target;
    		PotionEffect stolenEffect = null;
    		if (EntityDominationWitch.canAffectMind(livingTarget)) {
	    		for (PotionEffect effect : (Collection<PotionEffect>) livingTarget.getActivePotionEffects()) {
	    			try {
	    				if (!Potion.potionTypes[effect.getPotionID()].isBadEffect()) {
	    					stolenEffect = effect;
	    					break;
	    				}
	    			}
	    			catch (Exception ex) {
	    				ex.printStackTrace();
	    			}
	    		}
    		}

    		if (stolenEffect != null) {
    			livingTarget.removePotionEffect(stolenEffect.getPotionID());
    			int duration = Math.max(200, stolenEffect.getDuration());
    			duration *= 1.3;
    			this.addPotionEffect(new PotionEffect(stolenEffect.getPotionID(), duration, stolenEffect.getAmplifier()));
    			livingTarget.addPotionEffect(new PotionEffect(Potion.wither.id, 110, Math.max(0, stolenEffect.getAmplifier())));
    		}
    		else {
    			livingTarget.addPotionEffect(new PotionEffect(Potion.wither.id, 70, 0));
    		}
    	}
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
			this.dropItem(Items.experience_bottle, 1);
		}
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
    	int damage;
    	if (superRare > 0) {
			damage = 0;
		}
    	else {
	    	damage = Items.golden_helmet.getMaxDamage();
	    	damage = (int) (0.6F * damage + 0.3F * this.rand.nextInt(damage));
    	}
    	ItemStack drop = new ItemStack(Items.golden_helmet, 1, damage);
        EffectHelper.setItemName(drop, "Helmet of Mind Protection", 0xd);
        EffectHelper.addItemText(drop, "\u00a77Protects against");
        EffectHelper.addItemText(drop, "\u00a77domination witches");
        drop.addEnchantment(Enchantment.unbreaking, this.rand.nextInt(3) + 1);
        drop.stackTagCompound.setBoolean("SM|MindProtect", true);
        this.entityDropItem(drop, 0.0F);
    }
}