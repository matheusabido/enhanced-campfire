package dev.abidux.enhancedcampfire.mixin;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CampfireBlockEntity.class)
public interface CampfireBlockEntityAccessor {
    @Accessor("cookingTimes")
    public abstract int[] getCookingTimes();

    @Accessor("cookingTotalTimes")
    public abstract int[] getCookingTotalTimes();

    @Accessor("matchGetter")
    public abstract RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> getMatchGetter();
}