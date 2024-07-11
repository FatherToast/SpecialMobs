package fathertoast.specialmobs.common.bestiary;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.crust.api.config.common.value.*;
import fathertoast.crust.api.config.common.value.environment.biome.BiomeCategory;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * This class serves solely to store data for mob species in an organized way, providing builder methods as applicable.
 * The bulk of bestiary info is default config settings.
 */
public class BestiaryInfo {
    
    public enum DefaultWeight {
        DEFAULT( 60.0 ),
        DISABLED( 0.0 ),
        LOWEST( DEFAULT.value / 8.0 ),
        LOW( DEFAULT.value / 4.0 ),
        HIGH( DEFAULT.value * 2.5 ),
        HIGHEST( DEFAULT.value * 5.0 );
        
        public final double value;
        
        DefaultWeight( double v ) { value = v; }
    }
    
    public enum Theme {
        NONE( (cfgManager) -> new EnvironmentList() ),
        FIRE( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inUltraWarmDimension().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).isHot().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).isWarm().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).isFreezing().build(),
                // Regular frozen ocean is actually freezing, so already covered
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiome( Biomes.WARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).inBiome( Biomes.LUKEWARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).inBiome( Biomes.DEEP_LUKEWARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).inBiome( Biomes.COLD_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).inBiome( Biomes.DEEP_COLD_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inBiome( Biomes.DEEP_FROZEN_OCEAN ).build()
        ) ),
        ICE( (cfgManager) ->  new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inUltraWarmDimension().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).isFreezing().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).isWarm().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).isHot().build(),
                // Regular frozen ocean is actually freezing, so already covered
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiome( Biomes.DEEP_FROZEN_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).inBiome( Biomes.COLD_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).inBiome( Biomes.DEEP_COLD_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).inBiome( Biomes.LUKEWARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).inBiome( Biomes.DEEP_LUKEWARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inBiome( Biomes.WARM_OCEAN ).build()
        ) ),
        DESERT( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inUltraWarmDimension().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inNaturalDimension().inDryBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inWaterBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inHumidBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).isRaining().canSeeSky().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).belowHalfMoonLight().build()
        ) ),
        WATER( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inUltraWarmDimension().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOWEST.value ).inNaturalDimension().inDryBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inWaterBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inHumidBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).isRaining().canSeeSky().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).aboveHalfMoonLight().build()
        ) ),
        FOREST( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiomeCategory( BiomeCategory.TAIGA ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiomeCategory( BiomeCategory.JUNGLE ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiomeCategory( BiomeCategory.FOREST ).build(),
                //EnvironmentEntry.builder( DefaultWeight.HIGHEST.value ).inBiomeTag( BiomeTags.IS_SWAMP ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiome( Biomes.CRIMSON_FOREST ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).atMaxMoonLight().build()
        ) ),
        MOUNTAIN( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inMountainBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).aboveMountainLevel().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).atNoMoonLight().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).belowSeaLevel().build()
        ) ),
        STORM( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).isThundering().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).isRaining().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.LOW.value ).cannotSeeSky().build()
        ) ),
        TROPICAL( (cfgManager) -> new EnvironmentList(
                // All ocean biomes (except regular frozen ocean) have the same temp of 0.5, so we must call out specific biomes
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inBiome( Biomes.WARM_OCEAN ).build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.DISABLED.value ).isFreezing().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.DISABLED.value ).inBiome( Biomes.DEEP_FROZEN_OCEAN ).build()
        ) ),
        FISHING( (cfgManager) -> new EnvironmentList(
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGHEST.value ).inWaterBiome().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).atMaxMoonLight().build(),
                EnvironmentEntry.builder( cfgManager, DefaultWeight.HIGH.value ).isRaining().notInDryBiome().build()
        ) );

        private final Function<ConfigManager, EnvironmentList> func;
        private EnvironmentList value;
        
        Theme( Function<ConfigManager, EnvironmentList> func ) {
            this.func = func;
        }

        public EnvironmentList getValue() {
            if ( value == null ) {
                value = func.apply( ConfigManager.get( SpecialMobs.MOD_ID ) );
                value.setRange( DoubleField.Range.NON_NEGATIVE );
            }
            return value;
        }
    }
    
    /** The spot color for spawn eggs of this species. The base color is determined by the family. */
    public final int eggSpotsColor;
    /** The base render scale of this species. */
    public final float baseScale;
    
    /** The default species weight. */
    public final DefaultWeight defaultWeight;
    /** The species theme; determines default environment weight exceptions. */
    public final Theme theme;
    /** The default species attribute differences compared to their vanilla counterpart. */
    public final AttributeList defaultAttributes;
    
    /** The default texture. */
    public final ResourceLocation texture;
    /** The default glowing eyes texture. Not applicable for ghasts or families normally rendered at max brightness. */
    public final ResourceLocation eyesTexture;
    /** The default overlay texture. */
    public final ResourceLocation overlayTexture;
    /** Generally used as the "shooting" texture for ghasts. */
    public final ResourceLocation animationTexture;

    
    // Special Mob Data defaults
    public final int experience;
    public final int healTime;
    public final double fallDamageMultiplier;
    public final boolean isImmuneToFire;
    public final boolean isImmuneToBurning;
    public final boolean canBreatheInWater;
    public final boolean ignoreWaterPush;
    public final boolean isDamagedByWater;
    public final boolean allowLeashing;
    public final boolean ignorePressurePlates;
    public final RegistryEntryList<Block> immuneToStickyBlocks;
    public final RegistryEntryList<MobEffect> immuneToPotions;
    public final double rangedAttackDamage;
    public final double rangedAttackSpread;
    public final double rangedWalkSpeed;
    public final int rangedAttackCooldown;
    public final int rangedAttackMaxCooldown;
    public final double rangedAttackMaxRange;
    
    
    //--------------- Builder Implementation ----------------
    
    public static BestiaryInfo.Builder of( MobFamily.Species<?> species, EntityType.Builder<?> typeBuilder ) {
        return new Builder( species, typeBuilder );
    }
    
    private BestiaryInfo( int eggColor, float scale, DefaultWeight weight, Theme spawnTheme, List<AttributeEntry> attributes,
                          ResourceLocation tex, ResourceLocation eyeTex, ResourceLocation ovrTex, ResourceLocation animTex,
                          int xp, int regen, double fallDmg, boolean fireImm, boolean burnImm, boolean drownImm, boolean pushImm,
                          boolean waterDmg, boolean leash, boolean plateImm, Object[] blockImm, Object[] effectImm,
                          double raDmg, double raVar, double raSpd, int raCD, int raMCD, double raRng ) {
        eggSpotsColor = eggColor;
        baseScale = scale;
        
        defaultWeight = weight;
        theme = spawnTheme;
        defaultAttributes = new AttributeList( attributes );
        
        texture = tex;
        eyesTexture = eyeTex;
        overlayTexture = ovrTex;
        animationTexture = animTex;
        
        experience = xp;
        healTime = regen;
        fallDamageMultiplier = fallDmg;
        isImmuneToFire = fireImm;
        isImmuneToBurning = burnImm;
        canBreatheInWater = drownImm;
        ignoreWaterPush = pushImm;
        isDamagedByWater = waterDmg;
        allowLeashing = leash;
        ignorePressurePlates = plateImm;
        immuneToStickyBlocks = new LazyRegistryEntryList<>( ForgeRegistries.BLOCKS, false, blockImm );
        immuneToPotions = new LazyRegistryEntryList<>( ForgeRegistries.MOB_EFFECTS, false, effectImm );
        rangedAttackDamage = raDmg;
        rangedAttackSpread = raVar;
        rangedWalkSpeed = raSpd;
        rangedAttackCooldown = raCD;
        rangedAttackMaxCooldown = raMCD;
        rangedAttackMaxRange = raRng;
    }
    
    @SuppressWarnings( { "UnusedReturnValue", "unused" } )
    public static final class Builder {
        
        private final MobFamily.Species<?> owningSpecies;
        private final EntityType.Builder<?> entityTypeBuilder;
        
        // Fields NOT inherited from vanilla replacement
        private boolean colorSet;
        private int eggSpotsColor;
        private DefaultWeight defaultWeight = DefaultWeight.DEFAULT;
        private Theme spawnTheme = Theme.NONE;
        private final List<AttributeEntry> attributes = new ArrayList<>();
        
        // Fields inherited from vanilla replacement (technically also SM Data)
        private float baseScale = 1.0F;
        private ResourceLocation texture;
        private ResourceLocation eyesTexture;
        private ResourceLocation overlayTexture;
        private ResourceLocation animationTexture;
        
        // Special Mob Data fields (also inherited)
        private int experience = -1;
        private int healTime;
        private double fallDamageMultiplier = 1.0;
        private boolean isImmuneToFire;
        private boolean isImmuneToBurning;
        private boolean canBreatheInWater;
        private boolean ignoreWaterPush;
        private boolean isDamagedByWater;
        private boolean allowLeashing;
        private boolean ignorePressurePlates;
        private final ArrayList<Object> immuneToStickyBlocks = new ArrayList<>();
        private final ArrayList<Object> immuneToPotions = new ArrayList<>();
        private double rangedAttackDamage = -1.0;
        private double rangedAttackSpread = -1.0;
        private double rangedWalkSpeed = -1.0;
        private int rangedAttackCooldown = -1;
        private int rangedAttackMaxCooldown = -1;
        private double rangedAttackMaxRange = -1.0;
        
        private Builder( MobFamily.Species<?> species, EntityType.Builder<?> typeBuilder ) {
            owningSpecies = species;
            entityTypeBuilder = typeBuilder;
            
            if( species.specialVariantName == null ) {
                // Fire immunity should be copied from the entity type
                if( species.family.replaceableTypes[0].fireImmune() ) isImmuneToFire = true;
            }
            else {
                // Special variants should copy many of the vanilla replacement's stats
                final BestiaryInfo parent = species.family.vanillaReplacement.bestiaryInfo;
                
                baseScale = parent.baseScale;
                texture = parent.texture;
                eyesTexture = parent.eyesTexture;
                overlayTexture = parent.overlayTexture;
                animationTexture = parent.animationTexture;
                
                experience = parent.experience;
                healTime = parent.healTime;
                fallDamageMultiplier = parent.fallDamageMultiplier;
                isImmuneToFire = parent.isImmuneToFire;
                isImmuneToBurning = parent.isImmuneToBurning;
                canBreatheInWater = parent.canBreatheInWater;
                ignoreWaterPush = parent.ignoreWaterPush;
                isDamagedByWater = parent.isDamagedByWater;
                allowLeashing = parent.allowLeashing;
                ignorePressurePlates = parent.ignorePressurePlates;
                immuneToStickyBlocks.addAll( parent.immuneToStickyBlocks.getEntries() );
                immuneToPotions.addAll( parent.immuneToPotions.getEntries() );
                
                setAllRangedStats( parent.rangedAttackDamage, parent.rangedAttackSpread, parent.rangedWalkSpeed,
                        parent.rangedAttackCooldown, parent.rangedAttackMaxCooldown, parent.rangedAttackMaxRange );
            }
        }
        
        BestiaryInfo build() {
            // Perform a little verification
            if( !colorSet )
                throw new IllegalStateException( "Species " + owningSpecies.name + " has not assigned egg spots color!" );
            if( experience < 0 )
                throw new IllegalStateException( "Family " + owningSpecies.family.name + " has not set the base experience value!" );
            
            return new BestiaryInfo( eggSpotsColor, baseScale, defaultWeight, spawnTheme, attributes, texture, eyesTexture, overlayTexture, animationTexture,
                    experience, healTime, fallDamageMultiplier, isImmuneToFire, isImmuneToBurning, canBreatheInWater, ignoreWaterPush, isDamagedByWater,
                    allowLeashing, ignorePressurePlates, immuneToStickyBlocks.toArray(), immuneToPotions.toArray(),
                    rangedAttackDamage, rangedAttackSpread, rangedWalkSpeed, rangedAttackCooldown, rangedAttackMaxCooldown, rangedAttackMaxRange );
        }
        
        
        //--------------- Bestiary ----------------
        
        /** Sets the species spawn egg spots color. This MUST be called or the build will throw an exception! */
        public Builder color( int eggColor ) {
            eggSpotsColor = eggColor;
            colorSet = true;
            return this;
        }
        
        /** Sets the species default weight. */
        public Builder weight( DefaultWeight weight ) {
            defaultWeight = weight;
            return this;
        }
        
        /** Sets the species default environment weight exceptions by theme. */
        public Builder theme( Theme theme ) {
            spawnTheme = theme;
            return this;
        }
        
        
        //--------------- Size ----------------
        
        /** Sets the family base render size. Throws an exception if called for a special variant. */
        public Builder familySize( float renderScale ) {
            // Do NOT use for special variants; if the render scale is changed, the bounding box should match!
            if( owningSpecies.specialVariantName != null )
                throw new IllegalStateException( "Special variant " + owningSpecies.specialVariantName + " cannot set family render scale!" );
            baseScale = renderScale;
            return this;
        }
        
        /** Sets the species size - both render scale and bounding box dimensions. */
        public Builder size( float renderScale, float width, float height ) {
            baseScale = renderScale;
            entityTypeBuilder.sized( width, height );
            return this;
        }
        
        
        //--------------- Textures (Vanilla) ----------------
        
        /** Sets the species default base, glowing eyes, and overlay textures. */
        public Builder vanillaTexturesAll( String tex, String eyeTex, String ovrTex ) {
            return vanillaBaseTexture( tex ).vanillaEyesTexture( eyeTex ).vanillaOverlayTexture( ovrTex );
        }
        
        /** Sets the species default base and glowing eyes textures. Removes all other textures. */
        public Builder vanillaTextureWithEyes( String tex, String eyeTex ) {
            return vanillaBaseTexture( tex ).vanillaEyesTexture( eyeTex ).noOverlayTexture();
        }
        
        /** Sets the species default base and overlay textures. Removes all other textures. */
        public Builder vanillaTextureWithOverlay( String tex, String ovrTex ) {
            return vanillaBaseTexture( tex ).noEyesTexture().vanillaOverlayTexture( ovrTex );
        }
        
        /** Sets the species default base and animation textures. Removes all other textures. */
        public Builder vanillaTextureWithAnimation( String tex, String aniTex ) {
            return vanillaBaseTexture( tex ).noEyesTexture().vanillaAnimationTexture( aniTex );
        }
        
        /** Sets the species default base texture. Removes all other textures. */
        public Builder vanillaTextureBaseOnly( String tex ) { return vanillaBaseTexture( tex ).noEyesTexture().noOverlayTexture(); }
        
        /** Sets the species default base texture. */
        private Builder vanillaBaseTexture( String tex ) { return baseTexture( new ResourceLocation( tex ) ); }
        
        /** Sets the species default glowing eyes texture. */
        private Builder vanillaEyesTexture( String eyeTex ) { return eyesTexture( new ResourceLocation( eyeTex ) ); }
        
        /** Sets the species default overlay texture. */
        private Builder vanillaOverlayTexture( String ovrTex ) { return overlayTexture( new ResourceLocation( ovrTex ) ); }

        /** Sets the species default animation texture. */
        private Builder vanillaAnimationTexture( String aniTex ) { return animationTexture( new ResourceLocation( aniTex ) ); }
        
        
        //--------------- Textures (Auto-selected) ----------------
        
        /** Sets the species default base, glowing eyes, and overlay textures. */
        public Builder uniqueTexturesAll() { return uniqueBaseTexture().uniqueEyesTexture().uniqueOverlayTexture(); }
        
        /** Sets the species default base and glowing eyes textures. Removes all other textures. */
        public Builder uniqueTextureWithEyes() { return uniqueBaseTexture().uniqueEyesTexture().noOverlayTexture(); }
        
        /** Sets the species default base and overlay textures. Removes all other textures. */
        public Builder uniqueTextureWithOverlay() { return uniqueBaseTexture().noEyesTexture().uniqueOverlayTexture(); }
        
        /** Sets the species default base and animation textures. Removes all other textures. */
        public Builder uniqueTextureWithAnimation() { return uniqueBaseTexture().noEyesTexture().uniqueAnimationTexture(); }
        
        /** Sets the species default base texture. Removes all other textures. */
        public Builder uniqueTextureBaseOnly() { return uniqueBaseTexture().noEyesTexture().noOverlayTexture(); }
        
        /** Sets the species default base texture. */
        // Private because we always want to replace all textures when using a unique base
        private Builder uniqueBaseTexture() { return baseTexture( getBaseTexture() ); }
        
        /** Sets the species default glowing eyes texture. */
        public Builder uniqueEyesTexture() { return eyesTexture( getEyesTexture() ); }
        
        /** Sets the species default overlay texture. */
        public Builder uniqueOverlayTexture() { return overlayTexture( getOverlayTexture() ); }
        
        /** Sets the species default animation texture. */
        public Builder uniqueAnimationTexture() { return animationTexture( getAnimationTexture() ); }
        
        /** @return The expected base texture for this builder. */
        private ResourceLocation getBaseTexture() { return toTexture( References.TEXTURE_BASE_SUFFIX ); }
        
        /** @return The expected eyes texture for this builder. */
        private ResourceLocation getEyesTexture() { return toTexture( References.TEXTURE_EYES_SUFFIX ); }
        
        /** @return The expected overlay texture for this builder. */
        private ResourceLocation getOverlayTexture() { return toTexture( References.TEXTURE_OVERLAY_SUFFIX ); }

        /** @return The expected animation texture for this builder. */
        private ResourceLocation getAnimationTexture() { return toTexture( References.TEXTURE_ANIMATION_SUFFIX ); }
        
        /** @return The given strings converted to a texture resource location. */
        private ResourceLocation toTexture( String suffix ) {
            return References.getEntityTexture( ConfigUtil.camelCaseToLowerUnderscore( owningSpecies.family.name ),
                    ConfigUtil.camelCaseToLowerUnderscore( owningSpecies.specialVariantName ), suffix );
        }
        
        
        //--------------- Textures (Misc/Internal) ----------------
        
        /** Removes the species default glowing eyes texture. */
        public Builder noEyesTexture() { return eyesTexture( null ); }
        
        /** Removes the species default overlay texture. */
        public Builder noOverlayTexture() { return overlayTexture( null ); }
        
        /** Removes the species default animation texture. */
        public Builder noAnimationTexture() { return animationTexture( null ); }
        
        /** Sets the species default base texture. */
        private Builder baseTexture( @Nullable ResourceLocation tex ) {
            texture = tex;
            return this;
        }
        
        /** Sets the species default glowing eyes texture. */
        private Builder eyesTexture( @Nullable ResourceLocation eyeTex ) {
            eyesTexture = eyeTex;
            return this;
        }
        
        /** Sets the species default overlay texture. */
        private Builder overlayTexture( @Nullable ResourceLocation ovrTex ) {
            overlayTexture = ovrTex;
            return this;
        }

        /** Sets the species default animation texture. */
        private Builder animationTexture( @Nullable ResourceLocation aniTex ) {
            animationTexture = aniTex;
            return this;
        }
        
        
        //--------------- Creature Type Templates ----------------
        
        /** Sets the standard species stats implied by being undead. */
        public Builder undead() { return drownImmune().effectImmune( MobEffects.REGENERATION, MobEffects.POISON ); }
        
        /** Sets the standard species stats implied by being a spider. */
        public Builder spider() { return webImmune().effectImmune( MobEffects.POISON ); }
        
        
        //--------------- Special Mob Data ----------------
        
        /** Sets the species experience value. */
        public Builder experience( int xp ) {
            experience = xp;
            return this;
        }
        
        /** Adds a flat amount to the species experience value. */
        public Builder addExperience( int xp ) { return experience( experience + xp ); }
        
        /** Sets the species heal time. */
        public Builder regen( int time ) {
            healTime = time;
            return this;
        }
        
        /** Sets the species as fall damage immune. */
        public Builder fallImmune() { return fallDamage( 0.0 ); }
        
        /** Sets the species fall damage multiplier. */
        public Builder fallDamage( double multiplier ) {
            fallDamageMultiplier = multiplier;
            return this;
        }
        
        /** Sets the species as fire immune. */
        public Builder fireImmune() {
            entityTypeBuilder.fireImmune();
            isImmuneToFire = true;
            return this;
        }
        
        /** Sets the species as burning immune. */
        public Builder burnImmune() {
            isImmuneToBurning = true;
            return this;
        }
        
        /** Sets the species as drowning immune. */
        public Builder drownImmune() {
            canBreatheInWater = true;
            return this;
        }
        
        /** Sets the species as fluid-pushing immune. */
        public Builder fluidPushImmune() {
            ignoreWaterPush = true;
            return this;
        }
        
        /** Sets the species as damaged by water. */
        public Builder waterSensitive() {
            isDamagedByWater = true;
            return this;
        }
        
        /** Sets the species as NOT damaged by water. */
        public Builder waterInsensitive() {
            isDamagedByWater = false;
            return this;
        }
        
        /** Sets the species as leashable (can have a lead attached). */
        public Builder leashable() {
            allowLeashing = true;
            return this;
        }
        
        /** Sets the species as pressure plate immune. */
        public Builder pressurePlateImmune() {
            ignorePressurePlates = true;
            return this;
        }
        
        /** Sets the block hazards (damaging blocks) the species is immune to. */
        public Builder hazardImmune( Block... hazards ) {
            entityTypeBuilder.immuneTo( hazards );
            return this;
        }
        
        /** Sets the species as cobweb immune. */
        public Builder webImmune() { return stickyBlockImmune( Blocks.COBWEB ); }
        
        /**
         * Sets the species as immune to a specific list of sticky blocks.
         * Acceptable argument types are {@code Block}, {@code RegistryObject<Block>}, {@code ResourceLocation}, or {@code String}.
         */
        public Builder stickyBlockImmune( Block... blocks ) {
            immuneToStickyBlocks.addAll( Arrays.asList( blocks ) );
            return this;
        }
        
        /**
         * Sets the species as immune to a specific list of effects.
         * Acceptable argument types are {@code Effect}, {@code RegistryObject<Effect>}, {@code ResourceLocation}, or {@code String}.
         */
        public Builder effectImmune( Object... effects ) {
            immuneToPotions.addAll( Arrays.asList( effects ) );
            return this;
        }
        
        
        //--------------- Ranged Attacks (Special Mob Data) ----------------
        
        /** Sets the species ranged attack stats (for a bow user). */
        public Builder bowAttack( double damage, double spread, double walkSpeed, int cooldown, double range ) {
            return rangedDamage( damage ).rangedSpread( spread ).rangedWalkSpeed( walkSpeed ).rangedCooldown( cooldown ).rangedMaxRange( range );
        }
        
        /** Sets the species ranged attack stats (for a throwing item user). */
        public Builder throwAttack( double spread, double walkSpeed, int cooldown, double range ) {
            return rangedSpread( spread ).rangedWalkSpeed( walkSpeed ).rangedCooldown( cooldown ).rangedMaxRange( range );
        }
        
        /** Sets the species ranged attack stats (for a fireball shooter). */
        public Builder fireballAttack( double spread, int charge, int cooldown, double range ) {
            return rangedSpread( spread ).rangedCooldown( charge ).rangedMaxCooldown( charge + cooldown ).rangedMaxRange( range );
        }
        
        /** Sets the species ranged attack stats (for a spit shooter). */
        public Builder spitAttack( double damage, double spread, int cooldown, int extraCooldown, double range ) {
            return rangedDamage( damage ).rangedSpread( spread )
                    .rangedCooldown( cooldown ).rangedMaxCooldown( cooldown + extraCooldown ).rangedMaxRange( range );
        }
        
        /** Applies multipliers to the species ranged attack stats (for a spit shooter). */
        public Builder spitAttackMultiplied( double damage, double spread, float cooldown, double range ) {
            return multiplyRangedDamage( damage ).multiplyRangedSpread( spread )
                    .multiplyRangedCooldown( cooldown ).multiplyRangedMaxCooldown( cooldown ).multiplyRangedMaxRange( range );
        }
        
        /** Converts the entity's ranged attack stats to those of a fishing rod user. */
        public Builder convertRangedAttackToFishing( double spread, int cooldown, double range ) {
            return setAllRangedStats( -1.0, spread, -1.0, cooldown, -1, range );
        }
        
        /** Converts the entity's ranged attack stats to those of a beam attack user. */
        public Builder convertRangedAttackToBeam( double damage, double turnSpeed, int charge, int duration, double range ) {
            return setAllRangedStats( damage, -1.0, turnSpeed, charge, charge + duration, range );
        }
        
        /** Sets the species as unable to use ranged attacks (for any ranged user). */
        public Builder disableRangedAttack() { return rangedMaxRange( 0.0 ); }
        
        /** Applies a flat modifier to the species ranged attack damage. */
        public Builder addToRangedDamage( double value ) { return rangedDamage( rangedAttackDamage + value ); }
        
        /** Applies a multiplier to the species ranged attack damage. */
        public Builder multiplyRangedDamage( double value ) { return rangedDamage( rangedAttackDamage * value ); }
        
        /** Sets the species ranged attack damage. */
        public Builder rangedDamage( double value ) {
            if( owningSpecies.specialVariantName != null && rangedAttackDamage < 0.0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedAttackDamage = value;
            return this;
        }
        
        /** Applies a multiplier to the species ranged attack spread. */
        public Builder multiplyRangedSpread( double value ) { return rangedSpread( rangedAttackSpread * value ); }
        
        /** Sets the species ranged attack spread. */
        public Builder rangedSpread( double value ) {
            if( owningSpecies.specialVariantName != null && rangedAttackSpread < 0.0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedAttackSpread = value;
            return this;
        }
        
        /** Applies a multiplier to the species ranged attack walk speed. */
        public Builder multiplyRangedWalkSpeed( double value ) { return rangedWalkSpeed( rangedWalkSpeed * value ); }
        
        /** Sets the species ranged attack walk speed. */
        public Builder rangedWalkSpeed( double value ) {
            if( owningSpecies.specialVariantName != null && rangedWalkSpeed < 0.0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedWalkSpeed = value;
            return this;
        }
        
        /** Applies a multiplier to the species ranged attack cooldown. */
        public Builder multiplyRangedCooldown( float value ) { return rangedCooldown( Math.round( rangedAttackCooldown * value ) ); }
        
        /** Sets the species ranged attack cooldown. */
        public Builder rangedCooldown( int value ) {
            if( owningSpecies.specialVariantName != null && rangedAttackCooldown < 0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedAttackCooldown = value;
            return this;
        }
        
        /** Applies a multiplier to the species ranged attack max cooldown. */
        public Builder multiplyRangedMaxCooldown( float value ) { return rangedCooldown( Math.round( rangedAttackMaxCooldown * value ) ); }
        
        /** Sets the species ranged attack max cooldown. */
        public Builder rangedMaxCooldown( int value ) {
            if( owningSpecies.specialVariantName != null && rangedAttackMaxCooldown < 0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedAttackMaxCooldown = value;
            return this;
        }
        
        /** Applies a multiplier to the species ranged attack max range. */
        public Builder multiplyRangedMaxRange( double value ) { return rangedMaxRange( rangedAttackMaxRange * value ); }
        
        /** Sets the species ranged attack max range. */
        public Builder rangedMaxRange( double value ) {
            if( owningSpecies.specialVariantName != null && rangedAttackMaxRange < 0.0 )
                throw new IllegalStateException( "Attempted to add inapplicable ranged attack stat!" );
            rangedAttackMaxRange = value;
            return this;
        }
        
        /** Converts ALL the entity's ranged attack stats. This method allows enabling/disabling stats on variants. */
        private Builder setAllRangedStats( double damage, double spread, double walkSpeed, int cooldown, int maxCooldown, double range ) {
            rangedAttackDamage = damage;
            rangedAttackSpread = spread;
            rangedWalkSpeed = walkSpeed;
            rangedAttackCooldown = cooldown;
            rangedAttackMaxCooldown = maxCooldown;
            rangedAttackMaxRange = range;
            return this;
        }
        
        
        //--------------- Attribute Changes ----------------
        
        /** Adds a flat value to the base attribute. Not applicable for the movement speed attribute, use a multiplier instead. */
        public Builder addToAttribute(Attribute attribute, double value ) {
            if( attribute == Attributes.MOVEMENT_SPEED )
                throw new IllegalArgumentException( "Do not add flat movement speed!" );
            attributes.add( AttributeEntry.add( attribute, value ) );
            return this;
        }
        
        /** Adds a value multiplier to the base attribute. */
        public Builder multiplyAttribute( Attribute attribute, double value ) {
            attributes.add( AttributeEntry.mult( attribute, value ) );
            return this;
        }
    }
}