package fathertoast.specialmobs.common.config;

import fathertoast.crust.api.config.common.AbstractConfigFile;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.field.BooleanField;

public class ReadMeConfig extends AbstractConfigFile {
    @SuppressWarnings( "SameParameterValue" )
    static void makeReadMe( ConfigManager manager ) { new ReadMeConfig( manager ).SPEC.initialize(); }
    
    /** Builds the config spec that should be used for this config. */
    private ReadMeConfig( ConfigManager manager ) {
        super( manager, "README", "This file contains helpful information about how to use the config files in this mod." );
        SPEC.newLine( 2 );
        SPEC.comment(
                "Terminology used in Special Mobs configs:",
                " * Mob                 - An entity that is 'alive', short for \"Mobile\" or MobEntity.",
                " * Family              - The group of mobs based on (but not including) a particular vanilla mob; e.g., Creepers.",
                " * Species             - A specific type of mob within a family; e.g., Fire Creepers or vanilla-replacement Creepers.",
                " * Vanilla Replacement - The one species within a family that is intended to be a replica of the base vanilla mob.",
                " * Special Variant     - Any species that is not the family's vanilla replacement. Includes species that are " +
                        "replicas of 'vanilla special variants'; i.e. Husks and Strays.",
                " * Mob Replacer        - The tool that watches vanilla mob spawns and cancels them to spawn this mod's entities." );
        SPEC.newLine( 2 );
        SPEC.describeAttributeList();
        SPEC.newLine( 2 );
        SPEC.describeRegistryEntryList();
        SPEC.newLine( 2 );
        SPEC.describeEnvironmentListPart1of2();
        SPEC.newLine();
        SPEC.describeEnvironmentListPart2of2();
        
        SPEC.newLine( 6 );
        SPEC.define( new BooleanField( "secret_mode", false, (String[]) null ) );
    }
}