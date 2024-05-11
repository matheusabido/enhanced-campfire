package dev.abidux.enhancedcampfire.mixin;

import dev.abidux.enhancedcampfire.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements SidedInventory {
    public CampfireBlockEntityMixin(BlockPos pos, BlockState state) {
        super(BlockEntityType.CAMPFIRE, pos, state);
    }

    @Inject(method = "litServerTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/CampfireBlockEntity;)V", at = @At("HEAD"))
    private static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        CampfireBlockEntityMixin mixin = (CampfireBlockEntityMixin)(Object)campfire;

        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor)campfire;
        int[] cookingTimes = accessor.getCookingTimes();
        for (int i = 0; i < cookingTimes.length; i++) {
            if (cookingTimes[i] == -1) {
                cookingTimes[i] = -2;
                continue;
            }
            applySpeedModifierIfApplicable(mixin, i);

            ItemStack item = campfire.getItemsBeingCooked().get(i).copy();
            if (item.isEmpty()) continue;

            boolean isItemReady = cookingTimes[i] + 1 >= accessor.getCookingTotalTimes()[i];
            if (!isItemReady) continue;

            CampfireCookingRecipe recipe = accessor.getMatchGetter().getFirstMatch(new SimpleInventory(item), world).orElse(null);
            if (recipe == null) continue;
            ItemStack resultItem = recipe.getOutput(null).copy();

            if (Config.CAMPFIRE_SUPPORT_HOPPERS && handleHopper(campfire, resultItem, i))
                return;

            if (Config.CAMPFIRE_KEEPS_ITEMS) {
                cookingTimes[i] = -2;
                campfire.getItemsBeingCooked().set(i, resultItem);
                world.updateListeners(pos, state, state, 3);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            }
        }
    }

    private static boolean handleHopper(CampfireBlockEntity campfire, ItemStack item, int fromSlot) {
        World world = campfire.getWorld();
        if (world == null) return false;

        BlockState state = campfire.getCachedState();
        BlockPos pos = campfire.getPos();

        if (!(world.getBlockEntity(pos.down()) instanceof Hopper hopper)) return false;
        for (int i = 0; i < hopper.size(); i++) {
            ItemStack stack = hopper.getStack(i);
            if (!stack.isEmpty() && !ItemStack.canCombine(stack, item)) continue;

            int fit = stack.isEmpty() ? item.getMaxCount() : item.getMaxCount() - stack.getCount();
            int inserted = Math.min(fit, item.getCount());
            if (inserted == 0) continue;

            ItemStack insertedItem = item.split(inserted).copyWithCount(stack.getCount() + inserted);
            hopper.setStack(i, insertedItem);
            campfire.getItemsBeingCooked().set(fromSlot, ItemStack.EMPTY);
            world.updateListeners(pos, state, state, 3);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            return true;
        }
        return false;
    }

    private static void applySpeedModifierIfApplicable(CampfireBlockEntityMixin mixin, int i) {
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor)mixin;
        if (!Config.SOUL_CAMPFIRE_COOKS_FASTER || accessor.getCookingTimes()[i] > 0) return;
        CampfireBlockAccessor block = (CampfireBlockAccessor)mixin.getCachedState().getBlock();
        accessor.getCookingTotalTimes()[i] /= block.getFireDamage();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor)entity;
        return Config.CAMPFIRE_SUPPORT_HOPPERS && !entity.getItemsBeingCooked().get(slot).isEmpty() && accessor.getCookingTimes()[slot] < 0;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor)entity;
        return Config.CAMPFIRE_SUPPORT_HOPPERS && entity.getItemsBeingCooked().get(slot).isEmpty() && accessor.getMatchGetter().getFirstMatch(new SimpleInventory(stack), this.world).isPresent();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] {0, 1, 2, 3};
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        for (ItemStack item : entity.getItemsBeingCooked()) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        return entity.getItemsBeingCooked().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        return Inventories.splitStack(entity.getItemsBeingCooked(), slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        return Inventories.removeStack(entity.getItemsBeingCooked(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor)entity;
        entity.addItem(null, stack, accessor.getMatchGetter().getFirstMatch(new SimpleInventory(stack), this.world).get().getCookTime());
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        CampfireBlockEntity entity = (CampfireBlockEntity)(Object)this;
        entity.getItemsBeingCooked().clear();
    }
}
