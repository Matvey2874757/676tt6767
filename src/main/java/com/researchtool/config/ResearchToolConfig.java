package com.researchtool.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON config with feature-level toggles so all research modules can be disabled independently.
 */
public final class ResearchToolConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("researchtool.json");

    public boolean guiEnabled = true;
    public boolean playersTabEnabled = true;
    public boolean worldTabEnabled = true;
    public boolean movementEnabled = true;
    public boolean teleportStudyEnabled = true;
    public boolean creativeProtectionEnabled = true;
    public boolean localChatFilterEnabled = false;
    public boolean replacePermissionMessage = true;
    public boolean allowCommandTeleportRequest = true;

    public static ResearchToolConfig load() {
        try {
            if (Files.notExists(PATH)) {
                ResearchToolConfig cfg = new ResearchToolConfig();
                cfg.save();
                return cfg;
            }
            return GSON.fromJson(Files.readString(PATH), ResearchToolConfig.class);
        } catch (Exception e) {
            return new ResearchToolConfig();
        }
    }

    public void save() throws IOException {
        Files.createDirectories(PATH.getParent());
        Files.writeString(PATH, GSON.toJson(this));
    }
}
