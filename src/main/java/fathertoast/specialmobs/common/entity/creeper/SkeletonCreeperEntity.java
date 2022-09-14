package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.projectile.BoneShrapnelEntity;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.ai.goal.RestrictSunGoal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@SpecialMob
public class SkeletonCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SkeletonCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xC1C1C1 ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).undead()
                .addToAttribute( Attributes.MAX_HEALTH, -4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Skeleton Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.SKELETON.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<SkeletonCreeperEntity> getVariantFactory() { return SkeletonCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SkeletonCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SkeletonCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 3, new FleeSunGoal( this, 1.0 ) );
        AIHelper.insertGoal( goalSelector, 3, new RestrictSunGoal( this ) );
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius / 2.0F ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, true, false );
        
        if( level.isClientSide() ) return;
        
        final float shootPower = explosionPower * 2.0F + 4.0F;
        final int count = (int) Math.ceil( shootPower * shootPower * 3.5F );
        for( int i = 0; i < count; i++ ) {
            final BoneShrapnelEntity shrapnel = makeShrapnel( shootPower );
            
            final float speed = (0.7F + random.nextFloat()) * shootPower / 20.0F;
            final float pitch = random.nextFloat() * (float) Math.PI;
            final float yaw = random.nextFloat() * 2.0F * (float) Math.PI;
            final Vector3d velocity = new Vector3d(
                    MathHelper.cos( yaw ) * speed,
                    MathHelper.sin( pitch ) * (shootPower + random.nextFloat() * shootPower) / 18.0F,
                    MathHelper.sin( yaw ) * speed );
            shrapnel.shoot( velocity.x, velocity.y, velocity.z, (float) velocity.length(), 0.0F );
            
            shrapnel.life += pitch * 6.0F;
            level.addFreshEntity( shrapnel );
        }
        spawnAnim();
        playSound( SoundEvents.SKELETON_DEATH, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
    }
    
    /** @return A new shrapnel entity to shoot. */
    private BoneShrapnelEntity makeShrapnel( float damage ) {
        final BoneShrapnelEntity shrapnel = new BoneShrapnelEntity( this );
        
        shrapnel.setEnchantmentEffectsFromEntity( this, damage );
        if( isOnFire() ) shrapnel.setSecondsOnFire( 100 );
        
        byte pierce = 1;
        if( isPowered() ) {
            shrapnel.setCritArrow( true );
            pierce++;
        }
        if( isSupercharged() ) {
            shrapnel.setKnockback( shrapnel.knockback + 2 );
            pierce++;
        }
        shrapnel.setPierceLevel( pierce );
        
        return shrapnel;
    }
    
    /** @return This entity's creature type. */
    @Override
    public CreatureAttribute getMobType() { return CreatureAttribute.UNDEAD; }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( isSunBurnTick() ) {
            final ItemStack hat = getItemBySlot( EquipmentSlotType.HEAD );
            if( !hat.isEmpty() ) {
                if( hat.isDamageableItem() ) {
                    hat.setDamageValue( hat.getDamageValue() + random.nextInt( 2 ) );
                    if( hat.getDamageValue() >= hat.getMaxDamage() ) {
                        broadcastBreakEvent( EquipmentSlotType.HEAD );
                        setItemSlot( EquipmentSlotType.HEAD, ItemStack.EMPTY );
                    }
                }
            }
            else {
                setSecondsOnFire( 8 );
            }
        }
        super.aiStep();
    }
}