package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class _SpecialSlimeEntity extends SlimeEntity implements ISpecialMob<_SpecialSlimeEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x51A03E );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes(); // Slimes define their attributes elsewhere based on size
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.SLIME.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public _SpecialSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().initialize();
    }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Override to modify this slime's base attributes by size. */
    protected void modifyVariantAttributes( int size ) { }
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        onVariantAttack( target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundNBT saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundNBT saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialSlimeEntity.class, DataSerializers.FLOAT );
    
    protected int slimeExperienceValue = 0;
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE, 1.0F );
    }
    
    /** Returns true if this slime can deal damage. */
    @Override
    protected boolean isDealsDamage() { return getSize() > 0 && isEffectiveAi(); } // TODO config for allow tiny slimes to hit
    
    /** Sets this slime's size, optionally resetting its health to max. */
    @Override
    protected void setSize( int size, boolean resetHealth ) {
        super.setSize( size, resetHealth );
        
        modifyVariantAttributes( size );
        if( resetHealth ) setHealth( getMaxHealth() );
        xpReward = size + slimeExperienceValue;
    }
    
    /**
     * Alters this slime's base attribute by adding an amount to it.
     * Do NOT use this for move speed, instead use {@link #multAttribute(Attribute, double)}
     */
    protected void addAttribute( Attribute attribute, double amount ) {
        if( attribute != Attributes.MAX_HEALTH && attribute != Attributes.ATTACK_DAMAGE && attribute != Attributes.MOVEMENT_SPEED )
            throw new IllegalArgumentException( "Slime relative attributes are only health, damage, and speed!" );
        
        final ModifiableAttributeInstance attributeInstance = getAttribute( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() + amount );
    }
    
    /**
     * Alters this slime's base attribute by multiplying it by an amount.
     * Mainly use this for move speed, for other attributes use {@link #addAttribute(Attribute, double)}
     */
    protected void multAttribute( Attribute attribute, double amount ) {
        if( attribute != Attributes.MAX_HEALTH && attribute != Attributes.ATTACK_DAMAGE && attribute != Attributes.MOVEMENT_SPEED )
            throw new IllegalArgumentException( "Slime relative attributes are only health, damage, and speed!" );
        
        final ModifiableAttributeInstance attributeInstance = getAttribute( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() * amount );
    }
    
    /** Sets this slime's base attribute. */
    protected void setAttribute( Attribute attribute, double amount ) {
        if( attribute == Attributes.MAX_HEALTH || attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.MOVEMENT_SPEED )
            throw new IllegalArgumentException( "Use slime relative attribute!" );
        
        final ModifiableAttributeInstance attributeInstance = getAttribute( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + attribute.getDescriptionId() );
        attributeInstance.setBaseValue( amount );
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialSlimeEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialSlimeEntity> getSpecialData() { return specialData; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return slimeExperienceValue; } // Slime base xp
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) {
        slimeExperienceValue = xp;
        xpReward = getSize() + xp;
    }
    
    static ResourceLocation GET_TEXTURE_PATH( String type ) {
        return SpecialMobs.resourceLoc( SpecialMobs.TEXTURE_PATH + "slime/" + type + ".png" );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( "textures/entity/slime/slime.png" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        getSpecialData().tick();
    }
    
    /** @return The eye height of this entity when standing. */
    @Override
    protected float getStandingEyeHeight( Pose pose, EntitySize size ) {
        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getBaseScale() * (isBaby() ? 0.53448F : 1.0F);
    }
    
    /** @return Whether this entity is immune to fire damage. */
    @Override
    public boolean fireImmune() { return getSpecialData().isImmuneToFire(); }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !getSpecialData().isImmuneToBurning() ) super.setRemainingFireTicks( ticks );
    }
    
    /** @return True if this entity can be leashed. */
    @Override
    public boolean canBeLeashed( PlayerEntity player ) { return !isLeashed() && getSpecialData().allowLeashing(); }
    
    /** Sets this entity 'stuck' inside a block, such as a cobweb or sweet berry bush. Mod blocks could use this as a speed boost. */
    @Override
    public void makeStuckInBlock( BlockState block, Vector3d speedMulti ) {
        if( getSpecialData().canBeStuckIn( block ) ) super.makeStuckInBlock( block, speedMulti );
    }
    
    /** @return Called when this mob falls. Calculates and applies fall damage. Returns false if canceled. */
    @Override
    public boolean causeFallDamage( float distance, float damageMultiplier ) {
        return super.causeFallDamage( distance, damageMultiplier * getSpecialData().getFallDamageMultiplier() );
    }
    
    /** @return True if this entity should NOT trigger pressure plates or tripwires. */
    @Override
    public boolean isIgnoringBlockTriggers() { return getSpecialData().ignorePressurePlates(); }
    
    /** @return True if this entity can breathe underwater. */
    @Override
    public boolean canBreatheUnderwater() { return getSpecialData().canBreatheInWater(); }
    
    /** @return True if this entity can be pushed by (flowing) fluids. */
    @Override
    public boolean isPushedByFluid() { return !getSpecialData().ignoreWaterPush(); }
    
    /** @return True if this entity takes damage while wet. */
    @Override
    public boolean isSensitiveToWater() { return getSpecialData().isDamagedByWater(); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isSensitiveToWater() && source.getDirectEntity() instanceof SnowballEntity ) {
            amount = Math.max( 3.0F, amount );
        }
        return super.hurt( source, amount );
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
    }
}