package toast.specialMobs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import toast.specialMobs.entity.EntitySpecialFishHook;
import toast.specialMobs.entity.EntitySpecialSpitball;
import toast.specialMobs.entity.creeper.EntityEnderCreeper;
import toast.specialMobs.entity.creeper.EntityFireCreeper;
import toast.specialMobs.entity.ghast.EntityMiniGhast;
import toast.specialMobs.network.MessageExplosion;
import toast.specialMobs.network.MessageTexture;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * This is the mod class. Everything the mod does is initialized by this class.
 */
@Mod(modid = _SpecialMobs.MODID, name = "Special Mobs", version = _SpecialMobs.VERSION)
public class _SpecialMobs
{
    /* TO DO *\
    >> CURRENT
     * Magma cubes
     * Witches?
     * Water guardians (1.8)
     * Shulker (1.9)
     * Check into biome mod compatibility for added spawns
    >> Mobs
     * New hostile mobs
    \* ** ** */

    /** This mod's id. */
    public static final String MODID = "SpecialMobs";
    /** This mod's version. */
    public static final String VERSION = "3.2.2";

    /** If true, this mod starts up in debug mode. */
    public static final boolean debug = false;
    /** The sided proxy. This points to a "common" proxy if and only if we are on a dedicated
     * server. Otherwise, it points to a client proxy. */
    @SidedProxy(clientSide = "toast.specialMobs.client.ClientProxy", serverSide = "toast.specialMobs.CommonProxy")
    public static CommonProxy proxy;
    /** The mod's random number generator. */
    public static final Random random = new Random();
    /** The network channel for this mod. */
    public static SimpleNetworkWrapper CHANNEL;

    /** The path to the textures folder. */
    public static final String TEXTURE_PATH = _SpecialMobs.MODID + ":textures/models/";

    /** Monster "species" array. */
    public static final String[] MONSTER_KEY = {
        "Blaze", "CaveSpider", "Creeper", "Enderman", "Ghast", /*"LavaSlime",*/ "PigZombie", "Silverfish", "Skeleton", "Slime", "Spider", "Zombie"
    };
    /** Monster "sub-species" array. First dimension is the MONSTER_KEY[]. */
    public static final String[][] MONSTER_TYPES = {
        { // Blaze
        	"Cinder", "Conflagration", "Ember", "Hellfire", "Inferno", "Jolt", "Smolder", "Wildfire"
        },
        { // CaveSpider
            "Baby", "Flying", "Mother", "Tough", "Web", "Witch"
        },
        { // Creeper
            "Armor", "Dark", "Death", "Dirt", "Doom", "Drowning", "Ender", "Fire", "Gravel", "Gravity", "Jumping", "Lightning", "Mini", "Splitting"
        },
        { // Enderman
            "Blinding", "Cursed", "Icy", "Lightning", "Mini", "Mirage", "Thief"
        },
        { // Ghast
            "Baby", "Faint", "Fighter", "King", "Mini", "Queen", "Unholy"
        },
        { // PigZombie
            "Brutish", "Fishing", "Giant", "Hungry", "Plague", "Vampire"
        },
        { // Silverfish
            "Blinding", "Fishing", "Flying", "Poison", "Tough"
        },
        { // Skeleton
            "Brutish", "Fire", "Gatling", "Giant", "Ninja", "Poison", "Sniper", "Spitfire", "Thief"
        },
        { // Slime
        	"Blackberry", "Blueberry", "Caramel", "Grape", "Lemon", "Strawberry", "Watermelon"
        },
        { // Spider
            "Baby", "Desert", "Flying", "Ghost", "Giant", "Hungry", "Mother", "Pale", "Poison", "Small", "Tough", "Web", "Witch"
        },
        { // Zombie
            "Brutish", "Fire", "Fishing", "Giant", "Hungry", "Plague"
        }
    };

    /** Monster "species" color array. Used for spawn eggs. */
    public static final int[] MONSTER_KEY_COLORS = {
        /* Eventual
        "Blaze", "CaveSpider", "Creeper", "Enderman", "Ghast", "LavaSlime" (0x340000, 0xfcfc00), "PigZombie", "Silverfish", "Skeleton", "Slime", "Spider", "Zombie" */
        /*
        "Blaze",  "CaveSpider", "Creeper", "Enderman", "Ghast",  "PigZombie", "Silverfish", "Skeleton", "Slime",  "Spider", "Zombie" */
        0xf6b201, 0x0c424e,     0x0da70b,  0x161616,   0xf9f9f9, 0xea9393,    0x6e6e6e,     0xc1c1c1,   0x51a03e, 0x342d27, 0x00afaf
    };
    /** Monster "sub-species" color array. Used for spawn eggs. First dimension is the MONSTER_KEY[]. */
    public static final int[][] MONSTER_TYPE_COLORS = {
        { /* Blaze (0xfff87e)
            "Cinder", "Conflagration", "Ember",  "Hellfire", "Inferno", "Jolt",   "Smolder", "Wildfire" */
            0xffc0cb, 0xfff87e,        0x000000, 0xdddddd,   0xf14f00,  0x499cae, 0x000000,  0xf4ee32
        },
        { /* CaveSpider (0xa80e0e)
            "Baby",   "Flying", "Mother", "Tough",  "Web",    "Witch" */
            0xffc0cb, 0x6388b2, 0xb300b3, 0x8ea80e, 0xe7e7e7, 0xdd0e0e
        },
        { /* Creeper (0x000000)
            "Armor",  "Dark",   "Death",  "Dirt",   "Doom",   "Drowning", "Ender",  "Fire",   "Gravel", "Gravity", "Jumping", "Lightning", "Mini",   "Splitting" */
            0xc39536, 0xf9ff3a, 0xcd0000, 0x78553b, 0x494949, 0x2d41f4,   0xcc00fa, 0xe13916, 0x908884, 0x220022,  0x7d6097,  0x499cae,    0xffc0cb, 0x5f9d22
        },
        { /* Enderman (0x000000)
            "Blinding", "Cursed", "Icy",    "Lightning", "Mini",   "Mirage", "Thief" */
            0xffffff,   0xab1d1d, 0x72959c, 0x4bb4b5,    0xffc0cb, 0xc2bc84, 0x04fa00
        },
        { /* Ghast (0xbcbcbc)
            "Baby",   "Faint",  "Fighter", "King",   "Mini",   "Queen",  "Unholy" */
            0xffc0cb, 0x82c873, 0x7a1300,  0xe8c51a, 0xbcbcbc, 0xce0aff, 0x7ac754
        },
        { /* PigZombie (0x4c7129)
            "Brutish", "Fishing", "Giant",  "Hungry", "Plague", "Vampire" */
            0xfff87e,  0x2d41f4,  0x4c7129, 0xab1518, 0x8aa838, 0x000000
        },
        { /* Silverfish (0x303030)
            "Blinding", "Fishing", "Flying", "Poison", "Tough" */
            0x000000,   0x2d41f4,  0x6388b2, 0x779c68, 0xdd0e0e
        },
        { /* Skeleton (0x494949)
            "Brutish", "Fire",   "Gatling", "Giant",  "Ninja",  "Poison", "Sniper", "Spitfire", "Thief" */
            0xfff87e,  0xdc1a00, 0xffff0b,  0x494949, 0x333366, 0x779c68, 0x486720, 0xdc1a00,   0x000000
        },
        { /* Slime (0x7ebf6e)
            "Blackberry", "Blueberry", "Caramel", "Grape",  "Lemon",  "Strawberry", "Watermelon" */
            0x331133,     0x766bbc,    0x9d733f,  0xb333b3, 0xe6e861, 0xbe696b,     0xdf7679
        },
        { /* Spider (0xa80e0e)
            "Baby",   "Desert", "Flying", "Ghost",  "Giant",  "Hungry", "Mother", "Pale",   "Poison", "Small",  "Tough",  "Web",    "Witch" */
            0xffc0cb, 0xe6ddac, 0x6388b2, 0x82c873, 0xa80e0e, 0x799c65, 0xb300b3, 0xded4c6, 0x0c424e, 0xa80e0e, 0x8ea80e, 0xe7e7e7, 0xdd0e0e
        },
        { /* Zombie (0x799c65)
            "Brutish", "Fire",   "Fishing", "Giant",  "Hungry", "Plague" */
            0xfff87e,  0xdc1a00, 0x2d41f4,  0x799c65, 0xab1518, 0x8aa838
        }
    };

    /** Registers the entities in this mod and adds mob spawns. */
    private void registerMobs() {
        int id = 0; // Mod-specific mob id

        // Initialize everything needed to make new spawn eggs
        boolean makeSpawnEggs = Properties.getBoolean(Properties.GENERAL, "spawn_eggs");
        Method eggIdClaimer = null;
        int eggId;
        if (makeSpawnEggs) {
            try {
                eggIdClaimer = EntityRegistry.class.getDeclaredMethod("validateAndClaimId", int.class);
                eggIdClaimer.setAccessible(true);
            }
            catch (Exception ex) {
                _SpecialMobs.console("Error claiming spawn egg ID! Spawn eggs will probably be overwritten.");
                ex.printStackTrace();
            }
        }

        // Advanced Genetics compatibility
        Method advGeneticsAddAbility = null;
        String[][] advGeneticsAbilities = null;
        try {
            Class regHelper = Class.forName("com.advGenetics.API.RegistrationHelper");
            _SpecialMobs.console("Detected Advanced Genetics API, attempting to register mobs to it...");
            try {
                advGeneticsAddAbility = regHelper.getDeclaredMethod("addEntityToAbility", String.class, Class.class);
            }
            catch (Exception ex) {
                _SpecialMobs.console("Error finding Advanced Genetics registry!");
                ex.printStackTrace();
            }
            if (advGeneticsAddAbility != null) {
                advGeneticsAbilities = new String[][] {
                        // Blaze
                        { "fireballs" },
                        // CaveSpider
                        { "climb", "poison" },
                        // Creeper
                        { "selfexplode" },
                        // Enderman
                        { "deathenderchest", "teleport" },
                        // Ghast
                        { "fireballsexplode" },
                        // LavaSlime
                        //{ "slimy", "lavaswim" },
                        // PigZombie
                        { "lavaswim" },
                        // Silverfish
                        {  },
                        // Skeleton
                        { "infinity" },
                        // Slime
                        { "slimy" },
                        // Spider
                        { "climb" },
                        // Witch
                        //{ "potionthrower" },
                        // Zombie
                        { "resistance" }
                };
                for (String ability : advGeneticsAbilities[_SpecialMobs.monsterKey("Enderman")]) {
                    try {
                        advGeneticsAddAbility.invoke(null, ability, EntityEnderCreeper.class);
                    }
                    catch (Exception ex) {
                        // Do nothing
                    }
                }
            }
        }
        catch (Exception ex) {
            // Do nothing, Advanced Genetics not found
        }

        // Register main mobs
        String name;
        Class entityClass;
        for (int i = 0; i < _SpecialMobs.MONSTER_KEY.length; i++) {
            // Register vanilla replacement
            try {
                entityClass = Class.forName("toast.specialMobs.entity." + _SpecialMobs.MONSTER_KEY[i].toLowerCase() + ".Entity_Special" + _SpecialMobs.MONSTER_KEY[i]);
                EntityRegistry.registerModEntity(entityClass, "Special" + _SpecialMobs.MONSTER_KEY[i], id++, this, 80, 3, true);

                // Advanced Genetics compatibility
                if (advGeneticsAddAbility != null && advGeneticsAbilities != null) {
                    for (String ability : advGeneticsAbilities[i]) {
                        try {
                            advGeneticsAddAbility.invoke(null, ability, entityClass);
                        }
                        catch (Exception ex) {
                            // Do nothing
                        }
                    }
                }
            }
            catch (ClassNotFoundException ex) {
                _SpecialMobs.debugException("@" + _SpecialMobs.MONSTER_KEY[i] + ": class not found!");
            }

            // Register special variants
            for (int j = 0; j < _SpecialMobs.MONSTER_TYPES[i].length; j++) {
                name = _SpecialMobs.MONSTER_TYPES[i][j] + _SpecialMobs.MONSTER_KEY[i];
                try {
                    entityClass = Class.forName("toast.specialMobs.entity." + _SpecialMobs.MONSTER_KEY[i].toLowerCase() + ".Entity" + name);
                    EntityRegistry.registerModEntity(entityClass, name, id++, this, 80, 3, true);

                    // Add spawn egg by taking a global mob id
                    if (makeSpawnEggs) {
                        eggId = EntityRegistry.findGlobalUniqueEntityId();
                        try {
                            if (eggIdClaimer != null) {
                                eggId = ((Integer)eggIdClaimer.invoke(EntityRegistry.instance(), Integer.valueOf(eggId))).intValue();
                            }
                        }
                        catch (Exception ex) {
                            // Do nothing
                        }
                        EntityList.IDtoClassMapping.put(Integer.valueOf(eggId), entityClass);
                        EntityList.entityEggs.put(Integer.valueOf(eggId), new EntityEggInfo(eggId, _SpecialMobs.MONSTER_KEY_COLORS[i], _SpecialMobs.MONSTER_TYPE_COLORS[i][j]));
                    }

                    // Advanced Genetics compatibility
                    if (advGeneticsAddAbility != null && advGeneticsAbilities != null) {
                        for (String ability : advGeneticsAbilities[i]) {
                            try {
                                advGeneticsAddAbility.invoke(null, ability, entityClass);
                            }
                            catch (Exception ex) {
                                // Do nothing
                            }
                        }
                    }
                }
                catch (ClassNotFoundException ex) {
                    _SpecialMobs.debugException("@" + name + ": class not found!");
                }
            }
        }

        // Register other entities (like projectiles)
        EntityRegistry.registerModEntity(EntitySpecialFishHook.class, "SMFishHook", id++, this, 64, 5, true);
        EntityRegistry.registerModEntity(EntitySpecialSpitball.class, "SMSpitball", id++, this, 64, 5, true);

        //EntityRegistry.registerModEntity(EntitySpecialFireball.class, "SMFireball", id++, this, 64, 10, true);

        // Register extra mob spawns
        int spawnWeight;
        spawnWeight = Properties.getInt(Properties.SPAWNING, "end_ender_creeper");
        if (spawnWeight > 0) {
            EntityRegistry.addSpawn(EntityEnderCreeper.class, spawnWeight, 1, 1, EnumCreatureType.monster, BiomeGenBase.sky);
        }
        spawnWeight = Properties.getInt(Properties.SPAWNING, "nether_fire_creeper");
        if (spawnWeight > 0) {
            EntityRegistry.addSpawn(EntityFireCreeper.class, spawnWeight, 4, 4, EnumCreatureType.monster, BiomeGenBase.hell);
        }
        spawnWeight = Properties.getInt(Properties.SPAWNING, "overworld_ghast_mount");
        if (spawnWeight > 0) {
            EntityRegistry.addSpawn(EntityMiniGhast.class, spawnWeight, 1, 1, EnumCreatureType.monster, BiomeGenBase.ocean, BiomeGenBase.frozenOcean, BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge, BiomeGenBase.forest, BiomeGenBase.forestHills, BiomeGenBase.taiga, BiomeGenBase.taigaHills, BiomeGenBase.swampland, BiomeGenBase.river, BiomeGenBase.frozenRiver, BiomeGenBase.icePlains, BiomeGenBase.iceMountains, BiomeGenBase.beach, BiomeGenBase.jungle, BiomeGenBase.jungleHills);
        }
    }

    /**
     * Called before initialization. Loads the properties/configurations and registers blocks/items.
     *
     * @param event The event being fired.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        _SpecialMobs.debugConsole("Loading in debug mode!");
        Properties.init(new Configuration(event.getSuggestedConfigurationFile()));

        int id = 0;
        _SpecialMobs.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("SM|EX");
        if (event.getSide() == Side.CLIENT) {
            _SpecialMobs.CHANNEL.registerMessage(MessageExplosion.Handler.class, MessageExplosion.class, id++, Side.CLIENT);
            _SpecialMobs.CHANNEL.registerMessage(MessageTexture.Handler.class, MessageTexture.class, id++, Side.CLIENT);
        }
    }

    /**
     * Called during initialization. Registers entities, mob spawns, and renderers.
     *
     * @param event The event being fired.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        EnchantmentSpecial.init();
        new EventHandler();
        new TickHandler();
        this.registerMobs();
        _SpecialMobs.proxy.registerRenderers();
    }

    /**
     * Called after initialization. Used to check for dependencies.
     *
     * @param event The event being fired.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Currently unused
    }

    /**
     * Returns the index of the mob in MONSTER_KEY[].
     *
     * @param key The monster key to search for.
     * @return The index of the key in MONSTER_KEY[], negative if not found.
     */
    public static int monsterKey(String key) {
        if (key == null)
            return -1;
        if (key.startsWith(_SpecialMobs.MODID + ".Special")) {
            key = key.substring(_SpecialMobs.MODID.length() + 8);
        }
        return Arrays.binarySearch(_SpecialMobs.MONSTER_KEY, key);
    }
    /**
     * Returns the index[2] of the mob in MONSTER_TYPES[][].
     *
     * @param key The monster key to search for.
     * @param type The monster type to search for.
     * @return A length 2 int[] corresponding to the two indecies in MONSTER_TYPES[][]
     *      for the key and type, respectively.
     */
    public static int[] monsterType(String key, String type) {
        int i = _SpecialMobs.monsterKey(key);
        return new int[] { i, Arrays.binarySearch(_SpecialMobs.MONSTER_TYPES[i], type) };
    }

    /*
    // Separates the mobType from mob, returns String[] { mobType, mob }.
    @Deprecated
	public static String[] parseName(String mobName) {
        char[] chars = mobName.toCharArray();
        boolean[] breaks = new boolean[chars.length];
        String[] parts = new String[chars.length];
        String mobType = "";
        String mob = "";
        for (int i = 0; i < chars.length; i++)
            if (Character.isUpperCase(chars[i]))
                breaks[i] = true;
        int index = -1;
        for (int i = 0; i < chars.length; i++) {
            if (breaks[i] == true)
                index++;
            if (index >= 0) {
                if (parts[index] == null)
                    parts[index] = "";
                parts[index] = parts[index] + Character.toString(chars[i]);
            }
        }
        for (int i = 0; i < chars.length; i++) {
            if (parts[i] == null || parts[i] == "Entity")
                continue;
            if (mobType(parts[i]) >= 0) {
                mobType = parts[i];
                break;
            }
            if (i + 1 < chars.length && mobType(parts[i] + parts[i + 1]) >= 0) {
                mobType = parts[i] + parts[i + 1];
                break;
            }
            mob = mob + parts[i];
        }
		return new String[] { mobType, mob };
	}
     */

    /**
     * Makes the first letter upper case.
     *
     * @param string The string to capitalize.
     * @return A copy of the given string, with the first character capitalized.
     */
    public static String cap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toUpperCase();
        return Character.toString(Character.toUpperCase(string.charAt(0))) + string.substring(1);
    }

    /**
     * Makes the first letter lower case.
     *
     * @param string The string to decapitalize.
     * @return A copy of the given string, with the first character in lower case.
     */
    public static String decap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toLowerCase();
        return Character.toString(Character.toLowerCase(string.charAt(0))) + string.substring(1);
    }

    /**
     * Inserts a space before every capital letter, except the first.
     *
     * @param name The string to format.
     * @return A copy of the string with a space before each capital letter beyond the first,
     *      or "Zombie Pigman" if the given string was "PigZombie".
     */
    public static String localizeName(String name) {
        if (name.equals("PigZombie"))
            return "Zombie Pigman";
        if (name.length() > 1) {
            for (int i = 1; i < name.length(); i++) {
                if (Character.isUpperCase(name.charAt(i)))
                    return name.substring(0, i) + " " + _SpecialMobs.localizeName(name.substring(i));
            }
        }
        return name;
    }

    /**
     * Prints the message to the console with this mod's name tag.
     *
     * @param message The message to print.
     */
    public static void console(String message) {
        System.out.println("[" + _SpecialMobs.MODID + "] " + message);
    }
    /**
     * Prints the message to the console with this mod's name tag if debugging is enabled.
     *
     * @param message The message to print.
     */
    public static void debugConsole(String message) {
        if (_SpecialMobs.debug) {
            System.out.println("[" + _SpecialMobs.MODID + "] (debug) " + message);
        }
    }
    /**
     * Throws a runtime exception with a message and this mod's name tag if debugging is enabled.
     * Otherwise, prints the message to the console tagged as an error.
     *
     * @param message The message to print.
     */
    public static void debugException(String message) {
        if (_SpecialMobs.debug)
            throw new RuntimeException("[" + _SpecialMobs.MODID + "] " + message);
        _SpecialMobs.console("[ERROR] " + message);
    }
}
