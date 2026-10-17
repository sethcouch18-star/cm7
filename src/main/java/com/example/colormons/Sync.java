package com.example.colormons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

/** Sends palettes to the server (which relays them) and applies palettes from other players. */
public class Sync {

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(ColormonsNet.TYPE, ColormonsNet.CODEC,
                (payload, context) -> context.enqueueWork(() -> apply(payload)));
    }

    /** Push my edit so everyone sees it. Does nothing in single player. */
    public static void broadcast(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        MonColors palette = ColorStore.of(id);
        if (palette.isEmpty()) return;
        try {
            PacketDistributor.sendToServer(ColormonsNet.from(id.toString(), palette));
        } catch (Throwable ignored) {}
    }

    private static void apply(ColormonsNet payload) {
        try {
            ColorStore.putRemote(UUID.fromString(payload.uuid()), payload.toColors());
        } catch (Exception ignored) {}
    }
}
