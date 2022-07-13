package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class MirageEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MirageEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xC2BC84 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureWithEyes()
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mirage Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Blocks.SAND, 1 );
        loot.addUncommonDrop( "uncommon", Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS,
                Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<MirageEndermanEntity> getVariantFactory() { return MirageEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends MirageEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Whether this mirage enderman is fake. */
    public boolean isFake = false;
    
    public MirageEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Sets this mirage enderman as fake. */
    public void setFake() {
        isFake = true;
        setExperience( 0 );
        //noinspection ConstantConditions
        getAttribute( Attributes.ATTACK_DAMAGE ).setBaseValue( 0.0 );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        for( int i = 0; i < 64; i++ ) {
            if( teleport() ) break;
        }
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        if( isFake && tickCount >= 200 ) {
            spawnAnim();
            remove();
        }
        super.tick();
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isFake ) {
            setHealth( 0.0F );
            return true;
        }
        return super.hurt( source, amount );
    }
    
    /** @return Teleports this enderman to a new position; returns true if successful. */
    @Override
    protected boolean teleport( double x, double y, double z ) {
        if( isFake ) return false;
        
        if( super.teleport( x, y, z ) ) {
            mirage();
            return true;
        }
        return false;
    }
    
    private void mirage() {
        if( !isFake && getTarget() != null ) {
            final MirageEndermanEntity mirage = SPECIES.entityType.get().create( level );
            if( mirage == null ) return;
            
            mirage.setFake();
            mirage.copyPosition( this );
            mirage.setTarget( getTarget() );
            
            // Return one of the endermen to the initial position (either the real or the fake)
            if( random.nextInt( 4 ) == 0 ) {
                moveTo( xo, yo, zo );
            }
            else {
                mirage.moveTo( xo, yo, zo );
            }
            
            mirage.setHealth( getHealth() );
            level.addFreshEntity( mirage );
        }
    }
    
    /** Called to generate drops from this entity's loot table. */
    @Override
    protected void dropFromLootTable( DamageSource source, boolean killedByPlayer ) {
        if( !isFake ) super.dropFromLootTable( source, killedByPlayer );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putBoolean( References.TAG_IS_FAKE, isFake );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_IS_FAKE, References.NBT_TYPE_NUMERICAL ) )
            isFake = saveTag.getBoolean( References.TAG_IS_FAKE );
    }
}