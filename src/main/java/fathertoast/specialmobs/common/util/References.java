package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public final class References {
    
    /** The speed boost to apply to baby mobs. */
    public static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier( UUID.fromString( "B9766B59-9566-4402-BC1F-2EE2A276D836" ),
            "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE );
    
    
    //--------------- COLOR METHODS ----------------
    
    /** @return The red portion of an ARGB color int. Returned value will be in the range 0x00 - 0xFF. */
    public static int getRedBits( int color ) { return (color >> 16) & 0xFF; }
    
    /** @return The green portion of an ARGB color int. Returned value will be in the range 0x00 - 0xFF. */
    public static int getGreenBits( int color ) { return (color >> 8) & 0xFF; }
    
    /** @return The blue portion of an ARGB color int. Returned value will be in the range 0x00 - 0xFF. */
    public static int getBlueBits( int color ) { return color & 0xFF; }
    
    /** @return The alpha (opacity) portion of an ARGB color int. Returned value will be in the range 0x00 - 0xFF. */
    public static int getAlphaBits( int color ) { return (color >> 24) & 0xFF; }
    
    /** @return The red portion of an ARGB color int. Returned value will be in the range 0.0 - 1.0. */
    public static float getRed( int color ) { return (float) getRedBits( color ) / 0xFF; }
    
    /** @return The green portion of an ARGB color int. Returned value will be in the range 0.0 - 1.0. */
    public static float getGreen( int color ) { return (float) getGreenBits( color ) / 0xFF; }
    
    /** @return The blue portion of an ARGB color int. Returned value will be in the range 0.0 - 1.0. */
    public static float getBlue( int color ) { return (float) getBlueBits( color ) / 0xFF; }
    
    /** @return The alpha (opacity) portion of an ARGB color int. Returned value will be in the range 0.0 - 1.0. */
    @SuppressWarnings( "unused" ) // Nobody likes you, Booster
    public static float getAlpha( int color ) { return (float) getAlphaBits( color ) / 0xFF; }
    
    
    //--------------- BESTIARY REFLECTION ----------------
    
    public static final String ENTITY_PACKAGE = "fathertoast." + SpecialMobs.MOD_ID + ".common.entity.";
    public static final String VANILLA_REPLACEMENT_FORMAT = "%s._Special%sEntity";
    public static final String SPECIAL_VARIANT_FORMAT = "%s.%sEntity";
    
    private static final String TEXTURE_FORMAT = "textures/entity/%s/%s%s.png";
    public static final String TEXTURE_BASE_SUFFIX = "";
    public static final String TEXTURE_EYES_SUFFIX = "_eyes";
    public static final String TEXTURE_OVERLAY_SUFFIX = "_overlay";
    public static final String TEXTURE_ANIMATION_SUFFIX = "_anim";
    public static final String TEXTURE_ANIMATION_EYES_SUFFIX = "_anim_eyes";
    
    public static ResourceLocation getEntityTexture( String path, String fileName ) { return getEntityTexture( path, fileName, "" ); }
    
    @SuppressWarnings( "unused" )
    public static ResourceLocation getEntityBaseTexture( String path, String fileName ) {
        return getEntityTexture( path, fileName, TEXTURE_BASE_SUFFIX );
    }
    
    public static ResourceLocation getEntityEyesTexture( String path, String fileName ) {
        return getEntityTexture( path, fileName, TEXTURE_EYES_SUFFIX );
    }
    
    @SuppressWarnings( "unused" )
    public static ResourceLocation getEntityOverlayTexture( String path, String fileName ) {
        return getEntityTexture( path, fileName, TEXTURE_OVERLAY_SUFFIX );
    }
    
    @SuppressWarnings( "unused" )
    public static ResourceLocation getEntityShootingTexture( String path, String fileName ) {
        return getEntityTexture( path, fileName, TEXTURE_ANIMATION_SUFFIX );
    }
    
    public static ResourceLocation getEntityShootingEyesTexture( String path, String fileName ) {
        return getEntityTexture( path, fileName, TEXTURE_ANIMATION_EYES_SUFFIX );
    }
    
    public static ResourceLocation getEntityTexture( String path, String fileName, String suffix ) {
        return SpecialMobs.resourceLoc( String.format( TEXTURE_FORMAT, path, fileName, suffix ) );
    }
    
    
    //--------------- EVENT CODES ----------------
    
    /** Bit flags that can be provided to {@link net.minecraft.world.level.Level#setBlock(BlockPos, BlockState, int)}. */
    @SuppressWarnings( "unused" )
    public static final class SetBlockFlags {
        /** Triggers a block update. */
        public static final int BLOCK_UPDATE = 0b0000_0001;
        /** On servers, sends the change to clients. On clients, triggers a render update. */
        public static final int UPDATE_CLIENT = 0b0000_0010;
        /** Prevents clients from performing a render update. */
        public static final int SKIP_RENDER_UPDATE = 0b0000_0100;
        /** Forces clients to immediately perform the render update on the main thread. Generally used for direct player actions. */
        public static final int PRIORITY_RENDER_UPDATE = 0b0000_1000;
        /** Prevents neighboring blocks from being notified of the change. */
        public static final int SKIP_NEIGHBOR_UPDATE = 0b0001_0000;
        /** Prevents neighbor blocks that are removed by the change from dropping as items. Used by multi-part blocks to prevent dupes. */
        public static final int SKIP_NEIGHBOR_DROPS = 0b0010_0000;
        /** Marks the change as the result of a block moving. Generally prevents connection states from being updated. Used by pistons. */
        public static final int IS_MOVED = 0b0100_0000;
        /** Prevents light levels from being recalculated when set. */
        public static final int SKIP_LIGHT_UPDATE = 0b1000_0000;
        
        /** The set block flags used for most non-world-gen purposes. */
        public static final int DEFAULTS = BLOCK_UPDATE | UPDATE_CLIENT;
    }
    
    /**
     * Entity events. Sent from the server, executed on the client via {@link net.minecraft.world.entity.Entity#handleEntityEvent(byte)}.
     * This only contains event codes for Entity and LivingEntity.
     */
    public enum EntityEvent {
        // Note: if we want to go deeper, it may be wise to make this generic to only allow an appropriate Entity subclass.
        // There's no need to go to MobEntity as its sole event is already nicely abstracted.
        
        HURT_SOUND( 2 ), HURT_SOUND_THORNS( 33 ), HURT_SOUND_DROWN( 36 ),
        HURT_SOUND_BURNING( 37 ), HURT_SOUND_SWEET_BERRY_BUSH( 44 ),
        DEATH_SOUND( 3 ),
        SHIELD_BLOCK_SOUND( 29 ), SHIELD_BREAK_SOUND( 30 ),
        TELEPORT_TRAIL_PARTICLES( 46 ),
        ITEM_BREAK_FX_MAIN_HAND( 47 ), ITEM_BREAK_FX_OFF_HAND( 48 ),
        ITEM_BREAK_FX_HEAD( 49 ), ITEM_BREAK_FX_CHEST( 50 ), ITEM_BREAK_FX_LEGS( 51 ), ITEM_BREAK_FX_FEET( 52 ),
        HONEY_SLIDE_PARTICLES( 53 ) /* This is the only event from Entity. */, HONEY_JUMP_PARTICLES( 54 ),
        SWAP_HAND_ITEMS( 55 );
        
        private final byte ID;
        
        EntityEvent( int id ) { ID = (byte) id; }
        
        /** Sends this event from the given server entity to its client-sided counterpart. */
        public void broadcast( LivingEntity entity ) { entity.level().broadcastEntityEvent( entity, ID ); }
    }
    
    /**
     * Simple level events (ones that do not use extra metadata). Sent from the server, then executed on the client
     * via {@link net.minecraft.client.renderer.LevelRenderer#levelEvent(int, BlockPos, int)}.
     */
    public enum LevelEvent {
        // Note: if metadata events are needed, they will need to be implemented in a separate class
        
        DISPENSER_DISPENSE( 1000 ), DISPENSER_FAIL( 1001 ), DISPENSER_LAUNCH( 1002 ),
        ENDER_EYE_LAUNCH( 1003 ), ENDER_EYE( 2003 ), END_PORTAL_FRAME_FILL( 1503 ),
        FIREWORK_ROCKET_SHOOT( 1004 ),
        IRON_DOOR_OPEN( 1005 ), WOODEN_DOOR_OPEN( 1006 ), WOODEN_TRAPDOOR_OPEN( 1007 ), FENCE_GATE_OPEN( 1008 ),
        IRON_DOOR_CLOSE( 1011 ), WOODEN_DOOR_CLOSE( 1012 ), WOODEN_TRAPDOOR_CLOSE( 1013 ), FENCE_GATE_CLOSE( 1014 ),
        IRON_TRAPDOOR_CLOSE( 1036 ), IRON_TRAPDOOR_OPEN( 1037 ),
        FIRE_EXTINGUISH( 1009 ), LAVA_EXTINGUISH( 1501 ), REDSTONE_TORCH_BURNOUT( 1502 ),
        GHAST_WARN( 1015 ), GHAST_SHOOT( 1016 ),
        ENDER_DRAGON_SHOOT( 1017 ), ENDER_DRAGON_GROWL( 3001 ),
        BLAZE_SHOOT( 1018 ),
        ZOMBIE_ATTACK_WOODEN_DOOR( 1019 ), ZOMBIE_ATTACK_IRON_DOOR( 1020 ), ZOMBIE_BREAK_WOODEN_DOOR( 1021 ),
        ZOMBIE_INFECT( 1026 ), ZOMBIE_VILLAGER_CONVERTED( 1027 ),
        ZOMBIE_CONVERTED_TO_DROWNED( 1040 ), HUSK_CONVERTED_TO_ZOMBIE( 1041 ),
        WITHER_BREAK_BLOCK( 1022 ), WITHER_SHOOT( 1024 ),
        BAT_TAKEOFF( 1025 ),
        ANVIL_DESTROY( 1029 ), ANVIL_USE( 1030 ), ANVIL_LAND( 1031 ),
        BREWING_STAND_BREW( 1035 ), GRINDSTONE_USE( 1042 ), BOOK_PAGE_TURN( 1043 ), SMITHING_TABLE_USE( 1044 ),
        PORTAL_TRAVEL( 1032 ),
        CHORUS_FLOWER_GROW( 1033 ), CHORUS_FLOWER_DEATH( 1034 ),
        PHANTOM_BITE( 1039 ),
        SMOKE_AND_FLAME( 2004 ),
        EXPLOSION_PARTICLE( 2008 ), CLOUD_PARTICLES( 2009 ), EXPLOSION_EMITTER( 3000 );
        
        private final int ID;
        
        LevelEvent( int id ) { ID = id; }
        
        /** Plays this event at the entity's position, if the entity is not silenced. */
        public void play( Entity entity ) {
            if( !entity.isSilent() ) play( entity.level(), entity.blockPosition() );
        }
        
        /** Plays this event at a particular position. */
        public void play( Level level, BlockPos pos ) { play( level, null, pos ); }
        
        /** Plays this event at a particular position, excluding a particular player. */
        public void play( Level level, @Nullable Player player, BlockPos pos ) { level.levelEvent( player, ID, pos, 0 ); }
    }
    
    
    //--------------- NBT STUFF ----------------
    
    public static final int NBT_TYPE_NUMERICAL = 99;
    public static final int NBT_TYPE_STRING = StringTag.valueOf( "" ).getId(); // 8
    public static final int NBT_TYPE_LIST = new ListTag().getId(); // 9
    public static final int NBT_TYPE_COMPOUND = new CompoundTag().getId(); // 10
    
    // Projectiles
    public static final String TAG_KNOCKBACK = "Knockback";
    
    // Forge data
    public static final String TAG_FORGE_DATA = "ForgeData";
    public static final String TAG_INIT = "SpecialMobsInit";
    
    // Special mob data
    public static final String TAG_SPECIAL_MOB_DATA = "SpecialMobsData";
    public static final String TAG_RENDER_SCALE = "RenderScale";
    public static final String TAG_EXPERIENCE = "Experience";
    public static final String TAG_REGENERATION = "Regeneration";
    //    public static final String TAG_TEXTURE = "Texture";
    //    public static final String TAG_TEXTURE_EYES = "TextureEyes";
    //    public static final String TAG_TEXTURE_OVER = "TextureOverlay";
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
    public static final String TAG_RANGED_DAMAGE = "RangedDamage";
    public static final String TAG_RANGED_SPREAD = "RangedSpread";
    public static final String TAG_RANGED_WALK_SPEED = "RangedWalkSpeed";
    public static final String TAG_RANGED_COOLDOWN_MIN = "RangedCDMin";
    public static final String TAG_RANGED_COOLDOWN_MAX = "RangedCDMax";
    public static final String TAG_MAX_RANGE = "MaxRange";
    
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
    public static final String TAG_SUMMONS = "Summons"; // Drowning Creeper, Undead Witch, Wilds Witch, Queen Ghast, Wildfire Blaze
    
    // Growing mobs
    public static final String TAG_GROWTH_LEVEL = "GrowthLevel"; // Hungry Spider, Conflagration Blaze
    public static final String TAG_HEALTH_STACKS = "HealthStacks"; // Hungry Spider
    
    // Misc.
    public static final String TAG_FUSE_TIME = "FuseTime"; // Blackberry Slime, Volatile Magma Cube
    public static final String TAG_AMMO = "Ammo"; // Web (Cave) Spider, Mad Scientist Zombie, Desiccated Silverfish, Armored Blaze
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