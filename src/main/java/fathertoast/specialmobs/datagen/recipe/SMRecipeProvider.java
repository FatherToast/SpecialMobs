package fathertoast.specialmobs.datagen.recipe;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.fluids.PotionFluidType;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipeBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SMRecipeProvider extends RecipeProvider {
    
    public SMRecipeProvider( PackOutput packOutput ) {
        super( packOutput );
    }
    
    @Override
    protected void buildRecipes( Consumer<FinishedRecipe> saver ) {
        // Tinkers compat
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.BLAZES ), TinkerFluids.blazingBlood.get(), 20, 2,
                saver, "blazing_blood_from_all_blazes" );
        
        tinkersEntityMelting( EntityIngredient.of( MobFamily.CREEPER.vanillaReplacement.entityType.get() ), TinkerFluids.moltenGlass.get(), 50, 2,
                saver, "molten_glass_from_special_creeper" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.DROWNED ), TinkerFluids.moltenCopper.get(), 10, 4,
                saver, "molten_copper_from_all_drowned" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.ENDERMEN ), TinkerFluids.moltenEnder.get(), 25, 2,
                saver, "molten_ender_from_all_endermen" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.MAGMA_CUBES ), TinkerFluids.magma.get(), 25, 2,
                saver, "magma_from_all_magma_cubes" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.ZOMBIFIED_PIGLINS ), TinkerFluids.moltenGold.get(), 10, 4,
                saver, "molten_gold_from_all_zombified_piglins" );
        
        tinkersEntityMeltingPotion( EntityIngredient.of( MobFamily.GHAST.vanillaReplacement.entityType.get() ), Potions.REGENERATION, FluidValues.BOTTLE / 5, 2,
                saver, "regeneration_potion_from_special_ghast" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.SILVERFISH ), TinkerFluids.searedStone.get(), 50, 2,
                saver, "seared_stone_from_all_silverfish" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.SLIMES ), TinkerFluids.earthSlime.get(), 25, 2,
                saver, "slime_from_all_slimes" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.SPIDERS ), TinkerFluids.venom.get(), 25, 2,
                saver, "venom_from_all_spiders" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.CAVE_SPIDERS ), TinkerFluids.venom.get(), 25, 2,
                saver, "venom_from_all_cave_spiders" );
        
        tinkersEntityMelting( EntityIngredient.of( SMTags.EntityTypes.ZOMBIES ), TinkerFluids.moltenIron.get(), 10, 4,
                saver, "molten_iron_from_all_zombies" );
    }
    
    /** Generates a conditional recipe that only loads if the specified mod is loaded. */
    private static void conditionalModRecipe( String modId, RecipeBuilder recipe, RecipeCategory category, Consumer<FinishedRecipe> saver, @Nullable String recipeName ) {
        ResourceLocation recipeId = recipeName == null
                ? SpecialMobs.rl( "conditional/" + RecipeBuilder.getDefaultRecipeId( recipe.getResult() ).getPath() )
                : SpecialMobs.rl( "conditional/" + recipeName );
        
        ConditionalRecipe.builder()
                .addCondition( new ModLoadedCondition( modId ) )
                .addRecipe( conditionalSaver -> recipe.save( conditionalSaver, recipeId ) )
                .generateAdvancement( ResourceLocation.fromNamespaceAndPath( recipeId.getNamespace(), "recipes/" + category.getFolderName() + "/" + recipeId.getPath() ) )
                .build( saver, recipeId );
    }
    
    /** Helper method for generating conditional recipes that utilize special recipe builders from Tinker's. */
    private static void tinkersRecipe( AbstractRecipeBuilder<?> recipe, RegistryObject<?> recipeType, Consumer<FinishedRecipe> saver, String recipeName ) {
        ResourceLocation recipeId = SpecialMobs.rl( "conditional/" + Namespaces.T_CONSTRUCT + "/" + recipeType.getId().getPath() + "/" + recipeName );
        
        ConditionalRecipe.builder()
                .addCondition( new ModLoadedCondition( Namespaces.T_CONSTRUCT ) )
                .addRecipe( conditionalSaver -> recipe.save( conditionalSaver, recipeId ) )
                .generateAdvancement( ResourceLocation.fromNamespaceAndPath( recipeId.getNamespace(), "recipes/" + recipeId.getPath() ) )
                .build( saver, recipeId );
    }
    
    /** Helper method for generating entity-melting recipes for Tinker's. */
    private static void tinkersEntityMelting( EntityIngredient ingredient, Fluid fluid, int amount, int damage, Consumer<FinishedRecipe> saver, String recipeName ) {
        tinkersRecipe( EntityMeltingRecipeBuilder.melting( ingredient, new FluidStack( fluid, amount ), damage ),
                TinkerRecipeTypes.ENTITY_MELTING, saver, recipeName );
    }
    
    private static void tinkersEntityMeltingPotion( EntityIngredient ingredient, Potion potion, int amount, int damage, Consumer<FinishedRecipe> saver, String recipeName ) {
        tinkersRecipe( EntityMeltingRecipeBuilder.melting( ingredient, PotionFluidType.potionResult( potion, amount ), damage ),
                TinkerRecipeTypes.ENTITY_MELTING, saver, recipeName );
    }
    
    static class Namespaces {
        
        public static final String T_CONSTRUCT = "tconstruct";
    }
}
