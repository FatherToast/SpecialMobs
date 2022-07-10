package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class LemonSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<LemonSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6E861 )
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
    public static EntityType.IFactory<LemonSlimeEntity> getVariantFactory() { return LemonSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends LemonSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public LemonSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            ExplosionHelper.spawnLightning( level, target.getX(), target.getY(), target.getZ() );
            
            // Knock self back
            final float forwardPower = 1.1F;
            final float upwardPower = 1.0F;
            final Vector3d vKnockback = new Vector3d( target.getX() - getX(), 0.0, target.getZ() - getZ() )
                    .normalize().scale( -forwardPower ).add( getDeltaMovement().scale( 0.2F ) );
            setDeltaMovement( vKnockback.x, 0.4 * upwardPower, vKnockback.z );
        }
    }
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerWorld world, LightningBoltEntity lightningBolt ) { }
    
    private static final IParticleData JUMP_PARTICLE = new ItemParticleData( ParticleTypes.ITEM, Items.YELLOW_DYE.getDefaultInstance() );
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return JUMP_PARTICLE; }
}