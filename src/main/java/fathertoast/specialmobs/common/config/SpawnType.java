package fathertoast.specialmobs.common.config;

import fathertoast.crust.api.config.common.file.TomlHelper;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.MobSpawnType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum SpawnType implements Supplier<MobSpawnType> {
    
    NATURAL( MobSpawnType.NATURAL ),
    CHUNK_GENERATION( MobSpawnType.CHUNK_GENERATION ),
    SPAWNER( MobSpawnType.SPAWNER ),
    STRUCTURE( MobSpawnType.STRUCTURE ),
    BREEDING( MobSpawnType.BREEDING ),
    MOB_SUMMONED( MobSpawnType.MOB_SUMMONED ),
    JOCKEY( MobSpawnType.JOCKEY ),
    EVENT( MobSpawnType.EVENT ),
    CONVERSION( MobSpawnType.CONVERSION ),
    REINFORCEMENT( MobSpawnType.REINFORCEMENT ),
    TRIGGERED( MobSpawnType.TRIGGERED ),
    BUCKET( MobSpawnType.BUCKET ),
    SPAWN_EGG( MobSpawnType.SPAWN_EGG ),
    COMMAND( MobSpawnType.COMMAND ),
    DISPENSER( MobSpawnType.DISPENSER ),
    PATROL( MobSpawnType.PATROL );
    
    static {
        // Do some error checking
        if( SharedConstants.IS_RUNNING_IN_IDE ) {
            MobSpawnType[] vanillaValues = MobSpawnType.values();
            SpawnType[] ourValues = values();
            if( vanillaValues.length != ourValues.length )
                throw new IllegalStateException( "Spawn Type has a mismatched number of values!" );
            
            for( int i = 0; i < vanillaValues.length; i++ ) {
                MobSpawnType vanillaValue = vanillaValues[i];
                SpawnType ourValue = ourValues[i];
                String vanillaName = TomlHelper.enumToString( vanillaValue );
                String ourName = TomlHelper.enumToString( ourValue );
                if( !vanillaName.equals( ourName ) )
                    throw new IllegalArgumentException( "Spawn Type \"" + ourName +
                            "\" does not match its vanilla type \"" + vanillaName + "\"!" );
                if( vanillaValue != ourValue.get() )
                    throw new IllegalArgumentException( "Spawn Type \"" + ourValue +
                            "\" references the wrong vanilla type \"" + TomlHelper.enumToString( ourValue.get() ) + "\"!" );
            }
        }
    }
    
    /** @return Attempts to get our spawn type from the vanilla spawn type, returns null if not present. */
    @Nullable
    public static SpawnType fromVanilla( @Nullable MobSpawnType type ) {
        if( type != null ) {
            for( SpawnType val : values() ) {
                if( val.toVanilla() == type ) return val;
            }
        }
        return null;
    }
    
    /** @return Attempts to parse the string literal as a spawn type and returns it, or null if invalid. */
    @Nullable
    public static SpawnType parseName( String name ) {
        for( SpawnType val : values() ) {
            if( val.name().equalsIgnoreCase( name ) ) return val;
        }
        return null;
    }
    
    /** @return True if the string literal is a valid spawn type. */
    public static boolean isValid( String name ) { return parseName( name ) != null; }
    
    private final MobSpawnType underlyingType;
    
    SpawnType( MobSpawnType wrapped ) { underlyingType = wrapped; }
    
    /** @return The underlying vanilla spawn type. */
    public MobSpawnType toVanilla() { return underlyingType; }
    
    /** @return The underlying vanilla spawn type. */
    @Override
    public MobSpawnType get() { return toVanilla(); }
    
    /** @return This value, converted to a single-line string. */
    @Override
    public String toString() { return TomlHelper.enumToString( this ); }
}