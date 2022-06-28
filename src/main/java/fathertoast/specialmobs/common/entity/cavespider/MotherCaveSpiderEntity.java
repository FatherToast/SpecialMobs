package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class MotherCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MotherCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.9F, 0.6F );
        return new BestiaryInfo( 0xB300B3 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCaveSpiderEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 16.0 )
                .addAttribute( Attributes.ARMOR, 6.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 3.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mother Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.CAVE_SPIDER_SPAWN_EGG );
    }
    
    @SpecialMob.Constructor
    public MotherCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 0.8F );
        getSpecialData().setRegenerationTime( 30 );
        xpReward += 1;
        
        babies = 2 + random.nextInt( 3 );
        extraBabies = 2 + random.nextInt( 3 );
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The number of babies spawned on death. */
    private int babies;
    /** The number of extra babies that can be spawned from hits. */
    private int extraBabies;
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackDamage += 1.5F;
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            // Spawn babies when damaged
            if( extraBabies > 0 && amount > 1.0F && level instanceof IServerWorld && random.nextFloat() < 0.33F ) {
                extraBabies--;
                spawnBaby( 0.66F, null );
                playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
            }
            return true;
        }
        return false;
    }
    
    /** Called to remove this entity from the world. Includes death, unloading, interdimensional travel, etc. */
    @Override
    public void remove( boolean keepData ) {
        //noinspection deprecation
        if( isDeadOrDying() && !removed && level instanceof IServerWorld ) { // Same conditions as slime splitting
            // Spawn babies on death
            final int babiesToSpawn = babies + extraBabies;
            ILivingEntityData groupData = null;
            for( int i = 0; i < babiesToSpawn; i++ ) {
                groupData = spawnBaby( 0.33F, groupData );
            }
            playSound( SoundEvents.EGG_THROW, 1.0F, 2.0F / (random.nextFloat() * 0.4F + 0.8F) );
        }
        super.remove( keepData );
    }
    
    /** Helper method to simplify spawning babies. */
    @Nullable
    private ILivingEntityData spawnBaby( float speed, @Nullable ILivingEntityData groupData ) {
        final BabyCaveSpiderEntity baby = BabyCaveSpiderEntity.SPECIES.entityType.get().create( level );
        if( baby == null ) return groupData;
        
        baby.copyPosition( this );
        baby.yHeadRot = yRot;
        baby.yBodyRot = yRot;
        groupData = baby.finalizeSpawn( (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                SpawnReason.MOB_SUMMONED, groupData, null );
        baby.setTarget( getTarget() );
        
        baby.setDeltaMovement(
                (random.nextDouble() - 0.5) * speed,
                0.2 + 0.5 * random.nextDouble(), // Used to cause floor clip bug; remove if it happens again
                (random.nextDouble() - 0.5) * speed );
        baby.setOnGround( false );
        
        level.addFreshEntity( baby );
        return groupData;
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_BABIES, (byte) babies );
        saveTag.putByte( References.TAG_EXTRA_BABIES, (byte) extraBabies );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_BABIES, References.NBT_TYPE_NUMERICAL ) )
            babies = saveTag.getByte( References.TAG_BABIES );
        if( saveTag.contains( References.TAG_EXTRA_BABIES, References.NBT_TYPE_NUMERICAL ) )
            extraBabies = saveTag.getByte( References.TAG_EXTRA_BABIES );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "mother" ),
            GET_TEXTURE_PATH( "mother_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}