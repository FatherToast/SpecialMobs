package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.AbstractConfigFile;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.BooleanField;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.crust.api.config.common.field.EnvironmentListField;
import fathertoast.crust.api.config.common.file.TomlHelper;
import fathertoast.crust.api.config.common.value.EnvironmentEntry;
import fathertoast.crust.api.config.common.value.EnvironmentList;
import fathertoast.specialmobs.common.bestiary.MobFamily;

import java.util.List;

/**
 * This is the base config for mob families. This may be extended to add categories specific to the family, but all
 * options that are used by all families should be defined in this class.
 */
public class FamilyConfig extends AbstractConfigFile {
    protected static final double VARIANT_CHANCE_LOW = 0.2;
    protected static final double VARIANT_CHANCE_HIGH = 0.33;
    
    public static String dir( MobFamily<?, ?> family ) { return ConfigUtil.noSpaces( family.configName ); }
    
    protected static String fileName( MobFamily<?, ?> family ) { return "_family_of_" + ConfigUtil.noSpaces( family.configName ); }
    
    /** @return A basic config supplier with a lower default variant chance. */
    public static FamilyConfig newLessSpecial( ConfigManager manager, MobFamily<?, ?> family ) { return new FamilyConfig( manager, family, VARIANT_CHANCE_LOW ); }
    
    /** @return A basic config supplier with a higher default variant chance. */
    public static FamilyConfig newMoreSpecial( ConfigManager manager, MobFamily<?, ?> family ) { return new FamilyConfig( manager, family, VARIANT_CHANCE_HIGH ); }
    
    /** Category containing all options applicable to mob families as a whole; i.e. not specific to any particular family. */
    public final General GENERAL;
    
    /** Builds the config spec that should be used for this config. */
    public FamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) { this( manager, family, 0.25 ); }
    
    /** Builds the config spec that should be used for this config. */
    public FamilyConfig( ConfigManager manager, MobFamily<?, ?> family, double variantChance ) {
        super( manager, ConfigUtil.noSpaces( family.configName ) + "/" + fileName( family ),
                "This config contains options that apply to the family of " + family.configName + " as a whole;",
                "that is, the vanilla replacement and all special variants." );
        
        GENERAL = new General( this, family, variantChance );
    }
    
    public static class General extends AbstractConfigCategory<FamilyConfig> {
        
        public final BooleanField vanillaReplacement;
        
        public final DoubleField familyRandomScaling;
        
        public final DoubleField.EnvironmentSensitive specialVariantChance;
        
        public final DoubleField.EnvironmentSensitiveWeightedList<MobFamily.Species<?>> specialVariantList;
        
        General( FamilyConfig parent, MobFamily<?, ?> family, double variantChance ) {
            super( parent, "general",
                    "Options standard to all mob families (that is, not specific to any particular mob family)." );
            
            vanillaReplacement = SPEC.define( new BooleanField( "vanilla_replacement", true,
                    "Whether this mob family replaces vanilla " + family.configName + " with its vanilla replacement species.",
                    "The \"master_vanilla_replacement\" setting in the mod's main config must also be true for this to work." ) );
            
            SPEC.newLine();
            
            familyRandomScaling = SPEC.define( new DoubleField( "family_random_scaling", -1.0, DoubleField.Range.SIGNED_PERCENT,
                    "When greater than 0, " + family.configName + " will have a random render scale applied. This is a visual effect only.",
                    "If this is set to a non-negative value, it overrides the value set for \"master_random_scaling\", though",
                    "species configs can override this value." ) );
            
            SPEC.newLine();
            
            specialVariantChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "special_variant_chance.base", variantChance, DoubleField.Range.PERCENT,
                            "The chance for " + family.configName + " to spawn as special variants." ) ),
                    SPEC.define( new EnvironmentListField( "special_variant_chance.exceptions", new EnvironmentList(
                            EnvironmentEntry.builder( SPEC, (float) variantChance * 0.5F ).beforeDays( 5 ).build(), // Also skips first night's full moon
                            EnvironmentEntry.builder( SPEC, (float) variantChance * 2.0F ).atMaxMoonLight().aboveDifficulty( 0.5F ).build(),
                            EnvironmentEntry.builder( SPEC, (float) variantChance * 1.5F ).atMaxMoonLight().build(),
                            EnvironmentEntry.builder( SPEC, (float) variantChance * 1.5F ).aboveDifficulty( 0.5F ).build() )
                            .setRange( DoubleField.Range.PERCENT ),
                            "The chance for " + family.configName + " to spawn as special variants when specific environmental conditions are met." ) )
            );
            
            SPEC.newLine();
            
            List<String> comment;
            final DoubleField[] baseWeights = new DoubleField[family.variants.length];
            final EnvironmentListField[] weightExceptions = new EnvironmentListField[family.variants.length];
            
            comment = TomlHelper.newComment(
                    "The weight of each " + ConfigUtil.camelCaseToLowerSpace( family.name ) + " species to be chosen as the replacement when",
                    family.configName + " spawn as special variants. Higher weight is more common." );
            comment.add( TomlHelper.multiFieldInfo( DoubleField.Range.NON_NEGATIVE ) );
            SPEC.comment( comment );
            for( int i = 0; i < family.variants.length; i++ ) {
                baseWeights[i] = SPEC.define( new DoubleField(
                        "weight." + ConfigUtil.camelCaseToLowerUnderscore( family.variants[i].specialVariantName ) + ".base",
                        family.variants[i].bestiaryInfo.defaultWeight.value, DoubleField.Range.NON_NEGATIVE, (String[]) null ) );
            }
            
            SPEC.newLine();
            
            comment = TomlHelper.newComment(
                    "The weight of each " + ConfigUtil.camelCaseToLowerSpace( family.name ) + " species to be chosen as the replacement when",
                    family.configName + " spawn as special variants when specific environmental conditions are met. Higher weight is more common." );
            comment.add( TomlHelper.multiFieldInfo( DoubleField.Range.NON_NEGATIVE ) );
            SPEC.comment( comment );
            for( int i = 0; i < family.variants.length; i++ ) {
                weightExceptions[i] = SPEC.define( new EnvironmentListField(
                        "weight." + ConfigUtil.camelCaseToLowerUnderscore( family.variants[i].specialVariantName ) + ".exceptions",
                        family.variants[i].bestiaryInfo.theme.getValue(), (String[]) null ) );
            }
            
            specialVariantList = new DoubleField.EnvironmentSensitiveWeightedList<>( family.variants, baseWeights, weightExceptions );
        }
    }
}