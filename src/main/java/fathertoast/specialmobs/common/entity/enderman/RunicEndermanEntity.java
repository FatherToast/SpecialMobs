package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.RunicEndermanBeamAttackGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SpecialMob
public class RunicEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<RunicEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE42281 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).fallImmune().burnImmune()
                .convertRangedAttackToBeam( 2.0, 1.0, 60, 100, 16.0 )
                .addToAttribute( Attributes.ARMOR, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 1.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
        
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Runic Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Blocks.STONE );
        loot.addRareDrop( "rare", Items.END_CRYSTAL );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<RunicEndermanEntity> getVariantFactory() { return RunicEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends RunicEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The actual range of the beam when fired. Roughly based on end crystal max range. */
    public static final double BEAM_MAX_RANGE = 32.0;
    /** The parameter for beam attack state. */
    private static final EntityDataAccessor<Byte> BEAM_STATE = SynchedEntityData.defineId( RunicEndermanEntity.class, EntityDataSerializers.BYTE );
    
    public RunicEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( BEAM_STATE, BeamState.OFF.id() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 2, new RunicEndermanBeamAttackGoal( this ) );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.LEVITATION );
        MobHelper.knockback( this, target, 2.0F, 0.0F );
    }
    
    /** @return True if this enderman is using its beam attack. */
    public BeamState getBeamState() { return BeamState.of( entityData.get( BEAM_STATE ) ); }
    
    /** Sets whether this enderman is using its beam attack. */
    public void setBeamState( BeamState value ) { entityData.set( BEAM_STATE, value.id() ); }
    
    /** @return The bounding box to use for frustum culling. */
    @OnlyIn( Dist.CLIENT )
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBeamState() == BeamState.OFF ? super.getBoundingBoxForCulling() :
                super.getBoundingBoxForCulling().expandTowards( getViewVector( 1.0F ).scale( BEAM_MAX_RANGE ) );
    }
    
    public enum BeamState {
        OFF, CHARGING, DAMAGING;
        
        public byte id() { return (byte) ordinal(); }
        
        public static BeamState of( byte id ) { return values()[id]; }
    }
}