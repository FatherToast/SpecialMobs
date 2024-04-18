package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@SpecialMob
public class LemonSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<LemonSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6E861 ).theme( BestiaryInfo.Theme.STORM )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).fireImmune()
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Lemon Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.REDSTONE, 1 );
        loot.addUncommonDrop( "uncommon", Items.YELLOW_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<LemonSlimeEntity> getVariantFactory() { return LemonSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends LemonSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public LemonSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        ExplosionHelper.spawnLightning( level(), target.getX(), target.getY(), target.getZ() );
        
        // Knock self back
        final float forwardPower = 1.1F;
        final float upwardPower = 1.0F;
        final Vec3 vKnockback = new Vec3( target.getX() - getX(), 0.0, target.getZ() - getZ() )
                .normalize().scale( -forwardPower ).add( getDeltaMovement().scale( 0.2F ) );
        setDeltaMovement( vKnockback.x, 0.4 * upwardPower, vKnockback.z );
    }
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerLevel level, LightningBolt lightningBolt ) { }
    
    private static final ParticleOptions JUMP_PARTICLE = new ItemParticleOption( ParticleTypes.ITEM, Items.YELLOW_DYE.getDefaultInstance() );
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected ParticleOptions getParticleType() { return JUMP_PARTICLE; }
}