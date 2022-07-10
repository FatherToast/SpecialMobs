package fathertoast.specialmobs.common.entity.ai;

public interface IExplodingMob {
    /** Sets this exploding entity's swell direction. */
    void setSwellDir( int value );
    
    /** @return This exploding entity's swell direction. */
    int getSwellDir();
    
    /** @return Additional range from its target at which this entity will start to explode. */
    default double getExtraRange() { return 0.0; }
}