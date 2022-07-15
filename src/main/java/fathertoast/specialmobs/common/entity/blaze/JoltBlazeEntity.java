package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

@SpecialMob
public class JoltBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<JoltBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x499CAE )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).disableRangedAttack()
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 ).addToAttribute( Attributes.ARMOR, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.3 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Jolt",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.REDSTONE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<JoltBlazeEntity> getVariantFactory() { return JoltBlazeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends JoltBlazeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public JoltBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.causeLifeLoss( (LivingEntity) target, 2.0F );
        }
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() && isAlive() && getTarget() != null && random.nextInt( 20 ) == 0 &&
                distanceToSqr( getTarget() ) > 256.0 ) {
            for( int i = 0; i < 16; i++ ) {
                if( teleportTowards( getTarget() ) ) break;
            }
        }
        super.aiStep();
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isInvulnerableTo( source ) || fireImmune() && source.isFire() ) return false;
        
        if( source instanceof IndirectEntityDamageSource ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleport() ) return true;
            }
            return false;
        }
        
        final boolean success = super.hurt( source, amount );
        if( !level.isClientSide() && getHealth() > 0.0F ) {
            if( source.getEntity() instanceof LivingEntity ) {
                for( int i = 0; i < 16; i++ ) {
                    if( teleport() ) break;
                }
            }
            else if( random.nextInt( 10 ) != 0 ) {
                teleport();
            }
        }
        return success;
    }
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerWorld world, LightningBoltEntity lightningBolt ) { }
    
    /** @return Teleports this "enderman" to a random nearby position; returns true if successful. */
    protected boolean teleport() {
        if( level.isClientSide() || !isAlive() ) return false;
        
        final double x = getX() + (random.nextDouble() - 0.5) * 32.0;
        final double y = getY() + (double) (random.nextInt( 16 ) - 8);
        final double z = getZ() + (random.nextDouble() - 0.5) * 32.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" towards another entity; returns true if successful. */
    protected boolean teleportTowards( Entity target ) {
        final Vector3d directionFromTarget = new Vector3d(
                getX() - target.getX(),
                getY( 0.5 ) - target.getEyeY(),
                getZ() - target.getZ() )
                .normalize();
        
        final double x = getX() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.x * 16.0;
        final double y = getY() + (double) (random.nextInt( 8 ) - 2) - directionFromTarget.y * 16.0;
        final double z = getZ() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.z * 16.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" to a new position; returns true if successful. */
    protected boolean teleport( double x, double y, double z ) {
        final BlockPos.Mutable pos = new BlockPos.Mutable( x, y, z );
        
        while( pos.getY() > 0 && !level.getBlockState( pos ).getMaterial().blocksMotion() ) {
            pos.move( Direction.DOWN );
        }
        
        final BlockState block = level.getBlockState( pos );
        if( !block.getMaterial().blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) return false;
        
        EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y, z );
        if( event.isCanceled() ) return false;
        
        final boolean success = randomTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), false );
        if( success ) {
            ExplosionHelper.spawnLightning( level, xo, yo, zo );
            ExplosionHelper.spawnLightning( level, getX(), getY(), getZ() );
        }
        return success;
    }
}