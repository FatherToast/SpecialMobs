package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.UUID;

public final class References {
    
    /** The speed boost to apply to baby mobs. */
    public static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier( UUID.fromString( "B9766B59-9566-4402-BC1F-2EE2A276D836" ),
            "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE );
    
    
    //--------------- BESTIARY REFLECTION ----------------
    
    public static final String ENTITY_PACKAGE = "fathertoast." + SpecialMobs.MOD_ID + ".common.entity.";
    public static final String VANILLA_REPLACEMENT_FORMAT = "%s._Special%sEntity";
    public static final String SPECIAL_VARIANT_FORMAT = "%s.%sEntity";
    
    public static final String TEXTURE_FORMAT = SpecialMobs.TEXTURE_PATH + "%s/%s%s.png";
    public static final String TEXTURE_BASE_SUFFIX = "";
    public static final String TEXTURE_EYES_SUFFIX = "_eyes";
    public static final String TEXTURE_OVERLAY_SUFFIX = "_overlay";
    public static final String TEXTURE_SHOOTING_SUFFIX = "_shooting";
    //public static final String TEXTURE_SHOOTING_EYES_SUFFIX = "_shooting_eyes";
    
    
    //--------------- BIT FLAGS ----------------
    
    public static final int SET_BLOCK_FLAGS = 0b00000011;
    
    
    //--------------- EVENT CODES ----------------
    
    // Entity events; used in World#broadcastEntityEvent(Entity, byte) then executed by Entity#handleEntityEvent(byte)
    public static final byte EVENT_TELEPORT_TRAIL_PARTICLES = 46;
    
    // Level events; used in World#levelEvent(PlayerEntity, int, BlockPos, int) then executed by WorldRenderer#levelEvent(PlayerEntity, int, BlockPos, int)
    public static final int EVENT_GHAST_WARN = 1015;
    public static final int EVENT_GHAST_SHOOT = 1016;
    public static final int EVENT_BLAZE_SHOOT = 1018;
    
    
    //--------------- NBT STUFF ----------------
    
    public static final int NBT_TYPE_NUMERICAL = 99;
    public static final int NBT_TYPE_STRING = StringNBT.valueOf( "" ).getId(); // 8
    public static final int NBT_TYPE_LIST = new ListNBT().getId(); // 9
    public static final int NBT_TYPE_COMPOUND = new CompoundNBT().getId(); // 10
    
    public static final String TAG_FORGE_DATA = "ForgeData";
    
    public static final String TAG_INIT = "SpecialMobsInit";
    
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
    public static final String TAG_SUPERCHARGED = "Supercharged";
    public static final String TAG_DRY_EXPLODE = "CannotExplodeWhileWet";
    public static final String TAG_WHILE_BURNING_EXPLODE = "ExplodesWhileBurning";
    public static final String TAG_WHEN_SHOT_EXPLODE = "ExplodesWhenShot";
    
    // Witches
    public static final String TAG_SHEATHED_ITEM = "SheathedItem";
    public static final String TAG_POTION_USE_TIME = "PotionUseTimer";
    
    // Blazes
    public static final String TAG_BURST_COUNT = "FireballBurstCount";
    public static final String TAG_BURST_DELAY = "FireballBurstDelay";
    
    // Baby-able families - Skeletons, Wither Skeletons
    public static final String TAG_IS_BABY = "IsBaby";
    
    // Shifting mobs - Corporeal Shift Ghast
    public static final String TAG_IS_SHIFTED = "IsShifted";
    public static final String TAG_SHIFT_TIME = "ShiftTime";
    
    // Spawner mobs
    public static final String TAG_BABIES = "Babies"; // Mother (Cave) Spider, Wilds Witch, Queen Ghast, Wildfire Blaze
    public static final String TAG_EXTRA_BABIES = "ExtraBabies"; // Splitting Creeper, Mother (Cave) Spider, Wilds Witch
    public static final String TAG_SUMMONS = "Summons"; // Undead Witch, Wilds Witch, Queen Ghast, Wildfire Blaze
    
    // Growing mobs
    public static final String TAG_GROWTH_LEVEL = "GrowthLevel"; // Hungry Spider, Conflagration Blaze
    public static final String TAG_HEALTH_STACKS = "HealthStacks"; // Hungry Spider
    
    // Misc.
    public static final String TAG_FUSE_TIME = "FuseTime"; // Blackberry Slime, Volatile Magma Cube
    public static final String TAG_AMMO = "Ammo"; // Web (Cave) Spider, Mad Scientist Zombie
    public static final String TAG_IS_FAKE = "IsFake"; // Mirage Enderman
    public static final String TAG_EXPLOSION_POWER = "ExplosionPower"; // Hellfire Blaze
    
    
    //--------------- INTERNATIONALIZATION ----------------
    
    /** This method provides helper tags to make linking translations up easier, and also enforces the correct array length. */
    public static String[] translations( String key, String en, String es, String pt, String fr, String it, String de, String pir ) {
        // Note that this must match up EXACTLY to the TranslationKey enum in SMLanguageProvider
        String[] translation = { key, en, es, pt, fr, it, de, pir };
        
        // Fix the encoding to allow us to use accented characters in the translation string literals
        // Note: If a translation uses any non-ASCII characters, make sure they are all in this matrix! (case-sensitive)
        final String[][] utf8ToUnicode = {
                { "à", "\u00E0" }, { "á", "\u00E1" }, { "ã", "\u00E3" }, { "ä", "\u00E4" },
                { "ç", "\u00E7" },
                { "è", "\u00E8" }, { "é", "\u00E9" }, { "ê", "\u00EA" },
                { "í", "\u00ED" },
                { "ó", "\u00F3" }, { "õ", "\u00F5" }, { "ö", "\u00F6" },
                { "ù", "\u00F9" }, { "û", "\u00FB" }, { "ü", "\u00FC" },
                { "œ", "\u0153" }
        };
        for( int i = 1; i < translation.length; i++ ) {
            for( String[] fix : utf8ToUnicode )
                translation[i] = translation[i].replace( fix[0], fix[1] ); // Note: This is kinda dumb, but it works so idc
        }
        return translation;
    }
}