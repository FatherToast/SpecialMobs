package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class ThiefEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x04FA00, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialEndermanEntity.createAttributes() )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.2 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Thief Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ENDER_PEARL, 1 );
    }
    
    @SpecialMob.Constructor
    public ThiefEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private int teleportTargetDelay;
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( !level.isClientSide() && target instanceof LivingEntity && target.isAlive() && teleportTargetDelay <= 0 ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleportTarget( (LivingEntity) target ) ) {
                    teleportTargetDelay = 160; // 8s cooldown
                    for( int j = 0; j < 16; j++ ) {
                        if( teleportTowards( target ) ) break;
                    }
                    break;
                }
            }
        }
    }
    
    /** @return Teleports the target to a random nearby position; returns true if successful. */
    private boolean teleportTarget( LivingEntity target ) {
        final double xI = target.getX();
        final double yI = target.getY();
        final double zI = target.getZ();
        
        final double x = xI + (random.nextDouble() - 0.5) * 64.0;
        final double y = yI + random.nextInt( 64 ) - 32;
        final double z = zI + (random.nextDouble() - 0.5) * 64.0;
        
        if( target.isPassenger() ) {
            target.stopRiding();
        }
        
        // Note - this is based on the chorus fruit teleport, but with enderman teleport range
        EntityTeleportEvent.ChorusFruit event = ForgeEventFactory.onChorusFruitTeleport( target, x, y, z );
        if( event.isCanceled() ) return false;
        
        if( target.randomTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), true ) ) {
            level.playSound( null, xI, yI, zI, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS,
                    1.0F, 1.0F );
            target.playSound( SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F );
            return true;
        }
        return false;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        if( !level.isClientSide() ) teleportTargetDelay--;
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "thief" ),
            GET_TEXTURE_PATH( "thief_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}