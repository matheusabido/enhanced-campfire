package dev.abidux.enhancedcampfire;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = EnhancedCampfireMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue CAMPFIRE_KEEP_ITEMS_FIELD = BUILDER.comment("Defines whether campfires will keep the cooked items or not.").define("CAMPFIRE_KEEPS_ITEMS", true);
    private static final ForgeConfigSpec.BooleanValue SOUL_CAMPFIRE_COOKS_FASTER_FIELD = BUILDER.comment("Defines whether soul campfires will cook faster than normal campfires or not.").define("SOUL_CAMPFIRE_COOKS_FASTER", true);
    private static final ForgeConfigSpec.BooleanValue CAMPFIRE_SUPPORT_HOPPERS_FIELD = BUILDER.comment("Defines whether hoppers work with campfires or not.").define("CAMPFIRE_SUPPORT_HOPPERS", true);
    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean CAMPFIRE_KEEPS_ITEMS;
    public static boolean SOUL_CAMPFIRE_COOKS_FASTER;
    public static boolean CAMPFIRE_SUPPORT_HOPPERS;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        CAMPFIRE_KEEPS_ITEMS = CAMPFIRE_KEEP_ITEMS_FIELD.get();
        SOUL_CAMPFIRE_COOKS_FASTER = SOUL_CAMPFIRE_COOKS_FASTER_FIELD.get();
        CAMPFIRE_SUPPORT_HOPPERS = CAMPFIRE_SUPPORT_HOPPERS_FIELD.get();
    }
}