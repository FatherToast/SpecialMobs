package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class DarkCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xf9ff3a );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return _SpecialCreeperEntity.createAttributes();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Dark Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Blocks.TORCH );
        loot.addRareDrop( "rare", PotionUtils.setPotion( new ItemStack( Items.POTION ), Potions.NIGHT_VISION ) );
    }
    
    @SpecialMob.Constructor
    public DarkCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower() { return super.getVariantExplosionPower() / 2.0F; }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower, Explosion.Mode explosionMode ) {
        final EntityExplosionContext damageCalculator = new EntityExplosionContext( this );
        final Explosion explosion = new Explosion( level, this, null,
                damageCalculator, getX(), getY(), getZ(), explosionPower, false, explosionMode );
        
        if( net.minecraftforge.event.ForgeEventFactory.onExplosionStart( level, explosion ) ) return;
        explosion.explode();
        
        // Add unaffected light sources to the explosion's affected area
        // Note that this does NOT simulate another explosion, instead just directly searches for and targets lights
        //TODO this doesn't seem to work; figure out new method
        final BlockPos center = new BlockPos( getX(), getY(), getZ() );
        final int radius = explosionRadius * 4 * (isPowered() ? 2 : 1);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        for( int y = -radius; y <= radius; y++ ) {
            for( int x = -radius; x <= radius; x++ ) {
                for( int z = -radius; z <= radius; z++ ) {
                    if( x * x + y * y + z * z <= radius * radius ) {
                        pos.set( center.getX() + x, center.getY() + y, center.getZ() + z );
                        final BlockState block = level.getBlockState( pos );
                        
                        // Ignore the block if it is not a light or is already exploded
                        if( block.getLightValue( level, pos ) > 1 && !explosion.getToBlow().contains( pos ) ) {
                            float blockDamage = (float) radius * (0.7F + random.nextFloat() * 0.6F);
                            final FluidState fluid = level.getFluidState( pos );
                            final Optional<Float> optional = damageCalculator.getBlockExplosionResistance(
                                    explosion, level, pos, block, fluid );
                            if( optional.isPresent() ) {
                                blockDamage -= (optional.get() + 0.3F) * 0.3F;
                            }
                            if( blockDamage > 0.0F && damageCalculator.shouldBlockExplode( explosion, level, pos, block, blockDamage ) ) {
                                explosion.getToBlow().add( pos );
                            }
                        }
                    }
                }
            }
        }
        
        explosion.finalizeExplosion( true );
        
        // Move the time forward to next night if powered
        if( isPowered() && level instanceof ServerWorld ) {
            // Days are 24k ticks long; find how far along we are in the current day (0-23,999)
            long time = level.getDayTime();
            final int dayTime = (int) (time % 24_000L);
            
            // We decide that night starts at 13k ticks
            time += 13_000L - dayTime;
            // If we were already past the 13k point today, advance to the next night entirely
            if( dayTime > 13_000 ) time += 24_000L;
            
            ((ServerWorld) level).setDayTime( time );
        }
    }
    
    /**
     * Override to change effects applied by the lingering cloud left by this creeper's explosion.
     * If this list is empty, the lingering cloud is not created.
     */
    @Override
    protected void modifyVariantLingeringCloudEffects( List<EffectInstance> potions ) {
        potions.add( new EffectInstance( Effects.BLINDNESS, 100 ) );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( GET_TEXTURE_PATH( "dark" ) ),
            new ResourceLocation( GET_TEXTURE_PATH( "dark_eyes" ) )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}