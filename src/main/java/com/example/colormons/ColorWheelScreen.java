package com.example.colormons;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColorWheelScreen extends Screen {
    private static final int WHEEL = 140;
    private static ResourceLocation wheelTex;

    private final UUID monId;
    private final String species;
    private final PokemonEntity entity;
    private final MonColors mc;
    private final List<int[]> history = new ArrayList<>();

    private int selected = 0;
    private float brightness = 1f;
    private boolean editing = false;
    private float spin = 0f;

    private int wx, wy, slotX, slotY, mapX, mapY, paintX, paintY;

    public ColorWheelScreen(PokemonEntity mon) {
        super(Component.literal(mon.getPokemon().getSpecies().getName() + " Colours"));
        this.entity = mon;
        this.monId = mon.getPokemon().getUuid();
        this.species = mon.getPokemon().getSpecies().getResourceIdentifier().getPath();
        this.mc = ColorStore.of(monId);
        for (int i = 0; i < MonColors.GROUPS; i++) {
            if (mc.colors[i] >= 0) { selected = i; break; }
        }
        PaintPanel.load(species);
    }

    private void snapshot() {
        history.add(mc.colors.clone());
        if (history.size() > 20) history.remove(0);
    }

    @Override
    protected void init() {
        if (wheelTex == null) buildWheel();

        int left = 20;
        wx = width / 2 - 30;
        wy = height / 2 - 90;
        slotX = width - 300;
        slotY = height / 2 - 110;
        mapX = width - 182;
        mapY = slotY;
        paintX = 24;
        paintY = height / 2 + 20;

        for (int i = 0; i < MonColors.GROUPS; i++) {
            final int g = i;
            addRenderableWidget(Button.builder(Component.literal(Palette.GROUP_NAMES[i] + "  x" + count(g)), b -> selected = g)
                    .bounds(slotX, slotY + i * 26, 110, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal(editing ? "Edit: ON" : "Edit"), b -> {
            editing = !editing;
            b.setMessage(Component.literal(editing ? "Edit: ON" : "Edit"));
        }).bounds(mapX, slotY + MonColors.GROUPS * 26 + 8, 60, 20).build());

        for (int i = 0; i < 4; i++) {
            final int f = i;
            addRenderableWidget(Button.builder(Component.literal(Palette.FINISH_NAMES[i]), b -> {
                snapshot();
                mc.finish = f;
                ColorStore.touched(monId);
            }).bounds(left + i * 74, height - 40, 70, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> snapshot())
                .bounds(left, height - 66, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Undo"), b -> {
            if (!history.isEmpty()) {
                System.arraycopy(history.remove(history.size() - 1), 0, mc.colors, 0, MonColors.GROUPS);
                ColorStore.touched(monId);
            }
        }).bounds(left + 74, height - 66, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            snapshot();
            mc.reset();
            ColorStore.clear(monId);
        }).bounds(left + 148, height - 66, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left + 222, height - 66, 70, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Brush " + (EditBrush.size + 1)), b -> {
            EditBrush.size = (EditBrush.size + 1) % 4;
            b.setMessage(Component.literal("Brush " + (EditBrush.size + 1)));
        }).bounds(paintX, height - 66, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Undo brush"), b -> {
            EditBrush.undo(mc, (EditBrush.size + 1) * 9);
            ColorStore.touched(monId);
        }).bounds(paintX + 74, height - 66, 84, 20).build());

        for (int i = 0; i < 4; i++) {
            final int s = i;
            addRenderableWidget(Button.builder(Component.literal("S" + (i + 1)), b -> {
                if (isSaved(s)) {
                    snapshot();
                    System.arraycopy(ColorStore.SAVED[s], 0, mc.colors, 0, MonColors.GROUPS);
                    ColorStore.touched(monId);
                } else {
                    System.arraycopy(mc.colors, 0, ColorStore.SAVED[s], 0, MonColors.GROUPS);
                    ColorStore.save();
                }
            }).bounds(width - 40, 20 + i * 22, 26, 18).build());
        }
    }

    private boolean isSaved(int slot) {
        for (int v : ColorStore.SAVED[slot]) if (v >= 0) return true;
        return false;
    }

    private int count(int group) {
        int n = GroupMaps.count(species, group);
        for (int i = 0; i < mc.editPixels.size(); i++) {
            int idx = mc.editPixels.get(i);
            if (mc.editGroups.get(i) == group) n++;
            else if (idx >= 0) { /* moved out of its old group */ }
        }
        return n;
    }

    private void buildWheel() {
        NativeImage img = new NativeImage(WHEEL, WHEEL, true);
        float r = WHEEL / 2f;
        for (int y = 0; y < WHEEL; y++) for (int x = 0; x < WHEEL; x++) {
            float dx = x - r, dy = y - r, d = (float) Math.sqrt(dx * dx + dy * dy) / r;
            if (d > 1f) { img.setPixelRGBA(x, y, 0); continue; }
            float hue = (float) ((Math.atan2(dy, dx) / (2 * Math.PI) + 1) % 1);
            int rgb = Color.HSBtoRGB(hue, d, 1f);
            int R2 = (rgb >> 16) & 0xFF, G = (rgb >> 8) & 0xFF, B = rgb & 0xFF;
            img.setPixelRGBA(x, y, 0xFF000000 | (B << 16) | (G << 8) | R2);
        }
        wheelTex = minecraft.getTextureManager().register("colormons/wheel", new DynamicTexture(img));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        // left panel: 3D preview of the real Pokemon
        int pvx = 20, pvy = 40, pvs = 120;
        g.fill(pvx - 2, pvy - 2, pvx + 164, pvy + 164, 0xAA000000);
        Preview.draw(g, entity, pvx + 82, pvy + 130, 46.0f, spin);

        // paint grid below the preview, only while Edit is on
        if (editing) {
            g.fill(paintX - 2, paintY - 2, paintX + 164, paintY + 164, 0xAA000000);
            PaintPanel.draw(g, mc, GroupMaps.groups(species), paintX, paintY);
            g.drawString(font, "painting into " + Palette.GROUP_NAMES[selected], paintX, paintY + 168, 0x00FF00);
        }

        // wheel plus brightness bar
        g.blit(wheelTex, wx, wy, 0, 0, WHEEL, WHEEL, WHEEL, WHEEL);
        int sx = wx + WHEEL + 6;
        for (int i = 0; i < WHEEL; i++) {
            int v = 255 - i * 255 / WHEEL;
            g.fill(sx, wy + i, sx + 10, wy + i + 1, 0xFF000000 | (v << 16) | (v << 8) | v);
        }
        int knob = wy + (int) ((1 - brightness) * WHEEL);
        g.fill(sx - 2, knob - 1, sx + 12, knob + 1, 0xFFFF0000);

        // group slots
        for (int i = 0; i < MonColors.GROUPS; i++) {
            int y = slotY + i * 26;
            if (i == selected) g.fill(slotX - 4, y - 2, slotX + 112, y + 22, 0xFFFFFF00);
            g.fill(slotX + 114, y, slotX + 134, y + 20, 0xFF000000 | Palette.shown(mc, i));
        }

        // hover glow: the group map beside the buttons, with the hovered group lit
        int hovered = groupUnderMouse(mouseX, mouseY);
        if (hovered >= 0) {
            GroupOverlay.draw(g, species, mapX, mapY, 150, hovered);
        } else {
            GroupOverlay.draw(g, species, mapX, mapY, 150, -1);
        }

        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        g.drawString(font, "hover a group to see it", mapX, mapY - 12, 0x888888);
    }

    private int groupUnderMouse(int mx, int my) {
        for (int i = 0; i < MonColors.GROUPS; i++) {
            int y = slotY + i * 26;
            if (my >= y && my <= y + 20 && mx >= slotX && mx <= slotX + 134) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (editing && !isOverButton((int) mx, (int) my)) {
            int[] px = PaintPanel.pixelAt(mx, my, paintX, paintY);
            if (px != null) { EditBrush.paint(mc, px[0], px[1], PaintPanel.texWidth(), PaintPanel.texHeight()); ColorStore.touched(monId); return true; }
        }
        return pick(mx, my) || super.mouseClicked(mx, my, b);
    }

    private boolean isOverButton(int mx, int my) {
        for (var child : children()) {
            if (child instanceof Button btn && btn.isMouseOver(mx, my)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (editing && !isOverButton((int) mx, (int) my)) {
            int[] px = PaintPanel.pixelAt(mx, my, paintX, paintY);
            if (px != null) { EditBrush.paint(mc, px[0], px[1], PaintPanel.texWidth(), PaintPanel.texHeight()); ColorStore.touched(monId); return true; }
        }
        return pick(mx, my) || super.mouseDragged(mx, my, b, dx, dy);
    }

    private boolean pick(double mx, double my) {
        float r = WHEEL / 2f;
        int sx = wx + WHEEL + 6;
        if (mx >= sx && mx <= sx + 10 && my >= wy && my <= wy + WHEEL) {
            brightness = 1f - (float) ((my - wy) / WHEEL);
            int c = Palette.shown(mc, selected);
            float[] hsb = Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
            mc.colors[selected] = Color.HSBtoRGB(hsb[0], hsb[1], brightness) & 0xFFFFFF;
            return true;
        }
        double dx = mx - (wx + r), dy = my - (wy + r), d = Math.sqrt(dx * dx + dy * dy) / r;
        if (d <= 1) {
            float hue = (float) ((Math.atan2(dy, dx) / (2 * Math.PI) + 1) % 1);
            mc.colors[selected] = Color.HSBtoRGB(hue, (float) d, brightness) & 0xFFFFFF;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        spin += 0.01f;
        super.tick();
    }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        ColorStore.touched(monId);
        return super.mouseReleased(mx, my, b);
    }

    @Override
    public void onClose() {
        ColorStore.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
