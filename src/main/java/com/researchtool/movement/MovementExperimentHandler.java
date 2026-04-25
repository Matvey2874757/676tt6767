package com.researchtool.movement;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Educational movement analyzer.
 *
 * Intentionally does not spoof abilities or force protocol-invalid packets. It only adjusts local velocity
 * in controlled bounds and logs values for comparison against server-side anti-cheat logs.
 */
public final class MovementExperimentHandler {
    private static final Path LOG_PATH = FabricLoader.getInstance().getGameDir().resolve("logs/researchtool-movement.log");

    private boolean enabled;
    private boolean legitFallPulse;
    private int tickCounter;

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public void setLegitFallPulse(boolean legitFallPulse) {
        this.legitFallPulse = legitFallPulse;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onClientTick(MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null) {
            return;
        }

        tickCounter++;
        PlayerEntity p = client.player;
        Vec3d v = p.getVelocity();

        boolean pulseTick = legitFallPulse && (tickCounter % 30 == 0);
        double yVelocity;
        if (pulseTick) {
            yVelocity = v.y; // restore natural velocity this tick for observation.
        } else {
            yVelocity = 0.0;
            if (client.options.jumpKey.isPressed()) {
                yVelocity = Math.min(0.2, yVelocity + 0.2);
            }
            if (client.options.sneakKey.isPressed()) {
                yVelocity = Math.max(-0.2, yVelocity - 0.2);
            }
        }

        yVelocity = Math.max(-1.0, Math.min(1.0, yVelocity));
        p.setVelocity(v.x, yVelocity, v.z);
        logTick(pulseTick, p);

        if (tickCounter % 100 == 0) {
            p.sendMessage(Text.literal("[ResearchTool] Movement sample collected: tick=" + tickCounter), true);
        }
    }

    private void logTick(boolean pulseTick, PlayerEntity p) {
        String line = "%s tick=%d pulse=%s x=%.3f y=%.3f z=%.3f velY=%.3f onGround=%s%n".formatted(
                Instant.now(),
                tickCounter,
                pulseTick,
                p.getX(), p.getY(), p.getZ(),
                p.getVelocity().y,
                p.isOnGround()
        );
        try {
            Files.createDirectories(LOG_PATH.getParent());
            Files.writeString(LOG_PATH, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
