package com.researchtool.teleport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Teleport study workflow for local testing.
 */
public final class TeleportStudyHandler {
    private int localMoveTicks = -1;
    private Vec3d localMoveStart;

    public void sendTeleportRequest(MinecraftClient client, double x, double y, double z) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        String command = "tp %.2f %.2f %.2f".formatted(x, y, z);
        client.getNetworkHandler().sendPacket(new ChatCommandC2SPacket(command));
        client.player.sendMessage(Text.literal("[ResearchTool] Отправлен запрос телепортации для анализа прав: /" + command), false);
        teleportFx(client);
    }

    public void simulateTimerFailure(MinecraftClient client) {
        if (client.player == null) return;
        client.player.sendMessage(Text.literal("[ResearchTool] Запрос телепортации обрабатывается 3 секунды…"), false);
        teleportFx(client);
        new Thread(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException ignored) {
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendMessage(Text.literal("[ResearchTool] Ошибка: недостаточно прав."), false);
                }
            });
        }, "researchtool-teleport-timer").start();
    }

    public void localMoveAndObserveRollback(MinecraftClient client, double x, double y, double z) {
        if (client.player == null) return;
        localMoveStart = client.player.getPos();
        client.player.setPosition(x, y, z);
        localMoveTicks = 0;
        client.player.sendMessage(Text.literal("[ResearchTool] Локальное перемещение выполнено. Ожидание серверной коррекции…"), false);
        teleportFx(client);
    }

    public void onClientTick(MinecraftClient client) {
        if (localMoveTicks < 0 || client.player == null || localMoveStart == null) return;
        localMoveTicks++;
        // If server corrected position close to start, report a rollback.
        if (client.player.getPos().distanceTo(localMoveStart) < 1.0) {
            client.player.sendMessage(Text.literal("[ResearchTool] Коррекция позиции сервером через " + localMoveTicks + " тиков."), false);
            localMoveTicks = -1;
            localMoveStart = null;
            return;
        }
        if (localMoveTicks > 200) {
            client.player.sendMessage(Text.literal("[ResearchTool] Коррекция не зафиксирована в течение 200 тиков."), false);
            localMoveTicks = -1;
            localMoveStart = null;
        }
    }

    private void teleportFx(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f));
        for (int i = 0; i < 20; i++) {
            client.world.addParticle(ParticleTypes.PORTAL,
                    client.player.getX(),
                    client.player.getBodyY(0.5),
                    client.player.getZ(),
                    (client.world.random.nextDouble() - 0.5) * 0.25,
                    client.world.random.nextDouble() * 0.2,
                    (client.world.random.nextDouble() - 0.5) * 0.25);
        }
    }
}
