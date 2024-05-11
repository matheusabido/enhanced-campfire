package dev.abidux.enhancedcampfire;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class EnhancedCampfire implements ModInitializer {
	@Override
	public void onInitialize() {
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "enhancedcampfire.properties");
		Config.loadConfig(configFile);
	}
}