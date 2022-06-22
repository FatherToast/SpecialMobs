package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class SplittingCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x5F9D22, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCreeperEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 20.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Splitting Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.CREEPER_SPAWN_EGG );
    }
    
    @SpecialMob.Constructor
    public SplittingCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setImmuneToBurning( true );
        setExplodesWhenShot( true );
        xpReward += 2;
        extraBabies = random.nextInt( 4 );
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of extra mini creepers spawned on explosion (in addition to the amount based on explosion power). */
    private int extraBabies;
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, false, false );
        
        final int babiesToSpawn = extraBabies + (int) (explosionPower * explosionPower) / 2;
        for( int i = 0; i < babiesToSpawn; i++ ) {
            final MiniCreeperEntity baby = MiniCreeperEntity.ENTITY_TYPE.get().create( level );
            if( baby == null ) continue;
            
            baby.copyPosition( this );
            baby.yHeadRot = yRot;
            baby.yBodyRot = yRot;
            baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ), SpawnReason.MOB_SUMMONED, null, null );
            baby.setTarget( getTarget() );
            if( isPowered() ) baby.getEntityData().set( DATA_IS_POWERED, true );
            
            baby.setDeltaMovement(
                    (random.nextDouble() - 0.5) * explosionPower / 3.0,
                    0.3 + 0.3 * random.nextDouble(), // Used to cause floor clip bug; remove if it happens again
                    (random.nextDouble() - 0.5) * explosionPower / 3.0 );
            baby.setOnGround( false );
            
            level.addFreshEntity( baby );
        }
        playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) extraBabies );
    }
    
    /** Override to load data from this entity's  NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            extraBabies = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "splitting" ),
            GET_TEXTURE_PATH( "splitting_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}