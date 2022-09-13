package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.CorporealShiftGhastSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@SpecialMob
public class CorporealShiftGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<CorporealShiftGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xA7FF9B ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithAnimation()
                .addExperience( 4 ).regen( 80 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new CorporealShiftGhastSpeciesConfig( species, 300, 200 );
    }
    
    /** @return This entity's species config. */
    public CorporealShiftGhastSpeciesConfig getConfig() { return (CorporealShiftGhastSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Corporeal Shift Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "cluster", SMItems.INCORPOREAL_FIREBALL.get() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<CorporealShiftGhastEntity> getVariantFactory() { return CorporealShiftGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends CorporealShiftGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public static final DataParameter<Boolean> CORPOREAL = EntityDataManager.defineId( CorporealShiftGhastEntity.class, DataSerializers.BOOLEAN );
    
    private int shiftTime;
    
    public CorporealShiftGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) { super( entityType, world ); }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( CORPOREAL, false );
        if( !level.isClientSide() ) setCorporeal( random.nextBoolean() );
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if( --shiftTime <= 0 ) {
            if( !level.isClientSide() ) {
                setCorporeal( !isCorporeal() );
                spawnShiftSmoke( (ServerWorld) level );
            }
        }
    }
    
    private void spawnShiftSmoke( ServerWorld world ) {
        world.sendParticles( ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
                25, 0.0, 0.0, 0.0, 0.4 );
    }
    
    @Override
    public boolean isPushable() { return isCorporeal() && super.isPushable(); }
    
    @Override
    protected void doPush( Entity entity ) { if( isCorporeal() ) super.doPush( entity ); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) { return isCorporeal() && super.hurt( source, amount ); }
    
    public boolean isCorporeal() { return entityData.get( CORPOREAL ); }
    
    private void setCorporeal( boolean value ) {
        entityData.set( CORPOREAL, value );
        shiftTime = value ? getConfig().CORPOREAL_SHIFT.corporealTicks.get() : getConfig().CORPOREAL_SHIFT.incorporealTicks.get();
    }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( isCorporeal() ) {
            super.performRangedAttack( target, damageMulti );
            return;
        }
        
        References.LevelEvent.GHAST_SHOOT.play( this );
        
        final float accelVariance = MathHelper.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        final Vector3d lookVec = getViewVector( 1.0F ).scale( getBbWidth() );
        double dX = target.getX() - (getX() + lookVec.x) + getRandom().nextGaussian() * accelVariance;
        double dY = target.getY( 0.5 ) - (0.5 + getY( 0.5 ));
        double dZ = target.getZ() - (getZ() + lookVec.z) + getRandom().nextGaussian() * accelVariance;
        
        final IncorporealFireballEntity fireball = new IncorporealFireballEntity( level, this, dX, dY, dZ );
        fireball.explosionPower = getVariantExplosionPower( getExplosionPower() );
        fireball.setPos(
                getX() + lookVec.x,
                getY( 0.5 ) + 0.5,
                getZ() + lookVec.z );
        level.addFreshEntity( fireball );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putBoolean( References.TAG_IS_SHIFTED, isCorporeal() );
        saveTag.putShort( References.TAG_SHIFT_TIME, (short) shiftTime );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_IS_SHIFTED, References.NBT_TYPE_NUMERICAL ) )
            setCorporeal( saveTag.getBoolean( References.TAG_IS_SHIFTED ) );
        if( saveTag.contains( References.TAG_SHIFT_TIME, References.NBT_TYPE_NUMERICAL ) )
            shiftTime = saveTag.getShort( References.TAG_SHIFT_TIME );
    }
}