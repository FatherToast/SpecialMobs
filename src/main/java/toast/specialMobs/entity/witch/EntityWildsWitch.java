package toast.specialMobs.entity.witch;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;
import toast.specialMobs.entity.spider.EntityBabySpider;
import toast.specialMobs.entity.spider.EntitySmallSpider;
import toast.specialMobs.entity.spider.Entity_SpecialSpider;

public class EntityWildsWitch extends Entity_SpecialWitch {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "witch/wilds.png" )
    };
    
    /// The number of spiders this witch can spawn.
    public byte spiderCount;
    /// The number of times this witch can spawn baby spiders.
    public byte babyCount;
    /// The number of baby spiders this witch can spawn at once.
    public byte babiesPerSpawn = 3;
    
    
    public EntityWildsWitch( World world ) {
        super( world );
        getSpecialData().setTextures( EntityWildsWitch.TEXTURES );
        getSpecialData().isImmuneToWebs = true;
        spiderCount = (byte) (rand.nextInt( 4 ) + 1);
        babyCount = (byte) (rand.nextInt( 3 ) + 2);
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 0.7 );
    }
    
    /// Called when the witch is looking for a potion to drink.
    @Override
    public void tryDrinkPotion() {
        if( potionThrowDelay <= 0 ) {
            EntityLivingBase riding = ridingEntity instanceof EntityLivingBase ? (EntityLivingBase) ridingEntity : null;
            
            if( riding != null && riding.isBurning() && !riding.isPotionActive( Potion.fireResistance ) ) {
                drinkPotion( 16387 ); // Splash Fire Resistance
            }
            else if( isBurning() && !isPotionActive( Potion.fireResistance ) ) {
                drinkPotion( 8195 ); // Fire Resistance
            }
            else if( rand.nextFloat() < 0.2F && riding != null && riding.isInsideOfMaterial( Material.water ) && !riding.isPotionActive( Potion.waterBreathing ) ||
                    rand.nextFloat() < 0.15F && isInsideOfMaterial( Material.water ) && !isPotionActive( Potion.waterBreathing ) ) {
                if( riding != null && riding.isInsideOfMaterial( Material.water ) && !riding.isPotionActive( Potion.waterBreathing ) ) {
                    drinkPotion( 16397 ); // Splash Water Breathing
                }
                else {
                    drinkPotion( 8205 ); // Water Breathing
                }
            }
            else if( rand.nextFloat() < 0.1F && riding != null && riding.getHealth() < riding.getMaxHealth() ||
                    rand.nextFloat() < 0.05F && getHealth() < getMaxHealth() ) {
                if( riding != null && riding.getHealth() < riding.getMaxHealth() ) {
                    drinkPotion( 16389 ); // Splash Instant Health
                }
                else {
                    drinkPotion( 8197 ); // Instant Health
                }
            }
            else if( spiderCount > 0 && ridingEntity == null ) {
                if( rand.nextFloat() < 0.1F && getAttackTarget() != null ) {
                    potionThrowDelay = 8;
                    spiderCount--;
                    Entity_SpecialSpider spider = new Entity_SpecialSpider( worldObj );
                    spider.setLocationAndAngles( posX, posY, posZ, rotationYaw, 0.0F );
                    if( !worldObj.getCollidingBoundingBoxes( spider, spider.boundingBox ).isEmpty() ) {
                        // Spider too big, get a smaller one
                        spider = new EntitySmallSpider( worldObj );
                        spider.setLocationAndAngles( posX, posY, posZ, rotationYaw, 0.0F );
                    }
                    spider.onSpawnWithEgg( (IEntityLivingData) null );
                    spider.isHostile = true;
                    spider.setTarget( getAttackTarget() );
                    
                    SpecialMobData data = spider.getSpecialData();
                    data.arrowRefireMin = 0;
                    data.arrowRefireMax = 0;
                    data.arrowRange = 0.0F;
                    
                    worldObj.spawnEntityInWorld( spider );
                    mountEntity( spider );
                    worldObj.playSoundAtEntity( spider, "mob.ghast.fireball", 0.5F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
                }
            }
            else if( rand.nextFloat() < 0.2F && riding != null && getAttackTarget() != null && !riding.isPotionActive( Potion.moveSpeed ) && getAttackTarget().getDistanceSqToEntity( this ) > 121.0 ) {
                drinkPotion( 16418 ); // Splash Swiftness II
            }
            else if( rand.nextFloat() < 0.1F && riding == null && getAttackTarget() != null && !isPotionActive( Potion.moveSpeed ) && getAttackTarget().getDistanceSqToEntity( this ) > 121.0 ) {
                drinkPotion( 16386 ); // Splash Swiftness
            }
            else {
                tryDrinkPotionByType();
            }
        }
    }
    
    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack( EntityLivingBase target, float range ) {
        if( babyCount > 0 && rand.nextInt( 4 ) == 0 ) {
            babyCount--;
            EntityBabySpider baby = null;
            for( int i = babiesPerSpawn; i-- > 0; ) {
                baby = new EntityBabySpider( worldObj );
                baby.copyLocationAndAnglesFrom( this );
                baby.onSpawnWithEgg( (IEntityLivingData) null );
                baby.isHostile = true;
                baby.setTarget( getAttackTarget() );
                
                SpecialMobData data = baby.getSpecialData();
                data.arrowRefireMin = 0;
                data.arrowRefireMax = 0;
                data.arrowRange = 0.0F;
                
                worldObj.spawnEntityInWorld( baby );
            }
            if( baby != null ) {
                worldObj.playSoundAtEntity( baby, "mob.ghast.fireball", 0.5F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
                baby.spawnExplosionParticle();
            }
        }
        else {
            super.attackEntityWithRangedAttack( target, range );
        }
    }
    
    /// Overridden to modify potion attacks. Returns true if the potion was modified.
    @Override
    protected boolean adjustSplashPotionByType( EntityPotion thrownPotion, EntityLivingBase target, float range, float distance ) {
        thrownPotion.setPotionDamage( 16388 ); // The default potion // Splash Poison
        if( target.getHealth() <= 2.0F ) {
            super.adjustSplashPotionByType( thrownPotion, target, range, distance ); // Default - damage dealing potion
        }
        return true;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.spider_eye, 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.fermented_spider_eye, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        entityDropItem( new ItemStack( Items.spawn_egg, 1, EntityList.getEntityID( new EntitySkeleton( worldObj ) ) ), 0.0F );
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setByte( "Spiders", spiderCount );
        saveTag.setByte( "BabyCount", babyCount );
        saveTag.setByte( "BabiesPerSpawn", babiesPerSpawn );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "Spiders" ) ) {
            spiderCount = saveTag.getByte( "Spiders" );
        }
        else if( tag.hasKey( "Spiders" ) ) {
            spiderCount = tag.getByte( "Spiders" );
        }
        if( saveTag.hasKey( "BabyCount" ) ) {
            babyCount = saveTag.getByte( "BabyCount" );
        }
        else if( tag.hasKey( "BabyCount" ) ) {
            babyCount = tag.getByte( "BabyCount" );
        }
        if( saveTag.hasKey( "BabiesPerSpawn" ) ) {
            babiesPerSpawn = saveTag.getByte( "BabiesPerSpawn" );
        }
        else if( tag.hasKey( "BabiesPerSpawn" ) ) {
            babiesPerSpawn = tag.getByte( "BabiesPerSpawn" );
        }
    }
}