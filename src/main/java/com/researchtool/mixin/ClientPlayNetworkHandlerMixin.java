package com.researchtool.mixin;

import com.researchtool.ResearchToolMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional system-message replacement for cleaner experiment logs.
 */
@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void researchtool$replacePermissionMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!ResearchToolMod.config().replacePermissionMessage) return;
        String s = packet.content().getString();
        if (!s.toLowerCase().contains("do not have permission")) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[ResearchTool] Команда недоступна в учебном режиме."), false);
        }
        ci.cancel();
    }
}
