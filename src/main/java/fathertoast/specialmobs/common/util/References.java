package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

public final class References {
    
    //--------------- BESTIARY REFLECTION ----------------
    
    public static final String ENTITY_PACKAGE = "fathertoast." + SpecialMobs.MOD_ID + ".common.entity.";
    public static final String VANILLA_REPLACEMENT_FORMAT = "%s_Special%sEntity";
    public static final String SPECIAL_VARIANT_FORMAT = "%s%sEntity";
    
    
    //--------------- AI STUFF ----------------
    
    public static final int AI_BIT_NONE = 0b00000000;
    public static final int AI_BIT_MOVE = 0b00000001;
    public static final int AI_BIT_FACE = 0b00000010;
    public static final int AI_BIT_SWIM = 0b00000100;
    
    
    //--------------- NBT STUFF ----------------
    
    public static final int NBT_TYPE_NUMERICAL = 99;
    public static final int NBT_TYPE_STRING = StringNBT.valueOf( "" ).getId(); // 8
    public static final int NBT_TYPE_LIST = new ListNBT().getId(); // 9
    public static final int NBT_TYPE_COMPOUND = new CompoundNBT().getId(); // 10
    
    public static final String TAG_FORGE_DATA = "ForgeData";
    
    // Special mob data
    public static final String TAG_SPECIAL_MOB_DATA = "SpecialMobsData";
    public static final String TAG_RENDER_SCALE = "RenderScale";
    public static final String TAG_EXPERIENCE = "Experience";
    public static final String TAG_REGENERATION = "Regeneration";
    public static final String TAG_TEXTURE = "Texture";
    public static final String TAG_TEXTURE_EYES = "TextureEyes";
    public static final String TAG_TEXTURE_OVER = "TextureOverlay";
    public static final String TAG_ARROW_DAMAGE = "ArrowDamage";
    public static final String TAG_ARROW_SPREAD = "ArrowSpread";
    public static final String TAG_ARROW_WALK_SPEED = "ArrowWalkSpeed";
    public static final String TAG_ARROW_REFIRE_MIN = "ArrowRefireMin";
    public static final String TAG_ARROW_REFIRE_MAX = "ArrowRefireMax";
    public static final String TAG_ARROW_RANGE = "ArrowRange";
    public static final String TAG_FALL_MULTI = "FallMulti";
    public static final String TAG_FIRE_IMMUNE = "FireImmune";
    public static final String TAG_BURN_IMMUNE = "BurningImmune";
    public static final String TAG_LEASHABLE = "Leashable";
    public static final String TAG_TRAP_IMMUNE = "UnderPressure";
    public static final String TAG_DROWN_IMMUNE = "DrownImmune";
    public static final String TAG_WATER_PUSH_IMMUNE = "WaterPushImmune";
    public static final String TAG_WATER_DAMAGE = "WaterDamage";
    public static final String TAG_STICKY_IMMUNE = "StickyImmune";
    public static final String TAG_POTION_IMMUNE = "PotionImmune";
    
    // Creepers
    public static final String TAG_DRY_EXPLODE = "CannotExplodeWhileWet";
    public static final String TAG_WHEN_BURNING_EXPLODE = "ExplodesWhileBurning";
    public static final String TAG_WHEN_SHOT_EXPLODE = "ExplodesWhenShot";
}