package com.example.colormons;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

/**
 * Draws the Pokemon itself inside the GUI so the preview matches the world.
 * The panel is already laid out for it; if this build's GUI matrix state differs,
 * adjust the translate/scale here. Nothing else on the screen depends on it.
 */
public class Preview {

    public static void draw(GuiGraphics g, PokemonEntity entity, int x, int y, float size, float spin) {
        if (entity == null) return;
        Minecraft mc = Minecraft.getInstance();

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 100.0f);
        pose.scale(size, size, -size);
        pose.mulPose(new Quaternionf().rotateZ((float) Math.PI));
        pose.mulPose(new Quaternionf().rotateY(spin));

        Lighting.setupForEntityInInventory();
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        try {
            mc.getEntityRenderDispatcher().render(
                    entity,
                    0.0, 0.0, 0.0,
                    0.0f,
                    0.0f,
                    pose,
                    mc.renderBuffers().bufferSource(),
                    0xF000F0);
            mc.renderBuffers().bufferSource().endBatch();
        } catch (Throwable ignored) {
        } finally {
            mc.getEntityRenderDispatcher().setRenderShadow(true);
            pose.popPose();
        }
    }
}
