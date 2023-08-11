package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;

@SpecialMob
public class ThiefEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ThiefEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x04FA00 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithEyes()
                .addExperience( 2 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
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
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<ThiefEndermanEntity> getVariantFactory() { return ThiefEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ThiefEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private int teleportTargetDelay;
    
    public ThiefEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( !level.isClientSide() && target.isAlive() && teleportTargetDelay <= 0 ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleportTarget( target ) ) {
                    teleportTargetDelay = 160;
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
            level.playSound( null, xI, yI, zI, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS,
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
}