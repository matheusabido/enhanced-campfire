package dev.abidux.enhancedcampfire.handler;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CampfireItemHandler implements IItemHandler {

    private final CampfireBlockEntity entity;
    public CampfireItemHandler(CampfireBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public int getSlots() {
        return 4;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return entity.getItems().get(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack item = stack.copy();
        if (entity.getLevel() == null) return item;

        RecipeHolder<CampfireCookingRecipe> recipeHolder = entity.quickCheck.getRecipeFor(new SimpleContainer(item), entity.getLevel()).orElse(null);
        if (recipeHolder == null) return item; // recipe not registered

        if (simulate) {
            for (ItemStack current : entity.getItems()) {
                if (current.isEmpty())
                    return item.split(item.getCount() - 1); // can insert item
            }
            return item; // insufficient space
        }

        CampfireCookingRecipe recipe = recipeHolder.value();
        if (!entity.placeFood(null, stack, recipe.getCookingTime()))
            return item; // insufficient space

        item.shrink(1);
        return item.isEmpty() ? ItemStack.EMPTY : item; // return the item accepted
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return entity.getLevel() != null && entity.quickCheck.getRecipeFor(new SimpleContainer(stack), entity.getLevel()).isPresent();
    }
}
