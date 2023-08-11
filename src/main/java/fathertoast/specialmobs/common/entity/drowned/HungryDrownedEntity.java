package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

@SpecialMob
public class HungryDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HungryDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xAB1518 )
                .uniqueTextureWithOverlay()
                .addExperience( 2 ).regen( 30 ).disableRangedAttack()
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.3 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Drowned Hunger",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BONE );
        loot.addUncommonDrop( "uncommon", Items.BEEF, Items.CHICKEN, Items.MUTTON, Items.PORKCHOP, Items.RABBIT, Items.COOKIE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<HungryDrownedEntity> getVariantFactory() { return HungryDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HungryDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public HungryDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) {
        setCanPickUpLoot( false );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( level.isClientSide() ) return;
        
        if( target instanceof Player player && ForgeEventFactory.getMobGriefingEvent( level, this ) ) {
            final ItemStack food = MobHelper.stealRandomFood( player );
            if( !food.isEmpty() ) {
                final FoodProperties foodStats = food.getItem().getFoodProperties( food, this );
                heal( Math.max( foodStats == null ? 0.0F : foodStats.getNutrition(), 1.0F ) );
                playSound( SoundEvents.PLAYER_BURP, 0.5F, random.nextFloat() * 0.1F + 0.9F );
                return;
            }
        }
        // Take a bite out of the target if they have no food to eat
        MobHelper.stealLife( this, target, 2.0F );
    }
}