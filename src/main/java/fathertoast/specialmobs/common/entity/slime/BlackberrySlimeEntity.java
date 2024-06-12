package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.IExplodingMob;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialSwellGoal;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@SpecialMob
public class BlackberrySlimeEntity extends _SpecialSlimeEntity implements IExplodingMob {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlackberrySlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x331133 )
                .uniqueTextureBaseOnly()
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 2.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Blackberry Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.GUNPOWDER );
        loot.addUncommonDrop( "uncommon", Items.BLACK_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<BlackberrySlimeEntity> getVariantFactory() { return BlackberrySlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BlackberrySlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final byte MAX_FUSE = 30;
    
    private int fuse = 0;
    private boolean ignited = false;
    
    public BlackberrySlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new SpecialSwellGoal<>( this ) );
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        if( isAlive() && !level().isClientSide() ) {
            if( ignited ) setSwellDir( 1 );
            
            if( getSwellDir() > 0 ) {
                if( fuse == 0 ) {
                    playSound( SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F );
                }
                else if( fuse >= MAX_FUSE ) {
                    dead = true;
                    ExplosionHelper.explode( this, getSize() + 0.5F, true, false );
                    discard();
                    spawnLingeringCloud();
                }
                changeFuse( 1 );
            }
            else if( getSwellDir() < 0 && fuse > 0 ) {
                changeFuse( -1 );
                if( fuse <= 0 ) setSwellDir( 0 );
            }
        }
        super.tick();
    }
    
    /** Changes the fuse by a specific amount. The fuse must always be changed in step with render scale, so they stay in sync. */
    private void changeFuse( int change ) {
        fuse += change;
        getSpecialData().setRenderScale( getSpecialData().getRenderScale() + change * 0.014F );
    }
    
    /** Called to create a lingering effect cloud as part of this slime's explosion 'attack'. */
    protected void spawnLingeringCloud() {
        final List<MobEffectInstance> effects = new ArrayList<>( getActiveEffects() );
        
        if( !effects.isEmpty() ) {
            final AreaEffectCloud potionCloud = new AreaEffectCloud( level(), getX(), getY(), getZ() );
            potionCloud.setRadius( getSize() + 0.5F );
            potionCloud.setRadiusOnUse( -0.5F );
            potionCloud.setWaitTime( 10 );
            potionCloud.setDuration( potionCloud.getDuration() / 2 );
            potionCloud.setRadiusPerTick( -potionCloud.getRadius() / (float) potionCloud.getDuration() );
            for( MobEffectInstance effect : effects ) {
                potionCloud.addEffect( new MobEffectInstance( effect ) );
            }
            level().addFreshEntity( potionCloud );
        }
    }
    
    /** @return This entity's max fall distance. */
    @Override
    public int getMaxFallDistance() { return getTarget() == null ? 3 : 3 + (int) (getHealth() - 1.0F); }
    
    /** @return Called when this mob falls. Calculates and applies fall damage. Returns false if canceled. */
    @Override
    public boolean causeFallDamage( float distance, float damageMultiplier, DamageSource damageSource ) {
        final boolean success = super.causeFallDamage( distance, damageMultiplier, damageSource );
        
        // Speed up fuse from falling like creepers
        changeFuse( (int) (distance * 1.5F) );
        if( fuse > MAX_FUSE - 5 ) changeFuse( MAX_FUSE - 5 - fuse );
        return success;
    }
    
    /** @return Interacts (right click) with this entity and returns the result. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand ) {
        final ItemStack item = player.getItemInHand( hand );
        if( item.getItem() == Items.FLINT_AND_STEEL ) {
            // Allow players to ignite blackberry slimes like creepers
            level().playSound( player, getX(), getY(), getZ(), SoundEvents.FLINTANDSTEEL_USE, getSoundSource(),
                    1.0F, random.nextFloat() * 0.4F + 0.8F );
            if( !level().isClientSide ) {
                ignited = true;
                item.hurtAndBreak( 1, player, ( entity ) -> entity.broadcastBreakEvent( hand ) );
            }
            return InteractionResult.sidedSuccess( level().isClientSide );
        }
        return super.mobInteract( player, hand );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundTag saveTag ) {
        saveTag.putByte( References.TAG_FUSE_TIME, (byte) fuse );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( saveTag.contains( References.TAG_FUSE_TIME, References.NBT_TYPE_NUMERICAL ) )
            fuse = saveTag.getByte( References.TAG_FUSE_TIME );
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected ParticleOptions getParticleType() { return ParticleTypes.SMOKE; }
    
    
    //--------------- IExplodingEntity Implementations ----------------
    
    private int swellDir = 0;
    
    /** Sets this exploding entity's swell direction. */
    @Override
    public void setSwellDir( int value ) { swellDir = value; }
    
    /** @return This exploding entity's swell direction. */
    @Override
    public int getSwellDir() { return swellDir; }
    
    /** @return Additional range from its target at which this entity will start to explode. */
    @Override
    public double getExtraRange() { return (getSize() - 1) * 2.0F; }
}