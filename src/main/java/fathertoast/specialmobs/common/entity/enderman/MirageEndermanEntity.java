package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class MirageEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.TypeHolder
    public static RegistryObject<EntityType<MirageEndermanEntity>> ENTITY_TYPE;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xC2BC84, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - desert
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialEndermanEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 20.0 )
                .build();
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
    
    @SpecialMob.Constructor
    public MirageEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Whether this mirage enderman is fake. */
    public boolean isFake = false;
    
    /** Sets this mirage enderman as fake. */
    public void setFake() {
        isFake = true;
        xpReward = 0;
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
        
        double xI = getX();
        double yI = getY();
        double zI = getZ();
        
        if( super.teleport( x, y, z ) ) {
            mirage( xI, yI, zI );
            return true;
        }
        return false;
    }
    
    private void mirage( double xI, double yI, double zI ) {
        if( !isFake && getTarget() != null ) {
            final MirageEndermanEntity mirage = ENTITY_TYPE.get().create( level );
            if( mirage == null ) return;
            
            mirage.setFake();
            mirage.copyPosition( this );
            mirage.setTarget( getTarget() );
            
            // Return one of the endermen to the initial position (either the real or the fake)
            if( random.nextInt( 4 ) == 0 ) {
                moveTo( xI, yI, zI );
            }
            else {
                mirage.moveTo( xI, yI, zI );
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
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "mirage" ),
            GET_TEXTURE_PATH( "mirage_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}