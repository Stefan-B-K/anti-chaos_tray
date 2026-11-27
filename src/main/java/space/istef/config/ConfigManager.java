package space.istef.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final String APP_NAME = "Anti-Chaos_Tray";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path configDir;

    public ConfigManager() {
        this.configDir = determineConfigDir();
        ensureConfigDirExists();
    }

    private Path determineConfigDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                return Paths.get(appData, APP_NAME);
            }
            return Paths.get(userHome, "AppData", "Roaming", APP_NAME);
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            // Linux and others
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null && !xdgConfig.isEmpty()) {
                return Paths.get(xdgConfig, "anti-chaos-tray");
            }
            return Paths.get(userHome, ".config", "anti-chaos-tray");
        }
    }

    private void ensureConfigDirExists() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory: " + configDir, e);
        }
    }

    public Path getConfigDir() {
        return configDir;
    }

    public <T> T loadJson(String filename, Class<T> clazz) throws IOException {
        Path filePath = configDir.resolve(filename);
        if (!Files.exists(filePath)) {
            return null;
        }
        return objectMapper.readValue(filePath.toFile(), clazz);
    }

    public <T> void saveJson(String filename, T data) throws IOException {
        Path filePath = configDir.resolve(filename);
        objectMapper.writeValue(filePath.toFile(), data);
    }

    public boolean fileExists(String filename) {
        return Files.exists(configDir.resolve(filename));
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = configDir.resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}
