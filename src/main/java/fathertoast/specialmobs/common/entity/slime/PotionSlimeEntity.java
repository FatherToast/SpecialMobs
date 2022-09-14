package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.species.PotionSlimeSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

import static fathertoast.specialmobs.common.util.References.NBT_TYPE_STRING;
import static fathertoast.specialmobs.common.util.References.TAG_AMMO;

@SpecialMob
public class PotionSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<PotionSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xA94725 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithOverlay()
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 2.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new PotionSlimeSpeciesConfig( species );
    }
    
    /** @return This entity's species config. */
    public PotionSlimeSpeciesConfig getConfig() { return (PotionSlimeSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Potion Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon",
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.SLOWNESS ),
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.HARMING ),
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.POISON ),
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.WEAKNESS )
        );
        loot.addRareDrop( "rare",
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.STRONG_SLOWNESS ),
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.STRONG_HARMING ),
                PotionUtils.setPotion( new ItemStack( Items.SPLASH_POTION ), Potions.STRONG_POISON ),
                PotionUtils.setPotion( new ItemStack( Items.LINGERING_POTION ), Potions.HARMING )
        );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<PotionSlimeEntity> getVariantFactory() { return PotionSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends PotionSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The parameter for potion color. */
    private static final DataParameter<Integer> COLOR = EntityDataManager.defineId( PotionSlimeEntity.class, DataSerializers.INT );
    /** The color of an empty potion. */
    private static final int EMPTY_POTION_COLOR = 0x385DC6;
    
    private Effect potionEffect;
    
    public PotionSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) {
        super( entityType, world );
        setRandomPotionFill();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( COLOR, EMPTY_POTION_COLOR );
    }
    
    /** Sets the potion fill of this slime to a random effect based on config settings. */
    private void setRandomPotionFill() {
        final Set<Effect> allowedPotions = getConfig().POTION.allowedPotions.get().getEntries();
        if( allowedPotions.size() > 0 ) {
            final ArrayList<Effect> effects = new ArrayList<>( allowedPotions );
            if( !Config.MAIN.GENERAL.enableNausea.get() ) effects.remove( Effects.CONFUSION );
            
            if( effects.size() > 0 ) {
                setPotionFill( effects.get( random.nextInt( effects.size() ) ) );
                return;
            }
        }
        setPotionFill( null );
    }
    
    /** Sets the potion fill of this slime. */
    private void setPotionFill( @Nullable Effect effect ) {
        potionEffect = effect;
        entityData.set( COLOR, effect == null ? EMPTY_POTION_COLOR : effect.getColor() );
    }
    
    /** @return The color of the potion contained in this slime. */
    public int getPotionColor() { return entityData.get( COLOR ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( potionEffect == null ) target.clearFire();
        else MobHelper.applyEffect( target, potionEffect );
    }
    
    /**
     * Called when the slime spawns particles on landing, see onUpdate.
     * Return true to prevent the spawning of the default particles.
     */
    @Override
    protected boolean spawnCustomParticles() {
        final int color = getPotionColor();
        final float r = References.getRed( color );
        final float g = References.getGreen( color );
        final float b = References.getBlue( color );
        
        final int size = getSize();
        for( int i = 0; i < size * 8; i++ ) {
            final float angle = random.nextFloat() * 2.0F * (float) Math.PI;
            final float distance = (random.nextFloat() * 0.25F + 0.25F) * size;
            level.addParticle( getParticleType(),
                    getX() + MathHelper.sin( angle ) * distance,
                    getY(),
                    getZ() + MathHelper.cos( angle ) * distance,
                    r, g, b );
        }
        return true;
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return ParticleTypes.ENTITY_EFFECT; }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) {
        // Immune to debuffs
        return effect.getEffect().getCategory() != EffectType.HARMFUL && super.canBeAffected( effect );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putString( TAG_AMMO, SpecialMobs.toString( ForgeRegistries.POTIONS.getKey( potionEffect ) ) );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( TAG_AMMO, NBT_TYPE_STRING ) )
            setPotionFill( ForgeRegistries.POTIONS.getValue( new ResourceLocation( saveTag.getString( TAG_AMMO ) ) ) );
    }
}