package dev.abidux.enhancedcampfire.mixin;

import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CampfireBlock.class)
public interface CampfireBlockAccessor {
    @Accessor("fireDamage")
    int getFireDamage();
}
