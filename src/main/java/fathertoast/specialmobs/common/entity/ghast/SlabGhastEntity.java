package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.projectile.SlabFireballEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SlabGhastEntity extends _SpecialGhastEntity {

    //--------------- Static Special Mob Hooks ----------------

    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SlabGhastEntity> SPECIES;

    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE8C51A )
                .weight( BestiaryInfo.DefaultWeight.DEFAULT )
                .uniqueTextureWithAnimation()
                .size( 1.0F, 4.0F, 2.0F )
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 5.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.5 );
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Slab Ghast",
                "", "", "", "", "", "" );//TODO
    }

    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", SMItems.SLAB_FIREBALL.get() );
        loot.addUncommonDrop( "uncommon", Items.QUARTZ_SLAB );
    }

    @SpecialMob.Factory
    public static EntityType.EntityFactory<SlabGhastEntity> getVariantFactory() { return SlabGhastEntity::new; }

    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SlabGhastEntity> getSpecies() { return SPECIES; }


    //--------------- Variant-Specific Implementations ----------------

    public SlabGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, Level level ) { super( entityType, level ); }

    /** Override to change this ghast's explosion power multiplier. */
    @Override
    protected int getVariantExplosionPower( int radius ) { return Math.round( radius * 0.5F ); }

    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack(LivingEntity target, float damageMulti ) {
        References.LevelEvent.GHAST_SHOOT.play( this );

        final float accelVariance = Mth.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        final Vec3 lookVec = getViewVector( 1.0F ).scale( getBbWidth() );
        double dX = target.getX() - (getX() + lookVec.x) + getRandom().nextGaussian() * accelVariance;
        double dY = target.getY( 0.5 ) - (0.5 + getY( 0.5 ));
        double dZ = target.getZ() - (getZ() + lookVec.z) + getRandom().nextGaussian() * accelVariance;

        final SlabFireballEntity fireball = new SlabFireballEntity( level(), this, dX, dY, dZ );
        fireball.explosionPower = getVariantExplosionPower( getExplosionPower() );
        fireball.setPos(
                getX() + lookVec.x,
                getY( 0.5 ) + 0.5,
                getZ() + lookVec.z );
        level().addFreshEntity( fireball );
    }
}
