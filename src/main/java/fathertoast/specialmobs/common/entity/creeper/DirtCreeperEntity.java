package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

@SpecialMob
public class DirtCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DirtCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x78553B )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).burnImmune()
                .addToAttribute( Attributes.ARMOR, 6.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Dirt Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Blocks.DIRT );
        loot.addSemicommonDrop( "semicommon", Items.BREAD );
        loot.addRareDrop( "rare", Items.CARROT, Items.POTATO );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DirtCreeperEntity> getVariantFactory() { return DirtCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DirtCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public DirtCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.Mode explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this, explosionPower, false, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        if( explosionMode == Explosion.Mode.NONE ) return;
        
        final BlockState dirt = Blocks.DIRT.defaultBlockState();
        final int radius = (int) Math.floor( explosionPower );
        final BlockPos center = new BlockPos( explosion.getPos() );
        
        for( int y = -radius; y <= radius; y++ ) {
            for( int x = -radius; x <= radius; x++ ) {
                for( int z = -radius; z <= radius; z++ ) {
                    if( x * x + y * y + z * z <= radius * radius ) {
                        final BlockPos pos = center.offset( x, y, z );
                        if( level.getBlockState( pos ).getMaterial().isReplaceable() ) {
                            MobHelper.placeBlock( this, pos, dirt );
                        }
                    }
                }
            }
        }
    }
}