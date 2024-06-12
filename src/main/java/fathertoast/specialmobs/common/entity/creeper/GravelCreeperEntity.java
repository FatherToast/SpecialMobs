package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@SpecialMob
public class GravelCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GravelCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x908884 )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).burnImmune();
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
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<GravelCreeperEntity> getVariantFactory() { return GravelCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends GravelCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public GravelCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius / 2.0F ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.BlockInteraction explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this, explosionPower, explosionMode, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        if( explosionMode == Explosion.BlockInteraction.KEEP || level().isClientSide() ) return;
        
        final float throwPower = explosionPower + 4.0F;
        final int count = (int) Math.ceil( throwPower * throwPower * 3.5F );
        for( int i = 0; i < count; i++ ) {
            BlockPos pos = BlockPos.containing(getX(), getY() + getBbHeight() / 2.0F, getZ());
            FallingBlockEntity gravel = new FallingBlockEntity(level(), (double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, Blocks.GRAVEL.defaultBlockState());
            gravel.time = 1; // Prevent the entity from instantly dying
            gravel.dropItem = false;
            gravel.fallDistance = 3.0F;
            
            final float speed = (throwPower * 0.7F + random.nextFloat() * throwPower) / 20.0F;
            final float pitch = random.nextFloat() * (float) Math.PI;
            final float yaw = random.nextFloat() * 2.0F * (float) Math.PI;
            gravel.setDeltaMovement(
                    Mth.cos( yaw ) * speed,
                    Mth.sin( pitch ) * (throwPower + random.nextFloat() * throwPower) / 18.0F,
                    Mth.sin( yaw ) * speed );
            level().addFreshEntity( gravel );
        }
        spawnAnim();
        playSound( SoundEvents.GRAVEL_BREAK, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( source.is( DamageTypes.FALLING_BLOCK ) ||
                source.is( DamageTypes.FALLING_ANVIL ) ) {
            return true;
        }
        return super.hurt( source, amount );
    }
}