package com.researchtool.gui;

import com.researchtool.ResearchToolMod;
import com.researchtool.movement.MovementExperimentHandler;
import com.researchtool.teleport.TeleportStudyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Local research dashboard. All actions are informational or safe client-only simulations.
 */
public final class ResearchGui extends Screen {
    private static final String[] TABS = {"Игроки", "Мир (локально)", "Экспериментальное движение", "Исследование телепортации", "Настройки мода"};

    private final MinecraftClient client;
    private final MovementExperimentHandler movement;
    private final TeleportStudyHandler teleport;

    private int selectedTab = 0;
    private String teleportTarget = "";
    private double tx;
    private double ty;
    private double tz;

    public ResearchGui(MinecraftClient client, MovementExperimentHandler movement, TeleportStudyHandler teleport) {
        super(Text.literal("Research Tool"));
        this.client = client;
        this.movement = movement;
        this.teleport = teleport;
    }

    @Override
    protected void init() {
        clearChildren();
        int tabX = 10;
        for (int i = 0; i < TABS.length; i++) {
            int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(TABS[i]), b -> {
                selectedTab = idx;
                ResearchToolMod.playUiClick(client);
                init();
            }).dimensions(tabX, 10, 130, 20).build());
            tabX += 132;
        }

        switch (selectedTab) {
            case 0 -> initPlayersTab();
            case 1 -> initWorldTab();
            case 2 -> initMovementTab();
            case 3 -> initTeleportTab();
            case 4 -> initSettingsTab();
            default -> {
            }
        }
    }

    private void initPlayersTab() {
        if (client.getNetworkHandler() == null || client.player == null) return;
        List<PlayerListEntry> players = new ArrayList<>(client.getNetworkHandler().getPlayerList());
        players.sort(Comparator.comparing(entry -> entry.getProfile().getName()));

        int y = 40;
        for (PlayerListEntry entry : players) {
            String name = entry.getProfile().getName();
            addDrawableChild(ButtonWidget.builder(Text.literal("Мут " + name), b -> simulateMute(name))
                    .dimensions(20, y, 140, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Кик " + name), b -> simulateKick(name))
                    .dimensions(165, y, 140, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("ТП анализ -> " + name), b -> {
                teleportTarget = name;
                selectedTab = 3;
                ResearchToolMod.playUiClick(client);
                init();
            }).dimensions(310, y, 180, 20).build());
            y += 24;
            if (y > height - 30) break;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Радар игроков"), b -> postRadarReport()).dimensions(20, height - 28, 160, 20).build());
    }

    private void initWorldTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Дождь (локально)"), b -> fakeWeather("rain")).dimensions(20, 44, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Ясно (локально)"), b -> fakeWeather("clear")).dimensions(190, 44, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Гроза (локально)"), b -> fakeWeather("thunder")).dimensions(360, 44, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Время: Рассвет"), b -> fakeTime(0L)).dimensions(20, 70, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Время: Полдень"), b -> fakeTime(6000L)).dimensions(190, 70, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Время: Ночь"), b -> fakeTime(13000L)).dimensions(360, 70, 160, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Симуляция рестарта"), b -> simulateRestartAlert()).dimensions(20, 100, 220, 20).build());
    }

    private void initMovementTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal(movement.isEnabled() ? "Выключить эксперимент" : "Включить эксперимент"), b -> {
            movement.toggle();
            ResearchToolMod.playUiClick(client);
            init();
        }).dimensions(20, 44, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Имитация легит-падений: Вкл/Выкл"), b -> {
            movement.setLegitFallPulse(true);
            message("[ResearchTool] Имитация легитимных падений включена.");
        }).dimensions(20, 70, 260, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Открыть локальный лог"), b -> message("Лог: logs/researchtool-movement.log"))
                .dimensions(20, 96, 220, 20).build());
    }

    private void initTeleportTab() {
        if (client.player != null) {
            tx = client.player.getX();
            ty = client.player.getY();
            tz = client.player.getZ();
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Отправить запрос телепортации"), b -> teleport.sendTeleportRequest(client, tx, ty, tz))
                .dimensions(20, 44, 240, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Локальное перемещение + откат"), b -> teleport.localMoveAndObserveRollback(client, tx, ty, tz))
                .dimensions(20, 70, 240, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Симуляция таймера"), b -> teleport.simulateTimerFailure(client))
                .dimensions(20, 96, 240, 20).build());
    }

    private void initSettingsTab() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Справка: /fa, /fagive, /faconsole"), b -> {
        }).dimensions(20, 44, 320, 20).build());
    }

    private void simulateMute(String playerName) {
        message("[Server] You muted " + playerName + " for 10 min.");
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
    }

    private void simulateKick(String playerName) {
        message("[Server] " + playerName + " was kicked by an operator.");
    }

    private void postRadarReport() {
        if (client.world == null || client.player == null) return;
        message("[ResearchTool] Radar snapshot:");
        for (PlayerEntity player : client.world.getPlayers()) {
            message(" - " + player.getName().getString() + " x=" + (int) player.getX() + " y=" + (int) player.getY() + " z=" + (int) player.getZ() + " hp=" + player.getHealth());
        }
    }

    private void fakeWeather(String type) {
        if (client.world == null) return;
        switch (type) {
            case "rain" -> {
                client.world.setRainGradient(1.0f);
                client.world.setThunderGradient(0.0f);
            }
            case "clear" -> {
                client.world.setRainGradient(0.0f);
                client.world.setThunderGradient(0.0f);
            }
            case "thunder" -> {
                client.world.setRainGradient(1.0f);
                client.world.setThunderGradient(1.0f);
            }
            default -> {
            }
        }
        message("[ResearchTool] Локальная погода: " + type);
    }

    private void fakeTime(long time) {
        if (client.world == null) return;
        client.world.setTimeOfDay(time);
        message("[ResearchTool] Локальное время изменено: " + time);
    }

    private void simulateRestartAlert() {
        message("[Server] Restart simulation started at T+00:00");
        message("[Server] Restart simulation T+00:30");
        message("[Server] Restart simulation T+01:00");
        message("[Server] Simulation ended (no real restart). ");
    }

    private void message(String content) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(content), false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawText(textRenderer, "Цель ТП: " + (teleportTarget.isEmpty() ? "(не выбрана)" : teleportTarget), 20, height - 46, 0xCCCCCC, false);
        context.drawText(textRenderer, "Координаты ТП: X=%.1f Y=%.1f Z=%.1f".formatted(tx, ty, tz), 20, height - 34, 0xCCCCCC, false);
    }
}
