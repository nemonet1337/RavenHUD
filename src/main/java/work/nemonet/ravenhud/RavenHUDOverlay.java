package work.nemonet.ravenhud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class RavenHUDOverlay {

    private double lastX = 0, lastY = 0, lastZ = 0;
    private double currentSpeed = 0;

    public record HudColors(
        int normal, int warning, int alert, int number,
        int iron, int gold, int diamond, int lava
    ) {}

    public record ArmorStatus(ItemStack stack, EquipmentSlot slot) {
        public boolean isEmpty() { return stack.isEmpty(); }
    }

    private final RavenModeHud ravenHud  = new RavenModeHud();
    private final TypeBHud  typeBHud  = new TypeBHud();
    private final AcvHud    acvHud    = new AcvHud();
    private final AC6Hud    ac6Hud    = new AC6Hud();

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !Config.HUD_ENABLED.get()) return;
        if (mc.options.hideGui) return;

        updateSpeed(player);

        int width  = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        double midX = width  / 2.0;
        double midY = height / 2.0;

        HudColors colors = resolveColors();
        float lineR     = ((colors.normal() >> 16) & 0xFF) / 255.0F;
        float lineG     = ((colors.normal() >>  8) & 0xFF) / 255.0F;
        float lineB     = ( colors.normal()        & 0xFF) / 255.0F;
        float lineAlpha = Config.LINE_ALPHA.get().floatValue();

        switch (Config.HUD_MODE.get()) {
            case RAVEN  -> ravenHud.render(guiGraphics, player, mc, width, height, midX, midY,
                                           colors, lineR, lineG, lineB, lineAlpha, currentSpeed);
            case TYPE_B -> typeBHud.render(guiGraphics, player, mc, width, height, midX, midY,
                                           colors, lineR, lineG, lineB, lineAlpha, currentSpeed);
            case ACV    -> acvHud.render(guiGraphics, player, mc, width, height, midX, midY,
                                         colors, lineR, lineG, lineB, lineAlpha, currentSpeed);
            case AC6    -> ac6Hud.render(guiGraphics, player, mc, width, height, midX, midY,
                                         colors, lineAlpha, currentSpeed);
        }
    }

    private void updateSpeed(LocalPlayer player) {
        double dx = player.getX() - lastX;
        double dy = player.getY() - lastY;
        double dz = player.getZ() - lastZ;
        double calculated = Math.sqrt(dx*dx + dy*dy + dz*dz) * 100.0D;
        currentSpeed = (calculated < 500.0D) ? currentSpeed * 0.8D + calculated * 0.2D : 0;
        lastX = player.getX(); lastY = player.getY(); lastZ = player.getZ();
    }

    private HudColors resolveColors() {
        Config.ColorPreset preset = Config.COLOR_PRESET.get();
        if (preset == Config.ColorPreset.CUSTOM) {
            return new HudColors(
                parseColor(Config.COLOR_NORMAL.get(),  0xC7FFAE00),
                parseColor(Config.COLOR_WARNING.get(), 0xE5FFFF00),
                parseColor(Config.COLOR_ALERT.get(),   0xE5FF0000),
                parseColor(Config.COLOR_NUMBER.get(),  0xCCD09000),
                parseColor(Config.COLOR_IRON.get(),    0x807050),
                parseColor(Config.COLOR_GOLD.get(),    0x999000),
                parseColor(Config.COLOR_DIAMOND.get(), 0x009999),
                parseColor(Config.COLOR_LAVA.get(),    0xFF3000)
            );
        }
        return switch (preset) {
            case ORANGE -> new HudColors(0xC7FFAE00, 0xE5FFFF00, 0xE5FF0000, 0xCCD09000, 0x807050, 0x999000, 0x009999, 0xFF3000);
            case CYAN   -> new HudColors(0xCC00FFD8, 0xE5FFFF00, 0xE5FF0000, 0xCC00FFD8, 0x807050, 0x999000, 0x009999, 0xFF3000);
            case GREEN  -> new HudColors(0xCC60FFC4, 0xE5FFFF00, 0xE5FF0000, 0xCC00FFD8, 0x807050, 0x999000, 0x009999, 0xFF3000);
            case WHITE  -> new HudColors(0xCCE0E0E0, 0xE5FFAA00, 0xE5FF3333, 0xCCE0E0E0, 0x807050, 0x999000, 0x009999, 0xFF3000);
            default     -> new HudColors(0, 0, 0, 0, 0, 0, 0, 0);
        };
    }

    static int parseColor(String hex, int defaultColor) {
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }
}
