package com.example.colormons;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** One Pokemon's palette, sent both ways so every player sees the same colours. */
public record ColormonsNet(String uuid, int[] colors, int finish, int[] pixels, int[] pixelGroups)
        implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("colormons", "palette");
    public static final CustomPacketPayload.Type<ColormonsNet> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ColormonsNet> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ColormonsNet::uuid,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(
                    list -> list.stream().mapToInt(Integer::intValue).toArray(),
                    arr -> java.util.Arrays.stream(arr).boxed().toList()),
            ColormonsNet::colors,
            ByteBufCodecs.VAR_INT, ColormonsNet::finish,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(
                    list -> list.stream().mapToInt(Integer::intValue).toArray(),
                    arr -> java.util.Arrays.stream(arr).boxed().toList()),
            ColormonsNet::pixels,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(
                    list -> list.stream().mapToInt(Integer::intValue).toArray(),
                    arr -> java.util.Arrays.stream(arr).boxed().toList()),
            ColormonsNet::pixelGroups,
            ColormonsNet::new);
    @Override
    public Type<ColormonsNet> type() { return TYPE; }

    public static ColormonsNet from(String uuid, MonColors mc) {
        int[] px = new int[mc.editPixels.size()];
        int[] pg = new int[mc.editGroups.size()];
        for (int i = 0; i < px.length; i++) px[i] = mc.editPixels.get(i);
        for (int i = 0; i < pg.length; i++) pg[i] = mc.editGroups.get(i);
        return new ColormonsNet(uuid, mc.colors.clone(), mc.finish, px, pg);
    }

    public MonColors toColors() {
        MonColors mc = new MonColors();
        mc.colors = colors.clone();
        mc.finish = finish;
        for (int i = 0; i < pixels.length && i < pixelGroups.length; i++) {
            mc.editPixels.add(pixels[i]);
            mc.editGroups.add(pixelGroups[i]);
        }
        return mc;
    }
}
