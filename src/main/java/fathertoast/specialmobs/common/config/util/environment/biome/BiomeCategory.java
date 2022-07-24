package fathertoast.specialmobs.common.config.util.environment.biome;

import net.minecraft.world.biome.Biome;

/**
 * Used to wrap the vanilla enum Biome.Category so that it can be safely used in configs.
 * The declared names should match the string passed into vanilla enums' constructors so that both enums serialize identically.
 */
public enum BiomeCategory {
    NONE( Biome.Category.NONE ),
    TAIGA( Biome.Category.TAIGA ),
    EXTREME_HILLS( Biome.Category.EXTREME_HILLS ),
    JUNGLE( Biome.Category.JUNGLE ),
    MESA( Biome.Category.MESA ),
    PLAINS( Biome.Category.PLAINS ),
    SAVANNA( Biome.Category.SAVANNA ),
    ICY( Biome.Category.ICY ),
    THE_END( Biome.Category.THEEND ),
    BEACH( Biome.Category.BEACH ),
    FOREST( Biome.Category.FOREST ),
    OCEAN( Biome.Category.OCEAN ),
    DESERT( Biome.Category.DESERT ),
    RIVER( Biome.Category.RIVER ),
    SWAMP( Biome.Category.SWAMP ),
    MUSHROOM( Biome.Category.MUSHROOM ),
    NETHER( Biome.Category.NETHER );
    
    public final Biome.Category BASE;
    
    BiomeCategory( Biome.Category base ) { BASE = base; }
}