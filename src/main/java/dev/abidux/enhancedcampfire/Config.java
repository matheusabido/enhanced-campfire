package dev.abidux.enhancedcampfire;

import java.io.*;
import java.util.stream.Collectors;

public class Config {

    public static boolean CAMPFIRE_KEEPS_ITEMS;
    public static boolean SOUL_CAMPFIRE_COOKS_FASTER;
    public static boolean CAMPFIRE_SUPPORT_HOPPERS;

    public static void loadConfig(File file) {
        if (!file.exists()) createConfig(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String raw = reader.lines().collect(Collectors.joining("\n"));
            CAMPFIRE_KEEPS_ITEMS = raw.split("CAMPFIRE_KEEPS_ITEMS")[1].split("\n")[0].contains("true");
            SOUL_CAMPFIRE_COOKS_FASTER = raw.split("SOUL_CAMPFIRE_COOKS_FASTER")[1].split("\n")[0].contains("true");
            CAMPFIRE_SUPPORT_HOPPERS = raw.split("CAMPFIRE_SUPPORT_HOPPERS")[1].split("\n")[0].contains("true");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void createConfig(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print("#Defines whether campfires will keep the cooked items or not.\n" +
                    "CAMPFIRE_KEEPS_ITEMS = true\n" +
                    "#Defines whether soul campfires will cook faster than normal campfires or not.\n" +
                    "SOUL_CAMPFIRE_COOKS_FASTER = true\n" +
                    "#Defines whether hoppers work with campfires or not.\n" +
                    "CAMPFIRE_SUPPORT_HOPPERS = true");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}