package dev.abidux.enhancedcampfire;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(EnhancedCampfireMod.MOD_ID)
public class EnhancedCampfireMod {
    public static final String MOD_ID = "enhancedcampfire";

    public EnhancedCampfireMod() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}