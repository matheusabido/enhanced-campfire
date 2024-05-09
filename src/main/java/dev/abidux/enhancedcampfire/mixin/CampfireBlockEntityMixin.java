package dev.abidux.enhancedcampfire.mixin;

import dev.abidux.enhancedcampfire.Config;
import dev.abidux.enhancedcampfire.handler.CampfireItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity {

    private LazyOptional<IItemHandler> lazyItemHandler;
    private final IItemHandler itemHandler = new CampfireItemHandler((CampfireBlockEntity)(Object)this);
    public CampfireBlockEntityMixin(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityType.CAMPFIRE, p_155229_, p_155230_);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Inject(method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V", at = @At("HEAD"))
    private static void cookTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity entity, CallbackInfo ci) {
        for (int i = 0; i < entity.cookingProgress.length; i++) {
            if (entity.cookingProgress[i] == -1) {
                entity.cookingProgress[i] = -2; // avoids that it grows infinitely
                continue;
            }
            applySpeedModifierIfApplicable(entity, i);

            ItemStack item = entity.getItems().get(i).copy();
            if (entity.getItems().get(i).isEmpty()) continue;

            boolean isItemReady = entity.cookingProgress[i] + 1 >= entity.cookingTime[i];
            if (!isItemReady) continue;

            RecipeHolder<CampfireCookingRecipe> recipeHolder = entity.quickCheck.getRecipeFor(new SimpleContainer(item), level).orElse(null);
            if (recipeHolder == null) continue; // might be impossible but what if
            CampfireCookingRecipe recipe = recipeHolder.value();
            ItemStack resultItem = recipe.getResultItem(null).copy(); // parameter seems not to change anything

            if (Config.CAMPFIRE_SUPPORT_HOPPERS && handleHopper(entity, level, resultItem, i))
                return;

            if (Config.CAMPFIRE_KEEPS_ITEMS) {
                entity.cookingProgress[i] = -2;
                entity.getItems().set(i, resultItem);
                level.sendBlockUpdated(pos, state, state, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                entity.setChanged();
            }
        }
    }

    private static boolean handleHopper(CampfireBlockEntity entity, Level level, ItemStack resultItem, int slot) {
        BlockState state = entity.getBlockState();
        BlockPos pos = entity.getBlockPos();

        BlockEntity below = level.getBlockEntity(pos.below());
        if (below == null) return false; // doesn't have a block entity below

        LazyOptional<IItemHandler> optionalHandler = below.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if (!optionalHandler.isPresent() || optionalHandler.resolve().isEmpty()) return false;
        IItemHandler handler = optionalHandler.resolve().get();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack remaining = handler.insertItem(i, resultItem, false);
            if (!remaining.isEmpty()) continue; // couldn't insert

            entity.getItems().set(slot, ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
            entity.setChanged();
            return true;
        }
        return false;
    }

    private static void applySpeedModifierIfApplicable(CampfireBlockEntity entity, int slot) {
        if (!Config.SOUL_CAMPFIRE_COOKS_FASTER || entity.cookingProgress[slot] > 0) return;
        int speedModifier = ((CampfireBlock)entity.getBlockState().getBlock()).fireDamage;
        entity.cookingTime[slot] /= speedModifier;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}
