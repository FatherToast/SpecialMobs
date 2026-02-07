package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMSounds;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@SpecialMob
@SuppressWarnings( "resource" )
public class ImplodingCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ImplodingCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x9D7AA3 )
                .uniqueTextureBaseOnly()
                .addExperience( 1 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Imploding Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        // No loot
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<ImplodingCreeperEntity> getVariantFactory() { return ImplodingCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ImplodingCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public ImplodingCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius / 2.0F ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        double scale = 1;
        
        if( isPowered() )
            scale += 1;
        else if( isSupercharged() )
            scale += 3;
        
        final List<Entity> nearbyEntities = level().getEntitiesOfClass( Entity.class, new AABB( blockPosition() ).inflate( 7.5D * scale ) );
        
        // Pull nearby entities
        for( Entity entity : nearbyEntities ) {
            final HitResult bottomHit = level().clip(
                    new ClipContext( new Vec3( position().x, position().y, position().z ), entity.position(),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null ) );
            
            final HitResult topHit = level().clip(
                    new ClipContext( new Vec3( position().x, position().y + getBbHeight(), position().z ), entity.position(),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null ) );
            
            if( bottomHit.getType() == HitResult.Type.BLOCK && topHit.getType() == HitResult.Type.BLOCK )
                continue;
            
            Vec3 vec3 = new Vec3( getX() - entity.getX(), getY() - entity.getY(), getZ() - entity.getZ() );
            vec3 = vec3.normalize().multiply( 2.25 * scale, 2.25 * scale, 2.25 * scale );
            
            entity.setDeltaMovement( entity.getDeltaMovement().add( vec3 ) );
            entity.hasImpulse = true;
            
            // Send motion packet for players
            if( entity instanceof ServerPlayer serverPlayer ) {
                serverPlayer.connection.send( new ClientboundSetEntityMotionPacket( entity ) );
            }
        }
        playSound( SMSounds.IMPLODING_CREEPER_IMPLODE.get(), 4.0F, 1.0F + random.nextFloat() * 0.2F );
    }
}
