package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class GrapeSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GrapeSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xB333B3 );
        //TODO theme - mountain
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Grape Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.SLIME_BALL, 1 );
        loot.addUncommonDrop( "uncommon", Items.PURPLE_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<GrapeSlimeEntity> getVariantFactory() { return GrapeSlimeEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public GrapeSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setFallDamageMultiplier( 0.0F );
        slimeExperienceValue += 1;
    }
    
    /** Override to modify this slime's base attributes by size. */
    @Override
    protected void modifyVariantAttributes( int size ) {
        addAttribute( Attributes.MAX_HEALTH, 4.0 * size );
        multAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new SpecialLeapAtTargetGoal(
                this, 10, 6.0F, 12.0F, 1.1F, 2.6F ) );
    }
    
    private static final IParticleData JUMP_PARTICLE = new ItemParticleData( ParticleTypes.ITEM, Items.PURPLE_DYE.getDefaultInstance() );
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return JUMP_PARTICLE; }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "grape" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}