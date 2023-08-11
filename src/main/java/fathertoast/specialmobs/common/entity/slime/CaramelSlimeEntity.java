package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

@SpecialMob
public class CaramelSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<CaramelSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x9D733F )
                .uniqueTextureBaseOnly()
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 8.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Caramel Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.SUGAR );
        loot.addUncommonDrop( "uncommon", Items.ORANGE_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<CaramelSlimeEntity> getVariantFactory() { return CaramelSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends CaramelSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private final DamageSource grabDamageSource = DamageSource.mobAttack( this ).bypassArmor().bypassMagic();
    
    private int grabTime;
    
    public CaramelSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( grabTime <= -20 && getPassengers().isEmpty() ) {
            if( target.startRiding( this, true ) ) grabTime = 20;
        }
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        
        grabTime--;
        final List<Entity> riders = getPassengers();
        if( grabTime <= 0 && !riders.isEmpty() ) {
            for( Entity rider : riders ) {
                if( rider instanceof LivingEntity ) {
                    rider.hurt( grabDamageSource, 1.0F );
                    grabTime = 10;
                }
            }
        }
    }
    
    private static final ParticleOptions JUMP_PARTICLE = new ItemParticleOption( ParticleTypes.ITEM, Items.ORANGE_DYE.getDefaultInstance() );
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected ParticleOptions getParticleType() { return JUMP_PARTICLE; }
}