package toast.specialMobs.entity.silverfish;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntitySpecialFishHook;
import toast.specialMobs.entity.IAngler;

public class EntityFishingSilverfish extends Entity_SpecialSilverfish implements IAngler {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "silverfish/fishing.png" )
    };
    
    /// Ticks until this fishing silverfish can cast its lure again.
    public int rodTime = 0;
    /// This fishing silverfish's lure entity.
    private EntitySpecialFishHook fishHook = null;
    
    public EntityFishingSilverfish( World world ) {
        super( world );
        setSize( 0.4F, 0.8F );
        getSpecialData().setTextures( EntityFishingSilverfish.TEXTURES );
        getSpecialData().resetRenderScale( 1.2F );
        experienceValue += 2;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 0.9 );
    }
    
    /// Set/get functions for this angler's fishHook.
    @Override
    /// IAngler
    public void setFishHook( EntitySpecialFishHook hook ) {
        fishHook = hook;
    }
    
    @Override
    /// IAngler
    public EntitySpecialFishHook getFishHook() {
        return fishHook;
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( rodTime > 0 ) {
            rodTime--;
        }
        super.onLivingUpdate();
    }
    
    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity( Entity target, float distance ) {
        super.attackEntity( target, distance );
        if( !worldObj.isRemote && rodTime <= 0 ) {
            if( distance > 3.0F && distance < 10.0F ) {
                new EntitySpecialFishHook( worldObj, this, target );
                worldObj.spawnEntityInWorld( getFishHook() );
                worldObj.playSoundAtEntity( this, "random.bow", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F) );
                rodTime = rand.nextInt( 21 ) + 32;
            }
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.fish, 1 );
        }
    }
}