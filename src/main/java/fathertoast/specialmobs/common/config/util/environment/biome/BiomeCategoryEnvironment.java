package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class BiomeCategoryEnvironment extends EnumEnvironment<BiomeCategoryEnvironment.Value> {
    public enum Value {
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
        
        Value( Biome.Category vanillaCat ) { BASE = vanillaCat; }
    }
    
    public BiomeCategoryEnvironment( Value value ) { super( value ); }
    
    public BiomeCategoryEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public BiomeCategoryEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME_CATEGORY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) {
        return (pos != null && VALUE.BASE.equals( world.getBiome( pos ).getBiomeCategory() )) != INVERT;
    }
}