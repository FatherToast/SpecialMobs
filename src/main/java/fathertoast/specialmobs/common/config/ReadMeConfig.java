package fathertoast.specialmobs.common.config;

import fathertoast.crust.api.config.common.AbstractConfigFile;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.field.BooleanField;
import net.minecraft.ChatFormatting;

public class ReadMeConfig extends AbstractConfigFile {
    
    public final BooleanField secretMode;
    
    /** Builds the config spec that should be used for this config. */
    protected ReadMeConfig( ConfigManager manager ) {
        super( manager, "README", "This file contains helpful information about how to use the config files in this mod." );
        SPEC.newLine( 2 );
        SPEC.comment( ChatFormatting.AQUA + "Terminology used in Special Mobs configs:" );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Mob " + ChatFormatting.GRAY +
                "- An entity that is 'alive', short for \"Mobile\" or MobEntity." );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Family " + ChatFormatting.GRAY +
                "- The group of mobs based on (but not including) a particular vanilla mob; e.g., Creepers." );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Species " + ChatFormatting.GRAY +
                "- A specific type of mob within a family; e.g., Fire Creepers or vanilla-replacement Creepers." );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Vanilla Replacement " + ChatFormatting.GRAY +
                "- The one species within a family that is intended to be a replica of the base vanilla mob." );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Special Variant " + ChatFormatting.GRAY +
                "- Any species that is not the family's vanilla replacement. Includes species that are replicas of 'vanilla special variants'; i.e. Husks and Strays." );
        
        SPEC.newLine();
        SPEC.comment( ChatFormatting.YELLOW + " * Mob Replacer " + ChatFormatting.GRAY +
                "- The tool that watches vanilla mob spawns and cancels them to spawn this mod's entities." );
        
        SPEC.newLine( 8 );
        secretMode = SPEC.define( new BooleanField( "secret_mode", false, (String[]) null ) );
    }
}