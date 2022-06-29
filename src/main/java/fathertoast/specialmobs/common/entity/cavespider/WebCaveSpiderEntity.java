package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
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
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class WebCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WebCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xE7E7E7, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - forest
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCaveSpiderEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 4.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.2 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Cave Weaver",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Blocks.COBWEB );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<WebCaveSpiderEntity> getVariantFactory() { return WebCaveSpiderEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public WebCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
        webCount = 2 + random.nextInt( 5 );
    }
    
    /** The number of cobwebs this spider can place. */
    private int webCount;
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( !level.isClientSide() && webCount > 0 && target instanceof LivingEntity && !(target instanceof SpiderEntity) ) {
            final BlockPos pos = target.blockPosition();
            if( !tryPlaceWeb( pos ) && target.getBbHeight() > 1.0F ) {
                tryPlaceWeb( pos.above() );
            }
        }
    }
    
    /** Called when this entity dies to add drops regardless of loot table. */
    @Override
    protected void dropCustomDeathLoot( DamageSource source, int looting, boolean killedByPlayer ) {
        super.dropCustomDeathLoot( source, looting, killedByPlayer );
        tryPlaceWeb( blockPosition() );
    }
    
    /** @return Attempts to place a cobweb at the given position and returns true if successful. */
    private boolean tryPlaceWeb( BlockPos pos ) {
        if( level.getBlockState( pos ).getMaterial().isReplaceable() ) {
            level.setBlock( pos, Blocks.COBWEB.defaultBlockState(), References.SET_BLOCK_FLAGS );
            webCount--;
            return true;
        }
        return false;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_AMMO, (byte) webCount );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_AMMO, References.NBT_TYPE_NUMERICAL ) )
            webCount = saveTag.getByte( References.TAG_AMMO );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "web" ),
            GET_TEXTURE_PATH( "web_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}