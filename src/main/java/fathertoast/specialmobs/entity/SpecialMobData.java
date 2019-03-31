package fathertoast.specialmobs.entity;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.config.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;

@SuppressWarnings( "WeakerAccess" )
public
class SpecialMobData< T extends EntityLiving & ISpecialMob >
{
	private static final String TAG_RENDER_SCALE = "RenderScale";
	private static final String TAG_EXPERIENCE   = "Experience";
	private static final String TAG_REGENERATION = "Regeneration";
	private static final String TAG_TEXTURE      = "Texture";
	private static final String TAG_TEXTURE_EYES = "TextureEyes";
	private static final String TAG_TEXTURE_OVER = "TextureOverlay";
	
	private static final String TAG_ARROW_DAMAGE     = "ArrowDamage";
	private static final String TAG_ARROW_SPREAD     = "ArrowSpread";
	private static final String TAG_ARROW_WALK_SPEED = "ArrowWalkSpeed";
	private static final String TAG_ARROW_REFIRE_MIN = "ArrowRefireMin";
	private static final String TAG_ARROW_REFIRE_MAX = "ArrowRefireMax";
	private static final String TAG_ARROW_RANGE      = "ArrowRange";
	
	private static final String TAG_FALL_MULTI        = "FallMulti";
	private static final String TAG_FIRE_IMMUNE       = "FireImmune";
	private static final String TAG_BURN_IMMUNE       = "BurningImmune";
	private static final String TAG_LEASHABLE         = "Leashable";
	private static final String TAG_WEB_IMMUNE        = "WebImmune";
	private static final String TAG_TRAP_IMMUNE       = "UnderPressure";
	private static final String TAG_DROWN_IMMUNE      = "DrownImmune";
	private static final String TAG_WATER_PUSH_IMMUNE = "WaterPushImmune";
	private static final String TAG_WATER_DAMAGE      = "WaterDamage";
	private static final String TAG_POTION_IMMUNE     = "PotionImmune";
	
	/** The nbt 'type' used to match any kind of NBTPrimitive tag. */
	public static final int NBT_TYPE_PRIMITIVE = 99;
	
	/**
	 * @param tag The mob's base nbt tag
	 *
	 * @return The nbt tag to save special mob data to.
	 */
	public static
	NBTTagCompound getSaveLocation( NBTTagCompound tag )
	{
		final String TAG_FORGE = "ForgeData";
		final String TAG_MOD   = "SpecialMobsData";
		
		if( !tag.hasKey( TAG_FORGE, tag.getId( ) ) ) {
			tag.setTag( TAG_FORGE, new NBTTagCompound( ) );
		}
		tag = tag.getCompoundTag( TAG_FORGE );
		
		if( !tag.hasKey( TAG_MOD, tag.getId( ) ) ) {
			tag.setTag( TAG_MOD, new NBTTagCompound( ) );
		}
		return tag.getCompoundTag( TAG_MOD );
	}
	
	
	/** The entity this data is for. */
	private final T                      theEntity;
	/** Data manager parameter for render scale. */
	private final DataParameter< Float > renderScale;
	
	/** The base collision box scale of this variant's family. */
	private final float familyScale;
	/** The base collision box scale of this variant. */
	private       float baseScale;
	
	/** The base texture of the entity. */
	private ResourceLocation texture;
	/** The glowing eyes texture of the entity. */
	private ResourceLocation textureEyes;
	/** The overlay texture of the entity. */
	private ResourceLocation textureOverlay;
	/** True if the textures need to be sent to the client. */
	private boolean          updateTextures;
	
	/** The damage the entity uses for its ranged attacks, when applicable. */
	public float rangedAttackDamage;
	/** The spread (inaccuracy) of the entity's ranged attacks. */
	public float rangedAttackSpread;
	/** The movement speed multiplier the entity uses during its ranged attack ai. Requires an ai reload to take effect. */
	public float rangedWalkSpeed = 1.0F;
	/** The delay (in ticks) before a new ranged attack can begin after firing. Requires an ai reload to take effect. */
	public int   rangedAttackCooldown;
	/**
	 * The delay (in ticks) between each ranged attack at maximum delay. Requires an ai reload to take effect.
	 * Unused for bow attacks. For fireball attacks, this is the cooldown + charge time.
	 * For all other attacks, this is the cooldown at maximum range (scaled down to the minimum cooldown at point-blank).
	 */
	public int   rangedAttackMaxCooldown;
	/**
	 * The maximum distance (in blocks) the entity can fire ranged attacks from. Requires an ai reload to take effect.
	 * Ranged ai can only be used if this stat is greater than 0. Does not change aggro range.
	 */
	public float rangedAttackMaxRange;
	
	/** The rate this mob regenerates health (ticks per 1 health). Off if 0 or less. */
	private int healTimeMax;
	/** Counter to the next heal, if healTimeMax is greater than 0. */
	private int healTime;
	
	/** Proportion of fall damage taken. */
	private float fallDamageMultiplier = 1.0F;
	
	/** Whether the entity is immune to being set on fire. */
	private boolean isImmuneToBurning;
	/** Whether the entity can be leashed. */
	private boolean allowLeashing;
	/** Whether the entity moves normally through webs. */
	private boolean isImmuneToWebs;
	/** Whether the entity does not trigger pressure plates. */
	private boolean ignorePressurePlates;
	
	/** Whether the entity can breathe under water. */
	private boolean canBreatheInWater;
	/** Whether the entity can ignore pushing from flowing water. */
	private boolean ignoreWaterPush;
	/** Whether the entity is damaged when wet. */
	private boolean isDamagedByWater;
	
	/** List of potions that can not be applied to the entity. */
	private HashSet< String > immuneToPotions = new HashSet<>( );
	
	/**
	 * Constructs a SpecialMobData to store generic data about a mob.
	 *
	 * @param entity The entity to store data for.
	 * @param scale  Data parameter for storing the render scale.
	 */
	public
	SpecialMobData( T entity, DataParameter< Float > scale )
	{
		this( entity, scale, 1.0F );
	}
	
	/**
	 * Constructs a SpecialMobData to store generic data about a mob.
	 *
	 * @param entity          The entity to store data for.
	 * @param scale           Data parameter for storing the render scale.
	 * @param familyBaseScale Base render scale. Typically 1.0F.
	 */
	public
	SpecialMobData( T entity, DataParameter< Float > scale, float familyBaseScale )
	{
		theEntity = entity;
		renderScale = scale;
		
		familyScale = baseScale = familyBaseScale;
		
		setTextures( entity.getDefaultTextures( ) );
		
		theEntity.getDataManager( ).register( renderScale, nextScale( ) );
		
		if( entity.isEntityUndead( ) ) {
			addPotionImmunity( MobEffects.REGENERATION, MobEffects.POISON );
		}
		if( entity instanceof EntitySpider ) {
			setImmuneToWebs( true );
			addPotionImmunity( MobEffects.POISON );
		}
	}
	
	/** Copies all of the data from another mob, optionally copying texture(s). */
	public
	void copyDataFrom( EntityLiving entity, boolean copyTextures )
	{
		if( entity instanceof ISpecialMob ) {
			NBTTagCompound tag = new NBTTagCompound( );
			
			((ISpecialMob) entity).getSpecialData( ).writeToNBT( tag );
			if( !copyTextures ) {
				tag.removeTag( TAG_TEXTURE );
				tag.removeTag( TAG_TEXTURE_EYES );
				tag.removeTag( TAG_TEXTURE_OVER );
			}
			readFromNBT( tag );
		}
	}
	
	/** Called each tick for every living special mob. */
	public
	void onUpdate( )
	{
		// Send texture to client, if needed.
		if( updateTextures && !theEntity.world.isRemote && theEntity.ticksExisted > 1 ) {
			updateTextures = false;
			SpecialMobsMod.network( ).sendToDimension( new MessageTexture( theEntity ), theEntity.dimension );
		}
		
		// Update natural regen, if needed.
		if( healTimeMax > 0 && ++healTime >= healTimeMax ) {
			healTime = 0;
			theEntity.heal( 1.0F );
		}
		
		// Damage if wet and the entity is damaged by water
		if( isDamagedByWater( ) && theEntity.isWet( ) ) {
			theEntity.attackEntityFrom( DamageSource.DROWN, 1.0F );
		}
	}
	
	/**
	 * Alters the entity's base attribute by adding an amount to it.
	 * Do NOT use this for move speed, instead use {@link SpecialMobData#multAttribute(IAttribute, double)}
	 *
	 * @param attribute the attribute to modify
	 * @param amount    the amount to add to the attribute
	 */
	public
	void addAttribute( IAttribute attribute, double amount )
	{
		theEntity.getEntityAttribute( attribute ).setBaseValue( theEntity.getEntityAttribute( attribute ).getBaseValue( ) + amount );
	}
	
	/**
	 * Alters the entity's base attribute by multiplying it by an amount.
	 * Only use this for move speed, for other attributes use {@link SpecialMobData#addAttribute(IAttribute, double)}
	 *
	 * @param attribute the attribute to modify
	 * @param amount    the amount to multiply the attribute by
	 */
	public
	void multAttribute( IAttribute attribute, double amount )
	{
		theEntity.getEntityAttribute( attribute ).setBaseValue( theEntity.getEntityAttribute( attribute ).getBaseValue( ) * amount );
	}
	
	/**
	 * @param potions The effect(s) to grant immunity from.
	 */
	public
	void addPotionImmunity( Potion... potions )
	{
		for( Potion potion : potions ) {
			immuneToPotions.add( potion.getName( ) );
		}
	}
	
	/**
	 * @return Whether this entity has a glowing eyes texture.
	 */
	public
	boolean hasEyesTexture( ) { return textureEyes != null; }
	
	/**
	 * @return Whether this entity has an overlay texture.
	 */
	public
	boolean hasOverlayTexture( ) { return textureOverlay != null; }
	
	/**
	 * @return The base texture for the entity.
	 */
	public
	ResourceLocation getTexture( ) { return texture; }
	
	/**
	 * @return The glowing eyes texture for the entity.
	 */
	public
	ResourceLocation getTextureEyes( ) { return textureEyes; }
	
	/**
	 * @return The overlay texture for the entity.
	 */
	public
	ResourceLocation getTextureOverlay( ) { return textureOverlay; }
	
	/**
	 * @param textures The new texture(s) to set for the entity.
	 */
	private
	void setTextures( ResourceLocation[] textures )
	{
		texture = textures[ 0 ];
		if( textures.length > 1 ) {
			textureEyes = textures[ 1 ];
		}
		if( textures.length > 2 ) {
			textureOverlay = textures[ 2 ];
		}
	}
	
	/**
	 * @param textures The new texture(s) to load for the entity. Called when loaded from a packet.
	 */
	public
	void loadTextures( String[] textures )
	{
		try {
			loadTexture( textures[ 0 ] );
			loadTextureEyes( textures.length > 1 ? textures[ 1 ] : "" );
			loadTextureOverlay( textures.length > 2 ? textures[ 2 ] : "" );
		}
		catch( Exception ex ) {
			SpecialMobsMod.log( ).warn( "Failed to load textures for {}! ({})", theEntity, textures );
		}
	}
	
	private
	void loadTexture( String tex )
	{
		if( tex.isEmpty( ) ) {
			throw new IllegalArgumentException( "Entity must have a base texture" );
		}
		ResourceLocation newTexture = new ResourceLocation( tex );
		if( !newTexture.toString( ).equals( texture.toString( ) ) ) {
			texture = newTexture;
			updateTextures = true;
		}
	}
	
	private
	void loadTextureEyes( String tex )
	{
		if( tex.isEmpty( ) ) {
			if( textureEyes != null ) {
				textureEyes = null;
				updateTextures = true;
			}
		}
		else if( textureEyes == null ) {
			textureEyes = new ResourceLocation( tex );
			updateTextures = true;
		}
		else {
			ResourceLocation newTexture = new ResourceLocation( tex );
			if( !newTexture.toString( ).equals( textureEyes.toString( ) ) ) {
				texture = newTexture;
				updateTextures = true;
			}
		}
	}
	
	private
	void loadTextureOverlay( String tex )
	{
		if( tex.isEmpty( ) ) {
			if( textureOverlay != null ) {
				textureOverlay = null;
				updateTextures = true;
			}
		}
		else if( textureOverlay == null ) {
			textureOverlay = new ResourceLocation( tex );
			updateTextures = true;
		}
		else {
			ResourceLocation newTexture = new ResourceLocation( tex );
			if( !newTexture.toString( ).equals( textureOverlay.toString( ) ) ) {
				texture = newTexture;
				updateTextures = true;
			}
		}
	}
	
	/**
	 * @return The render scale for the entity.
	 */
	public
	float getRenderScale( ) { return theEntity.getDataManager( ).get( renderScale ); }
	
	public
	void setRenderScale( float scale )
	{
		if( !theEntity.world.isRemote ) {
			theEntity.getDataManager( ).set( renderScale, scale );
		}
	}
	
	public
	float getFamilyBaseScale( ) { return familyScale; }
	
	public
	float getBaseScaleForPreScaledValues( ) { return getBaseScale( ) / getFamilyBaseScale( ); }
	
	public
	float getBaseScale( ) { return baseScale; }
	
	public
	void setBaseScale( float newBaseScale )
	{
		baseScale = newBaseScale;
		setRenderScale( nextScale( ) );
	}
	
	private
	float nextScale( )
	{
		if( Config.get( ).GENERAL.RANDOM_SCALING > 0.0F ) {
			return baseScale * (1.0F + (theEntity.getRNG( ).nextFloat( ) - 0.5F) * Config.get( ).GENERAL.RANDOM_SCALING);
		}
		return baseScale;
	}
	
	public
	double getRangedDamage( float distanceFactor )
	{
		int powerEnchant = EnchantmentHelper.getMaxEnchantmentLevel( Enchantments.POWER, theEntity );
		return rangedAttackDamage * (distanceFactor +
		                             theEntity.getRNG( ).nextGaussian( ) * 0.125 +
		                             theEntity.world.getDifficulty( ).getDifficultyId( ) * 0.055F) +
		       (powerEnchant > 0 ? powerEnchant * 0.5 + 0.5 : 0.0);
	}
	
	public
	void setRegenerationTime( int ticks ) { healTimeMax = ticks; }
	
	public
	float getFallDamageMultiplier( ) { return fallDamageMultiplier; }
	
	public
	void setFallDamageMultiplier( float value ) { fallDamageMultiplier = value; }
	
	public
	void setImmuneToFire( boolean value )
	{
		theEntity.setImmuneToFire( value );
		if( value ) {
			theEntity.setPathPriority( PathNodeType.LAVA, PathNodeType.WATER.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.OPEN.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.OPEN.getPriority( ) );
		}
		else {
			theEntity.setPathPriority( PathNodeType.LAVA, PathNodeType.LAVA.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getPriority( ) );
		}
	}
	
	public
	boolean isImmuneToBurning( ) { return isImmuneToBurning; }
	
	public
	void setImmuneToBurning( boolean value )
	{
		isImmuneToBurning = value;
		if( value ) {
			theEntity.setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.OPEN.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.DANGER_FIRE.getPriority( ) );
		}
		else {
			theEntity.setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getPriority( ) );
			theEntity.setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getPriority( ) );
		}
	}
	
	public
	boolean allowLeashing( ) { return allowLeashing; }
	
	public
	void setAllowLeashing( boolean value ) { allowLeashing = value; }
	
	public
	boolean isImmuneToWebs( ) { return isImmuneToWebs; }
	
	public
	void setImmuneToWebs( boolean value ) { isImmuneToWebs = value; }
	
	public
	boolean ignorePressurePlates( ) { return ignorePressurePlates; }
	
	public
	void setIgnorePressurePlates( boolean value ) { ignorePressurePlates = value; }
	
	public
	boolean canBreatheInWater( ) { return canBreatheInWater; }
	
	public
	void setCanBreatheInWater( boolean value ) { canBreatheInWater = value; }
	
	public
	boolean ignoreWaterPush( ) { return ignoreWaterPush; }
	
	public
	void setIgnoreWaterPush( boolean value ) { ignoreWaterPush = value; }
	
	public
	boolean isDamagedByWater( ) { return isDamagedByWater; }
	
	public
	void setDamagedByWater( boolean value )
	{
		isDamagedByWater = value;
		if( value ) {
			theEntity.setPathPriority( PathNodeType.WATER, PathNodeType.LAVA.getPriority( ) );
		}
		else {
			theEntity.setPathPriority( PathNodeType.WATER, PathNodeType.WATER.getPriority( ) );
		}
	}
	
	/**
	 * Tests a potion effect to see if it is applicable to the entity.
	 *
	 * @param effect The potion effect to test.
	 *
	 * @return True if the potion is allowed to be applied.
	 */
	public
	boolean isPotionApplicable( PotionEffect effect )
	{
		return !immuneToPotions.contains( effect.getPotion( ).getName( ) );
	}
	
	/**
	 * Saves this data to NBT.
	 *
	 * @param tag The tag to save to.
	 */
	public
	void writeToNBT( NBTTagCompound tag )
	{
		tag.setFloat( TAG_RENDER_SCALE, getRenderScale( ) );
		tag.setInteger( TAG_EXPERIENCE, theEntity.getExperience( ) );
		tag.setByte( TAG_REGENERATION, (byte) healTimeMax );
		
		tag.setString( TAG_TEXTURE, texture.toString( ) );
		tag.setString( TAG_TEXTURE_EYES, textureEyes == null ? "" : textureEyes.toString( ) );
		tag.setString( TAG_TEXTURE_OVER, textureOverlay == null ? "" : textureOverlay.toString( ) );
		
		// Arrow AI
		tag.setFloat( TAG_ARROW_DAMAGE, rangedAttackDamage );
		tag.setFloat( TAG_ARROW_SPREAD, rangedAttackSpread );
		tag.setFloat( TAG_ARROW_WALK_SPEED, rangedWalkSpeed );
		tag.setShort( TAG_ARROW_REFIRE_MIN, (short) rangedAttackCooldown );
		tag.setShort( TAG_ARROW_REFIRE_MAX, (short) rangedAttackMaxCooldown );
		tag.setFloat( TAG_ARROW_RANGE, rangedAttackMaxRange );
		
		// Abilities
		tag.setFloat( TAG_FALL_MULTI, getFallDamageMultiplier( ) );
		tag.setBoolean( TAG_FIRE_IMMUNE, theEntity.isImmuneToFire( ) );
		tag.setBoolean( TAG_BURN_IMMUNE, isImmuneToBurning( ) );
		tag.setBoolean( TAG_LEASHABLE, allowLeashing( ) );
		tag.setBoolean( TAG_WEB_IMMUNE, isImmuneToWebs( ) );
		tag.setBoolean( TAG_TRAP_IMMUNE, ignorePressurePlates( ) );
		tag.setBoolean( TAG_DROWN_IMMUNE, canBreatheInWater( ) );
		tag.setBoolean( TAG_WATER_PUSH_IMMUNE, ignoreWaterPush( ) );
		tag.setBoolean( TAG_WATER_DAMAGE, isDamagedByWater( ) );
		
		NBTTagList potionsTag = new NBTTagList( );
		for( String potionName : immuneToPotions ) {
			potionsTag.appendTag( new NBTTagString( potionName ) );
		}
		tag.setTag( TAG_POTION_IMMUNE, potionsTag );
	}
	
	/**
	 * Loads this data from NBT.
	 *
	 * @param tag The tag to load from.
	 */
	public
	void readFromNBT( NBTTagCompound tag )
	{
		if( tag.hasKey( TAG_RENDER_SCALE, NBT_TYPE_PRIMITIVE ) ) {
			setRenderScale( tag.getFloat( TAG_RENDER_SCALE ) );
		}
		if( tag.hasKey( TAG_EXPERIENCE, NBT_TYPE_PRIMITIVE ) ) {
			theEntity.setExperience( tag.getInteger( TAG_EXPERIENCE ) );
		}
		if( tag.hasKey( TAG_REGENERATION, NBT_TYPE_PRIMITIVE ) ) {
			healTimeMax = tag.getByte( TAG_REGENERATION );
		}
		
		try {
			int nbtTypeString = new NBTTagString( ).getId( );
			if( tag.hasKey( TAG_TEXTURE, nbtTypeString ) ) {
				loadTexture( tag.getString( TAG_TEXTURE ) );
			}
			if( tag.hasKey( TAG_TEXTURE_EYES, nbtTypeString ) ) {
				loadTextureEyes( tag.getString( TAG_TEXTURE_EYES ) );
			}
			if( tag.hasKey( TAG_TEXTURE_OVER, nbtTypeString ) ) {
				loadTextureOverlay( tag.getString( TAG_TEXTURE_OVER ) );
			}
		}
		catch( Exception ex ) {
			SpecialMobsMod.log( ).warn( "Failed to load textures from NBT! " + theEntity.toString( ) );
		}
		
		
		// Arrow AI
		if( tag.hasKey( TAG_ARROW_DAMAGE, NBT_TYPE_PRIMITIVE ) ) {
			rangedAttackDamage = tag.getFloat( TAG_ARROW_DAMAGE );
		}
		if( tag.hasKey( TAG_ARROW_SPREAD, NBT_TYPE_PRIMITIVE ) ) {
			rangedAttackSpread = tag.getFloat( TAG_ARROW_SPREAD );
		}
		if( tag.hasKey( TAG_ARROW_WALK_SPEED, NBT_TYPE_PRIMITIVE ) ) {
			rangedWalkSpeed = tag.getFloat( TAG_ARROW_WALK_SPEED );
		}
		if( tag.hasKey( TAG_ARROW_REFIRE_MIN, NBT_TYPE_PRIMITIVE ) ) {
			rangedAttackCooldown = tag.getShort( TAG_ARROW_REFIRE_MIN );
		}
		if( tag.hasKey( TAG_ARROW_REFIRE_MAX, NBT_TYPE_PRIMITIVE ) ) {
			rangedAttackMaxCooldown = tag.getShort( TAG_ARROW_REFIRE_MAX );
		}
		if( tag.hasKey( TAG_ARROW_RANGE, NBT_TYPE_PRIMITIVE ) ) {
			rangedAttackMaxRange = tag.getFloat( TAG_ARROW_RANGE );
		}
		
		// Abilities
		if( tag.hasKey( TAG_FALL_MULTI, NBT_TYPE_PRIMITIVE ) ) {
			setFallDamageMultiplier( tag.getFloat( TAG_FALL_MULTI ) );
		}
		if( tag.hasKey( TAG_FIRE_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			theEntity.setImmuneToFire( tag.getBoolean( TAG_FIRE_IMMUNE ) );
		}
		if( tag.hasKey( TAG_BURN_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			setImmuneToBurning( tag.getBoolean( TAG_BURN_IMMUNE ) );
		}
		if( tag.hasKey( TAG_LEASHABLE, NBT_TYPE_PRIMITIVE ) ) {
			setAllowLeashing( tag.getBoolean( TAG_LEASHABLE ) );
		}
		if( tag.hasKey( TAG_WEB_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			setImmuneToWebs( tag.getBoolean( TAG_WEB_IMMUNE ) );
		}
		if( tag.hasKey( TAG_TRAP_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			setIgnorePressurePlates( tag.getBoolean( TAG_TRAP_IMMUNE ) );
		}
		if( tag.hasKey( TAG_DROWN_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			setCanBreatheInWater( tag.getBoolean( TAG_DROWN_IMMUNE ) );
		}
		if( tag.hasKey( TAG_WATER_PUSH_IMMUNE, NBT_TYPE_PRIMITIVE ) ) {
			setIgnoreWaterPush( tag.getBoolean( TAG_WATER_PUSH_IMMUNE ) );
		}
		if( tag.hasKey( TAG_WATER_DAMAGE, NBT_TYPE_PRIMITIVE ) ) {
			setDamagedByWater( tag.getBoolean( TAG_WATER_DAMAGE ) );
		}
		if( tag.hasKey( TAG_POTION_IMMUNE, new NBTTagList( ).getId( ) ) ) {
			NBTTagList potionsTag = tag.getTagList( TAG_POTION_IMMUNE, new NBTTagString( ).getId( ) );
			immuneToPotions.clear( );
			for( int i = 0; i < potionsTag.tagCount( ); i++ ) {
				immuneToPotions.add( potionsTag.getStringTagAt( i ) );
			}
		}
	}
}
