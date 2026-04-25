package com.researchtool.mixin;

import com.researchtool.ResearchToolMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents accidental creative inventory writes during experiments.
 */
@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void researchtool$blockCreativePacket(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (!ResearchToolMod.config().creativeProtectionEnabled) return;
        if (!(packet instanceof CreativeInventoryActionC2SPacket)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§cИсследовательский мод: взятие предметов из креатива отключено"), false);
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1.0f));
        }
        ci.cancel();
    }
}
