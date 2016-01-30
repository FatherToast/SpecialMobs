package toast.specialMobs.entity.ghast;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityFaintGhast extends EntityMeleeGhast
{
    /// Useful properties for this class.
    private static final boolean XRAY_GHOSTS = Properties.getBoolean(Properties.STATS, "xray_ghosts");

    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/faint.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/faint_shooting.png")
    };

    public EntityFaintGhast(World world) {
        super(world);
        this.noClip = true;
        this.setSize(1.0F, 1.0F);
        this.getSpecialData().setTextures(EntityFaintGhast.TEXTURES);
        this.getSpecialData().resetRenderScale(0.25F);
        this.getSpecialData().canBreatheInWater = true;
        this.getSpecialData().isImmuneToFalling = true;
        this.getSpecialData().ignorePressurePlates = true;
        this.getSpecialData().ignoreWaterPush = true;
        this.getSpecialData().isImmuneToWebs = true;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// Updates this entity's target.
    @Override
    protected void updateEntityTarget() {
        if (this.targetedEntity != null && this.targetedEntity.isDead) {
            this.targetedEntity = null;
        }
        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.worldObj.getClosestVulnerablePlayerToEntity(this, 100.0);
            if (this.targetedEntity != null) {
                if (EntityFaintGhast.XRAY_GHOSTS || this.canEntityBeSeen(this.targetedEntity)) {
                    this.aggroCooldown = 20;
                }
                else {
                    this.targetedEntity = null;
                }
            }
        }
    }

    /// True if the ghast has an unobstructed line of travel to the waypoint.
    @Override
    public boolean isCourseTraversable(double v) {
        return true;
    }

    /// Get this entity's creature type.
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    /// Checks if this entity is inside of an opaque block
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false; /// Immune to suffocation.
    }

    /// Returns the brightness of this entity for rendering.
    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender(float partialTick) {
        return 15728880;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.slime_ball, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8200);
        EffectHelper.setItemName(itemStack, "Potion of Shadow", 0xf);
        EffectHelper.addPotionEffect(itemStack, Potion.invisibility, 1200, 0);
        EffectHelper.addPotionEffect(itemStack, Potion.blindness, 1200, 0);
        this.entityDropItem(itemStack, 0.0F);
    }
}