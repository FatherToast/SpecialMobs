package toast.specialMobs.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.network.MessageTexture;

import java.util.HashSet;

/**
 * Helper class used to store data that is common to all different mod species.
 */
public class SpecialMobData {
    
    // Useful properties for this class.
    /** The maximum multiple of difference between differently sized mobs. */
    private static final float RANDOM_SCALING = (float) Properties.getDouble( Properties.GENERAL, "random_scaling" );
    
    /** The index for the scale variable in the data watcher. */
    private static final byte SCALE = DataWatcherHelper.instance.GENERIC.nextKey();
    
    /** Called to force static fields to be initialized. */
    public static void init() {
        // Do nothing
    }
    
    /**
     * @param tag The mob's base nbt tag
     * @return The nbt tag to save special mob data to.
     */
    public static NBTTagCompound getSaveLocation( NBTTagCompound tag ) {
        if( !tag.hasKey( "ForgeData" ) ) {
            tag.setTag( "ForgeData", new NBTTagCompound() );
        }
        tag = tag.getCompoundTag( "ForgeData" );
        
        if( !tag.hasKey( "SMData" ) ) {
            tag.setTag( "SMData", new NBTTagCompound() );
        }
        return tag.getCompoundTag( "SMData" );
    }
    
    /** The entity this data is for. */
    private final EntityLiving theEntity;
    /** The texture(s) of the entity. */
    private ResourceLocation[] textures;
    /** True if the textures need to be sent to the client. */
    private boolean updateTextures;
    /** The rate this mob regenerates health (ticks per 1 health). Off if 0 or less. */
    private byte healTimeMax;
    /** Counter to the next heal, if healTimeMax is greater than 0. */
    private byte healTime;
    
    /** The damage the entity uses for its arrow attacks. */
    public float arrowDamage = 2.0F;
    /** The spread (inaccuracy) of the entity's arrow attacks. */
    public float arrowSpread = 14.0F;
    /** The movement speed the entity uses for its arrow attack AI. */
    public float arrowMoveSpeed;
    /** The delay (in ticks) between each arrow attack at point-blank range. */
    public short arrowRefireMin;
    /** The delay (in ticks) between each arrow attack at max range. */
    public short arrowRefireMax;
    /** The maximum distance (in blocks) the entity can fire arrows from. Does not change aggro range. */
    public float arrowRange;
    
    /** The entity's innate armor level. */
    public byte armor;
    /** Whether the entity is immune to fire. */
    public boolean isImmuneToFire;
    /** Whether the entity is immune to being set on fire. */
    public boolean isImmuneToBurning;
    /** Whether the entity can be leashed. */
    public boolean allowLeashing;
    /** Whether the entity moves normally through webs. */
    public boolean isImmuneToWebs;
    /** Whether the entity is immune to fall damage. */
    public boolean isImmuneToFalling;
    /** Whether the entity does not trigger pressure plates. */
    public boolean ignorePressurePlates;
    /** Whether the entity can breathe underwater. */
    public boolean canBreatheInWater;
    /** Whether the entity can ignore pushing from flowing water. */
    public boolean ignoreWaterPush;
    /** Whether the entity is damaged when wet. */
    public boolean isDamagedByWater;
    /** List of potions that can not be applied to the entity. */
    public HashSet<Integer> immuneToPotions = new HashSet<>();
    
    /**
     * Constructs a SpecialMobData to store generic data about a mob, initialized with the mob's
     * texture(s).
     *
     * @param entity       The entity to store data for.
     * @param baseTextures The entity's default textures.
     */
    public SpecialMobData( EntityLiving entity, ResourceLocation... baseTextures ) {
        theEntity = entity;
        textures = baseTextures;
        
        theEntity.getDataWatcher().addObject( SpecialMobData.SCALE, SpecialMobData.RANDOM_SCALING > 0.0F ? 1.0F + (entity.getRNG().nextFloat() - 0.5F) * SpecialMobData.RANDOM_SCALING : 1.0F );
        
        if( entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD ) {
            immuneToPotions.add( Potion.regeneration.id );
            immuneToPotions.add( Potion.poison.id );
        }
    }
    
    /** Called each tick for every living special mob. */
    public void onUpdate() {
        // Send texture to client, if needed.
        if( updateTextures && !theEntity.worldObj.isRemote && theEntity.ticksExisted > 1 ) {
            updateTextures = false;
            _SpecialMobs.CHANNEL.sendToDimension( new MessageTexture( theEntity ), theEntity.dimension );
        }
        
        // Update natural regen, if needed.
        if( healTimeMax > 0 && ++healTime >= healTimeMax ) {
            healTime = 0;
            theEntity.heal( 1.0F );
        }
        
        // Damage if wet and the entity is damaged by water
        if( isDamagedByWater && theEntity.isWet() ) {
            theEntity.attackEntityFrom( DamageSource.drown, 1 );
        }
    }
    
    /**
     * Handles the onSpawnWithEgg method to initialize the entity.
     *
     * @param data  Entity group data. Generally unused.
     * @param proxy A fake, vanilla version of the entity this data is saved to.
     * @return The new data object, if any. Otherwise, returns the original.
     */
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data, EntityLiving proxy ) {
        NBTTagCompound entityData = new NBTTagCompound();
        theEntity.writeToNBT( entityData );
        proxy.readFromNBT( entityData );
        data = proxy.onSpawnWithEgg( data );
        
        entityData = new NBTTagCompound();
        proxy.writeToNBT( entityData );
        theEntity.readFromNBT( entityData );
        ((ISpecialMob) theEntity).adjustEntityAttributes();
        
        if( proxy.riddenByEntity != null ) {
            proxy.riddenByEntity.mountEntity( theEntity );
        }
        theEntity.mountEntity( proxy.ridingEntity );
        return data;
    }
    
    /**
     * Alters the entity's base attribute by adding an amount to it.
     * Do NOT use this for move speed, instead use {@link SpecialMobData#multAttribute(IAttribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to add to the attribute
     */
    public void addAttribute( IAttribute attribute, double amount ) {
        theEntity.getEntityAttribute( attribute ).setBaseValue( theEntity.getEntityAttribute( attribute ).getAttributeValue() + amount );
    }
    
    /**
     * Alters the entity's base attribute by multiplying it by an amount.
     * Only use this for move speed, for other attributes use {@link SpecialMobData#addAttribute(IAttribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to multiply the attribute by
     */
    public void multAttribute( IAttribute attribute, double amount ) {
        theEntity.getEntityAttribute( attribute ).setBaseValue( theEntity.getEntityAttribute( attribute ).getAttributeValue() * amount );
    }
    
    /**
     * @param time The new heal time to set for the entity.
     */
    public void setHealTime( int time ) {
        healTimeMax = (byte) time;
    }
    
    /**
     * @return The number of textures for the entity.
     */
    public int getTextureCount() {
        return textures.length;
    }
    
    /**
     * @return The texture for the entity.
     */
    public ResourceLocation getTexture() {
        return textures[0];
    }
    
    /**
     * @param index The index of the texture to get.
     * @return The texture for the entity with a specific index.
     */
    public ResourceLocation getTexture( int index ) {
        return textures[index];
    }
    
    /**
     * @return All the textures for the entity.
     */
    public ResourceLocation[] getTextures() {
        return textures;
    }
    
    /**
     * @param tex The new texture(s) to set for the entity.
     */
    public void setTextures( ResourceLocation... tex ) {
        textures = tex;
    }
    
    /**
     * @param tex The new texture(s) to load for the entity. Called when loaded from NBT or packet.
     */
    public void loadTextures( String... tex ) {
        try {
            ResourceLocation[] newTextures = new ResourceLocation[textures.length];
            for( int i = newTextures.length; i-- > 0; ) {
                if( !textures[i].toString().equals( tex[i] ) ) {
                    updateTextures = true;
                    newTextures[i] = new ResourceLocation( tex[i] );
                }
                else {
                    newTextures[i] = textures[i];
                }
            }
            if( updateTextures ) {
                setTextures( newTextures );
            }
        }
        catch( Exception ex ) {
            _SpecialMobs.console( "Failed to load textures!" );
        }
    }
    
    /**
     * @return The render scale for the entity.
     */
    public float getRenderScale() {
        return theEntity.getDataWatcher().getWatchableObjectFloat( SpecialMobData.SCALE );
    }
    
    /**
     * @param scale The new render scale to set for the entity.
     */
    public void setRenderScale( float scale ) {
        if( !theEntity.worldObj.isRemote ) {
            theEntity.getDataWatcher().updateObject( SpecialMobData.SCALE, scale );
        }
    }
    
    /**
     * Rerolls the scale for this mob from a base value.
     *
     * @param scale The base scale.
     */
    public void resetRenderScale( float scale ) {
        if( !theEntity.worldObj.isRemote ) {
            if( SpecialMobData.RANDOM_SCALING > 0.0 )
                scale += (theEntity.getRNG().nextFloat() - 0.5F) * SpecialMobData.RANDOM_SCALING;
            
            theEntity.getDataWatcher().updateObject( SpecialMobData.SCALE, scale );
        }
    }
    
    /**
     * Tests a potion effect to see if it is applicable to the entity.
     *
     * @param effect The potion effect to test.
     * @return True if the potion is allowed to be applied.
     */
    public boolean isPotionApplicable( PotionEffect effect ) {
        return !immuneToPotions.contains( effect.getPotionID() );
    }
    
    /**
     * Saves this data to NBT.
     *
     * @param tag The tag to save to.
     */
    public void writeToNBT( NBTTagCompound tag ) {
        tag.setFloat( "SMScale", getRenderScale() );
        
        tag.setByte( "SMRegen", healTimeMax );
        tag.setByte( "SMRegenTick", healTime );
        
        NBTTagList textureTag = new NBTTagList();
        for( ResourceLocation texture : textures ) {
            textureTag.appendTag( new NBTTagString( texture.toString() ) );
        }
        tag.setTag( "SMTex", textureTag );
        
        // Arrow AI
        tag.setFloat( "SMArrowDamage", arrowDamage );
        tag.setFloat( "SMArrowSpread", arrowSpread );
        tag.setFloat( "SMArrowMoveSpeed", arrowMoveSpeed );
        tag.setShort( "SMArrowRefireMin", arrowRefireMin );
        tag.setShort( "SMArrowRefireMax", arrowRefireMax );
        tag.setFloat( "SMArrowRange", arrowRange );
        
        // Abilities
        tag.setByte( "SMArmor", armor );
        tag.setBoolean( "SMFireImmune", isImmuneToFire );
        tag.setBoolean( "SMBurningImmune", isImmuneToBurning );
        tag.setBoolean( "SMLeash", allowLeashing );
        tag.setBoolean( "SMWebImmune", isImmuneToWebs );
        tag.setBoolean( "SMFallImmune", isImmuneToFalling );
        tag.setBoolean( "SMUnderPressure", ignorePressurePlates );
        tag.setBoolean( "SMWaterBreath", canBreatheInWater );
        tag.setBoolean( "SMWaterPushImmune", ignoreWaterPush );
        tag.setBoolean( "SMWaterDamage", isDamagedByWater );
        
        int[] potionIds = new int[immuneToPotions.size()];
        int i = 0;
        for( int id : immuneToPotions ) {
            potionIds[i++] = id;
        }
        tag.setIntArray( "SMPotionImmune", potionIds );
    }
    
    /**
     * Loads this data from NBT.
     *
     * @param tag The tag to load from.
     */
    public void readFromNBT( NBTTagCompound tag ) {
        if( tag.hasKey( "SMScale" ) ) {
            setRenderScale( tag.getFloat( "SMScale" ) );
        }
        if( tag.hasKey( "SMRegen" ) ) {
            healTimeMax = tag.getByte( "SMRegen" );
            healTime = tag.getByte( "SMRegenTick" );
        }
        if( tag.hasKey( "SMTex" ) ) {
            try {
                NBTTagList textureTag = tag.getTagList( "SMTex", new NBTTagString().getId() );
                String[] newTextures = new String[textures.length];
                for( int i = newTextures.length; i-- > 0; ) {
                    newTextures[i] = textureTag.getStringTagAt( i );
                }
                loadTextures( newTextures );
            }
            catch( Exception ex ) {
                _SpecialMobs.console( "Failed to load textures from NBT! " + theEntity.toString() );
            }
        }
        
        // Arrow AI
        if( tag.hasKey( "SMArrowDamage" ) ) {
            arrowDamage = tag.getFloat( "SMArrowDamage" );
        }
        if( tag.hasKey( "SMArrowSpread" ) ) {
            arrowSpread = tag.getFloat( "SMArrowSpread" );
        }
        if( tag.hasKey( "SMArrowMoveSpeed" ) ) {
            arrowMoveSpeed = tag.getFloat( "SMArrowMoveSpeed" );
        }
        if( tag.hasKey( "SMArrowRefireMin" ) ) {
            arrowRefireMin = tag.getShort( "SMArrowRefireMin" );
        }
        if( tag.hasKey( "SMArrowRefireMax" ) ) {
            arrowRefireMax = tag.getShort( "SMArrowRefireMax" );
        }
        if( tag.hasKey( "SMArrowRange" ) ) {
            arrowRange = tag.getFloat( "SMArrowRange" );
        }
        
        // Abilities
        if( tag.hasKey( "SMArmor" ) ) {
            armor = tag.getByte( "SMArmor" );
        }
        if( tag.hasKey( "SMFireImmune" ) ) {
            isImmuneToFire = tag.getBoolean( "SMFireImmune" );
        }
        if( tag.hasKey( "SMBurningImmune" ) ) {
            isImmuneToBurning = tag.getBoolean( "SMBurningImmune" );
        }
        if( tag.hasKey( "SMLeash" ) ) {
            allowLeashing = tag.getBoolean( "SMLeash" );
        }
        if( tag.hasKey( "SMWebImmune" ) ) {
            isImmuneToWebs = tag.getBoolean( "SMWebImmune" );
        }
        if( tag.hasKey( "SMFallImmune" ) ) {
            isImmuneToFalling = tag.getBoolean( "SMFallImmune" );
        }
        if( tag.hasKey( "SMUnderPressure" ) ) {
            ignorePressurePlates = tag.getBoolean( "SMUnderPressure" );
        }
        if( tag.hasKey( "SMWaterBreath" ) ) {
            canBreatheInWater = tag.getBoolean( "SMWaterBreath" );
        }
        if( tag.hasKey( "SMWaterPushImmune" ) ) {
            ignoreWaterPush = tag.getBoolean( "SMWaterPushImmune" );
        }
        if( tag.hasKey( "SMWaterDamage" ) ) {
            isDamagedByWater = tag.getBoolean( "SMWaterDamage" );
        }
        
        if( tag.hasKey( "SMPotionImmune" ) ) {
            int[] potionIds = tag.getIntArray( "SMPotionImmune" );
            immuneToPotions.clear();
            for( int id : potionIds ) {
                immuneToPotions.add( id );
            }
        }
    }
}