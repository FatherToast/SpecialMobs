package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class HellfireBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HellfireBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.7F, 1.99F );
        return new BestiaryInfo( 0xDDDDDD );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialBlazeEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Hellfire",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.GUNPOWDER );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<HellfireBlazeEntity> getVariantFactory() { return HellfireBlazeEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The base explosion strength of this blaze's fireballs. */
    private int explosionPower = 2;
    
    public HellfireBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 1.1F );
        xpReward += 2;
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackSpread *= 0.05F;
        setRangedAI( 1, 0, 60, 100, 40.0F );
    }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( !isSilent() ) level.levelEvent( null, 1018, blockPosition(), 0 );
        
        final float accelVariance = MathHelper.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().rangedAttackSpread;
        final double dX = target.getX() - getX() + getRandom().nextGaussian() * accelVariance;
        final double dY = target.getY( 0.5 ) - getY( 0.5 );
        final double dZ = target.getZ() - getZ() + getRandom().nextGaussian() * accelVariance;
        
        final FireballEntity fireball = new FireballEntity( level, this, dX, dY, dZ );
        fireball.explosionPower = explosionPower;
        fireball.setPos( fireball.getX(), getY( 0.5 ) + 0.5, fireball.getZ() );
        level.addFreshEntity( fireball );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isInvulnerableTo( source ) ) return false;
        
        if( source.getDirectEntity() instanceof FireballEntity && source.getEntity() instanceof PlayerEntity ) {
            super.hurt( source, 1000.0F ); // Die from returned fireballs (like ghasts)
            return true;
        }
        return super.hurt( source, amount );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_EXPLOSION_POWER, (byte) explosionPower );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_EXPLOSION_POWER, References.NBT_TYPE_NUMERICAL ) )
            explosionPower = saveTag.getByte( References.TAG_EXPLOSION_POWER );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "hellfire" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}