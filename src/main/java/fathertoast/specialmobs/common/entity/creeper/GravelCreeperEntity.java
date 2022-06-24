package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class GravelCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x908884 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return _SpecialCreeperEntity.createAttributes();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Gravel Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Blocks.GRAVEL );
        loot.addUncommonDrop( "uncommon", Items.FLINT );
    }
    
    @SpecialMob.Constructor
    public GravelCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setImmuneToBurning( true );
        xpReward += 1;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius / 2.0F ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.Mode explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this, explosionPower, explosionMode, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        if( explosionMode == Explosion.Mode.NONE || level.isClientSide() ) return;
        
        final float throwPower = explosionPower + 4.0F;
        final int count = (int) Math.ceil( throwPower * throwPower * 3.5F );
        for( int i = 0; i < count; i++ ) {
            final FallingBlockEntity gravel = new FallingBlockEntity( level,
                    getX(), getY() + getBbHeight() / 2.0F, getZ(), Blocks.GRAVEL.defaultBlockState() );
            gravel.time = 1; // Prevent the entity from instantly dying
            gravel.dropItem = false;
            gravel.setHurtsEntities( true );
            gravel.fallDistance = 3.0F;
            
            final float speed = (throwPower * 0.7F + random.nextFloat() * throwPower) / 20.0F;
            final float pitch = random.nextFloat() * (float) Math.PI;
            final float yaw = random.nextFloat() * 2.0F * (float) Math.PI;
            gravel.setDeltaMovement(
                    MathHelper.cos( yaw ) * speed,
                    MathHelper.sin( pitch ) * (throwPower + random.nextFloat() * throwPower) / 18.0F,
                    MathHelper.sin( yaw ) * speed );
            level.addFreshEntity( gravel );
        }
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( DamageSource.FALLING_BLOCK.getMsgId().equals( source.getMsgId() ) ||
                DamageSource.ANVIL.getMsgId().equals( source.getMsgId() ) ) {
            return true;
        }
        return super.hurt( source, amount );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "gravel" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}