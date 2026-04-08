package dk.thedemonprince.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("signplate");
    private static ConfigManager instance;
    private final File configFile;
    private final Gson gson;
    private ConfigData configData;

    private ConfigManager() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = configDir.resolve("signplate.json").toFile();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                configData = gson.fromJson(reader, ConfigData.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load config", e);
                configData = new ConfigData();
            }
        } else {
            configData = new ConfigData();
            save();
        }
    }

    public synchronized void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public ConfigData getConfig() {
        return configData;
    }

    public static class ConfigData {
        public boolean signPlateEnabled = false;
        public int selectedSignTemplate = -1;
        public List<SignTemplate> signTemplates = new ArrayList<>();
    }

    public static class SignTemplate {
        public String name;
        public String line1;
        public String line2;
        public String line3;
        public String line4;

        public SignTemplate(String name, String line1, String line2, String line3, String line4) {
            this.name = name;
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
            this.line4 = line4;
        }
    }
}
