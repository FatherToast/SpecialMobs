package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.BlazeSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SpecialMob
public class HellfireBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HellfireBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDDDDD )
                .uniqueTextureBaseOnly()
                .size( 1.1F, 0.7F, 1.99F )
                .addExperience( 2 )
                .fireballAttack( 0.05, 60, 100, 40.0 )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new BlazeSpeciesConfig( species, 1, 0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Hellfire",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.GUNPOWDER );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<HellfireBlazeEntity> getVariantFactory() { return HellfireBlazeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HellfireBlazeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The base explosion strength of this blaze's fireballs. */
    private int explosionPower = 2;
    
    public HellfireBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( !isSilent() ) level.levelEvent( null, References.EVENT_BLAZE_SHOOT, blockPosition(), 0 );
        
        final float accelVariance = MathHelper.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        final double dX = target.getX() - getX() + getRandom().nextGaussian() * accelVariance;
        final double dY = target.getY( 0.5 ) - getY( 0.5 );
        final double dZ = target.getZ() - getZ() + getRandom().nextGaussian() * accelVariance;
        
        final FireballEntity fireball = new FireballEntity( level, this, dX, dY, dZ );
        fireball.explosionPower = explosionPower;
        fireball.setPos( fireball.getX(), getY( 0.5 ) + 0.5, fireball.getZ() );
        level.addFreshEntity( fireball );
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_EXPLOSION_POWER, (byte) explosionPower );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_EXPLOSION_POWER, References.NBT_TYPE_NUMERICAL ) )
            explosionPower = saveTag.getByte( References.TAG_EXPLOSION_POWER );
    }
}