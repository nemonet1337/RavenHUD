package work.nemonet.ravenhud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.navigation.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class RavenHUDOverlay {

    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    private double currentSpeed = 0;

    // Record for holding HUD colors to avoid multiple parseColor calls and parameters
    public record HudColors(
        int normal,
        int warning,
        int alert,
        int number,
        int iron,
        int gold,
        int diamond,
        int lava
    ) {}

    // Record representing armor slots and stack
    public record ArmorStatus(
        ItemStack stack,
        EquipmentSlot slot
    ) {
        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !Config.HUD_ENABLED.get()) {
            return;
        }

        if (mc.options.hideGui) {
            return;
        }



        // 速度の更新
        updateSpeed(player);

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        double midX = width / 2.0;
        double midY = height / 2.0;

        // カラー値のパースと不変レコード化 (プリセット考慮)
        Config.ColorPreset preset = Config.COLOR_PRESET.get();
        HudColors colors;
        if (preset == Config.ColorPreset.CUSTOM) {
            colors = new HudColors(
                    parseColor(Config.COLOR_NORMAL.get(), 0xC7FFAE00),
                    parseColor(Config.COLOR_WARNING.get(), 0xE5FFFF00),
                    parseColor(Config.COLOR_ALERT.get(), 0xE5FF0000),
                    parseColor(Config.COLOR_NUMBER.get(), 0xCCD09000),
                    parseColor(Config.COLOR_IRON.get(), 0x807050),
                    parseColor(Config.COLOR_GOLD.get(), 0x999000),
                    parseColor(Config.COLOR_DIAMOND.get(), 0x009999),
                    parseColor(Config.COLOR_LAVA.get(), 0xFF3000)
            );
        } else {
            colors = switch (preset) {
                case ORANGE -> new HudColors(
                        0xC7FFAE00, // normal
                        0xE5FFFF00, // warning
                        0xE5FF0000, // alert
                        0xCCD09000, // number
                        0x807050,   // iron
                        0x999000,   // gold
                        0x009999,   // diamond
                        0xFF3000    // lava
                );
                case CYAN -> new HudColors(
                        0xCC00FFD8, // normal
                        0xE5FFFF00,
                        0xE5FF0000,
                        0xCC00FFD8,
                        0x807050,
                        0x999000,
                        0x009999,
                        0xFF3000
                );
                case GREEN -> new HudColors(
                        0xCC60FFC4, // normal (エメラルドグリーン)
                        0xE5FFFF00,
                        0xE5FF0000,
                        0xCC00FFD8, // number (水色)
                        0x807050,
                        0x999000,
                        0x009999,
                        0xFF3000
                );
                case WHITE -> new HudColors(
                        0xCCE0E0E0, // normal (明るいグレー/白)
                        0xE5FFAA00, // warning (オレンジ/イエロー)
                        0xE5FF3333, // alert (赤)
                        0xCCE0E0E0, // number
                        0x807050,
                        0x999000,
                        0x009999,
                        0xFF3000
                );
                default -> new HudColors(0, 0, 0, 0, 0, 0, 0, 0);
            };
        }

        float lineR = ((colors.normal() >> 16) & 0xFF) / 255.0F;
        float lineG = ((colors.normal() >> 8) & 0xFF) / 255.0F;
        float lineB = (colors.normal() & 0xFF) / 255.0F;
        float lineAlpha = Config.LINE_ALPHA.get().floatValue();

        Config.HudMode mode = Config.HUD_MODE.get();

        switch (mode) {
            case RAVEN -> renderRaven(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
            case TYPE_B -> renderTypeB(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
            case ACV -> renderACV(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
            case AC6 -> renderAC6(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
        }
    }

    private void updateSpeed(LocalPlayer player) {
        double dx = player.getX() - lastX;
        double dy = player.getY() - lastY;
        double dz = player.getZ() - lastZ;

        double calculated = Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0D;
        if (calculated < 500.0D) {
            currentSpeed = currentSpeed * 0.8D + calculated * 0.2D;
        } else {
            currentSpeed = 0;
        }

        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
    }

    private void renderRaven(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int w, int j, double d, double d1,
                             HudColors colors, float lineR, float lineG, float lineB, float lineAlpha) {

        int shadowColor = ((int) (0.3F * 255) << 24) |
                ((int) (lineR * 0.1F * 255) << 16) |
                ((int) (lineG * 0.1F * 255) << 8) |
                ((int) (lineB * 0.1F * 255));

        // Speed Numbers Frame Shadow
        drawPolygon(guiGraphics, shadowColor,
                15.0D, j - 52.0D,
                15.0D, j - 23.0D,
                65.0D, j - 23.0D,
                65.0D, j - 37.0D,
                50.0D, j - 52.0D);

        // Height Numbers Frame Shadow
        drawPolygon(guiGraphics, shadowColor,
                w - 55.0D, 130.0D,
                w - 55.0D, 142.0D,
                w - 42.0D, 155.0D,
                w - 10.0D, 155.0D,
                w - 10.0D, 130.0D);

        // Armor Frame Shadow
        drawPolygon(guiGraphics, shadowColor,
                15.0D, 15.0D,
                15.0D, 40.0D,
                85.0D, 40.0D,
                85.0D, 35.0D,
                65.0D, 15.0D);

        // Boost Frame Shadow
        guiGraphics.fill(3, 15, 13, j - 23, shadowColor);

        int frameColor = ((int) (lineAlpha * 255) << 24) | (colors.normal() & 0x00FFFFFF);

        // Speed Frame
        drawLineLoop(guiGraphics, frameColor,
                15.0D, j - 52.0D,
                50.0D, j - 52.0D,
                65.0D, j - 37.0D,
                65.0D, j - 23.0D,
                15.0D, j - 23.0D);

        // Height Frame
        drawLineLoop(guiGraphics, frameColor,
                w - 55.0D, 130.0D,
                w - 10.0D, 130.0D,
                w - 10.0D, 155.0D,
                w - 42.0D, 155.0D,
                w - 55.0D, 142.0D);

        // Armor Frame
        drawLineLoop(guiGraphics, frameColor,
                15.0D, 15.0D,
                65.0D, 15.0D,
                85.0D, 35.0D,
                85.0D, 40.0D,
                15.0D, 40.0D);

        // Boost Frame
        drawLineLoop(guiGraphics, frameColor,
                3.0D, 15.0D,
                13.0D, 15.0D,
                13.0D, j - 23.0D,
                3.0D, j - 23.0D);

        // 方位計 (Orientation / Heading)
        float yaw = player.getYRot();
        double d2 = 0.0D - (double) (yaw % 10.0F);
        int k = (int) (yaw / 10.0F) * 2;

        guiGraphics.horizontalLine((int) (d - (w / 4)), (int) (d + (w / 4)), 12, frameColor);

        for (int l = -w / 20; l < w / 20; l++) {
            double posX = d / 2 + d2 + 2;
            if (posX >= d - (w / 4) && posX <= d + (w / 4)) {
                int len = ((k + l) % 18 == 0) ? 7 : (((l & 1) == 1) ? 3 : 5);
                guiGraphics.verticalLine((int) posX, 12, 12 + len, frameColor);
            }
            d2 += 5.0D;
        }

        drawPolygon(guiGraphics, frameColor, d, 15.0D, d + 4.0D, 21.0D, d - 4.0D, 21.0D);

        d2 = 0.0D - (double) (yaw % 10.0F);
        for (int l = -w / 20; l < w / 20; l++) {
            double posX = d / 2 + d2;
            if (posX >= d - (w / 4) && posX <= d + (w / 4)) {
                if ((k + l) % 18 == 0) {
                    int dirIndex = ((k + l) / 2 + Config.DEG_OFFSET.get()) % 36;
                    if (dirIndex < 0) dirIndex += 36;
                    String dirStr = switch (dirIndex) {
                        case 0 -> "N";
                        case 9 -> "E";
                        case 18 -> "S";
                        case 27 -> "W";
                        default -> "";
                    };

                    if (!dirStr.isEmpty()) {
                        guiGraphics.text(mc.font, dirStr, (int) posX, 2, colors.number(), false);
                    }
                }
            }
            d2 += 5.0D;
        }

        // ピッチ計
        float pitch = player.getXRot();
        double d3 = d1 - 105.0D - ((double) (pitch % 10.0F)) * 3.0D;
        int i1 = (int) (pitch / 10.0F) * 2;

        for (int j1 = -7; j1 < 7; j1++) {
            if (d3 > d1 - 60.0D && d3 < d1 + 61.0D) {
                int k1 = i1 + j1;
                if (k1 % 9 == 0) {
                    guiGraphics.horizontalLine((int) (d - 90.0D), (int) (d - 55.0D), (int) d3, frameColor);
                    guiGraphics.horizontalLine((int) (d + 55.0D), (int) (d + 90.0D), (int) d3, frameColor);
                } else {
                    int step = ((j1 & 1) == 0) ? 8 : 6;
                    for (int l1 = (step == 8 ? 0 : 1); l1 < step; l1++) {
                        double d7 = l1 * 2;
                        guiGraphics.fill((int) (d - 90.0D + d7), (int) d3, (int) (d - 89.0D + d7), (int) d3 + 1, frameColor);
                        guiGraphics.fill((int) (d + 90.0D - d7), (int) d3, (int) (d + 89.0D - d7), (int) d3 + 1, frameColor);
                    }
                    if (step == 8) {
                        int edgeDir = (k1 < 0 && k1 > -18 || k1 > 18) ? 3 : -3;
                        guiGraphics.verticalLine((int) (d - 90.0D), (int) d3, (int) d3 + edgeDir, frameColor);
                        guiGraphics.verticalLine((int) (d + 90.0D), (int) d3, (int) d3 + edgeDir, frameColor);
                    }
                }
            }
            d3 += 15.0D;
        }
        guiGraphics.horizontalLine((int) (d - 40.0D), (int) (d - 15.0D), (int) d1, frameColor);
        guiGraphics.horizontalLine((int) (d + 15.0D), (int) (d + 40.0D), (int) d1, frameColor);

        // 高度目盛り
        guiGraphics.verticalLine(w - 3, 92, j - 21, frameColor);
        d3 = (player.getY() * 10.0D) % 10.0D;
        int heightRange = j - 90 - 23;
        for (int j2 = 0; j2 < 129; j2++) {
            double tickY = (j - 21) - (j2 * heightRange / 128.0D);
            if (j2 % 32 == 0) {
                guiGraphics.horizontalLine(w - 10, w - 3, (int) tickY, frameColor);
            } else {
                int len = (j2 % 8 == 0) ? 3 : 0;
                guiGraphics.horizontalLine(w - 3 - len, w - 3, (int) tickY, frameColor);
            }
        }
        double hMarkerY = (j - 21) - (player.getY() * heightRange / 128.0d);
        drawPolygon(guiGraphics, frameColor, w - 4.0D, hMarkerY, w - 10.0D, hMarkerY - 3.0D, w - 10.0D, hMarkerY + 3.0D);

        // Speed
        String speedStr = String.format("%.1f", currentSpeed);
        guiGraphics.text(mc.font, "Speed", 17, j - 49, colors.number(), false);
        guiGraphics.text(mc.font, speedStr, 17, j - 35, colors.number(), false);

        // Height
        String heightStr = String.format("%.1f", player.getY());
        guiGraphics.text(mc.font, "Height", w - mc.font.width("Height") - 15, 132, colors.number(), false);
        guiGraphics.text(mc.font, heightStr, w - mc.font.width(heightStr) - 15, 145, colors.number(), false);

        // Pitch角度の数値
        d3 = d1 - 105.0D - ((double) (pitch % 10.0F)) * 3.0D - 3.0D;
        for (int k4 = -7; k4 < 7; k4++) {
            if (d3 > d1 - 63.0D && d3 < d1 + 60.0D && (k4 & 1) == 0) {
                int pVal = Math.abs(i1 + k4) / 2;
                if (pVal > 9) pVal = 18 - pVal;
                String pStr = String.format("%d0", pVal);
                guiGraphics.text(mc.font, pStr, (int) d - 104, (int) d3, colors.number(), false);
                guiGraphics.text(mc.font, pStr, (int) d + 94, (int) d3, colors.number(), false);
            }
            d3 += 15.0D;
        }

        // 探知可能鉱石の表示
        int oreAlphaVal = colors.normal() & 0xFF000000;
        int ironColor = oreAlphaVal | (colors.iron() & 0x00FFFFFF);
        int goldColor = oreAlphaVal | (colors.gold() & 0x00FFFFFF);
        int diamondColor = oreAlphaVal | (colors.diamond() & 0x00FFFFFF);
        int lavaColor = oreAlphaVal | (colors.lava() & 0x00FFFFFF);

        double posY = player.getY();
        if (posY < 64.0D) {
            guiGraphics.text(mc.font, "Iron", w - mc.font.width("Iron") - 10, 90, ironColor, false);
        }
        if (posY < 30.0D) {
            guiGraphics.text(mc.font, "Gold", w - mc.font.width("Gold") - 10, 100, goldColor, false);
        }
        if (posY < 16.0D) {
            guiGraphics.text(mc.font, "Diamond", w - mc.font.width("Diamond") - 10, 110, diamondColor, false);
        }
        if (posY < 12.0D) {
            guiGraphics.text(mc.font, "Lava", w - mc.font.width("Lava") - 10, 120, lavaColor, false);
        }

        // ワールド時計 (getDayTime() の代わりに getGameTime() を使用)
        long time = mc.level.getGameTime() % 24000L;
        int j6 = (int) (time / 10L);
        int k6 = j6 / 100 + 6;
        if (k6 >= 24) k6 -= 24;
        int l6 = (int) ((j6 % 100) / 1.66666665D);
        String timeStr = String.format("%02d:%02d", k6, l6);
        guiGraphics.text(mc.font, timeStr, 5, j - 15, colors.number(), false);

        // ステータス表示ポーション効果 (Record + Loop リファクタリング, SPEED/STRENGTHに修正)
        record EffectDisplay(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, String label, int color) {}
        List<EffectDisplay> effects = List.of(
            new EffectDisplay(MobEffects.FIRE_RESISTANCE, "ANTI-Flame", colors.number()),
            new EffectDisplay(MobEffects.SPEED, "SPD-Boost", colors.number()),
            new EffectDisplay(MobEffects.STRENGTH, "ATK-Boost", colors.number()),
            new EffectDisplay(MobEffects.REGENERATION, "AUTO-Repair", colors.number()),
            new EffectDisplay(MobEffects.HUNGER, "FUEL-Leakage", colors.warning())
        );

        int effectOffset = 0;
        for (var e : effects) {
            if (player.hasEffect(e.effect())) {
                effectOffset++;
                guiGraphics.text(mc.font, e.label(), 20, 34 + (effectOffset * 11), e.color(), false);
            }
        }

        // 毒の点滅警告
        if (player.hasEffect(MobEffects.POISON) && (j6 % 3 != 1)) {
            String poisonAlert = "ALERT POISON!";
            guiGraphics.text(mc.font, poisonAlert, (int) d - mc.font.width(poisonAlert) / 2, (int) d1 - 60, colors.alert(), false);
        }

        // 体力警告
        if (player.getHealth() < 9.0F && (j6 % 3 != 1)) {
            String healthAlert = "ALERT GCS DOWN!";
            int cAlert = (player.getHealth() < 5.0F) ? colors.alert() : colors.warning();
            guiGraphics.text(mc.font, healthAlert, (int) d - mc.font.width(healthAlert) / 2, (int) d1 - 80, cAlert, false);
        }

        // 空腹度警告
        if (player.getFoodData().getFoodLevel() < 7 && (j6 % 3 != 1)) {
            String fuelAlert = "ALERT LESS FUEL";
            int cAlert = (player.getHealth() < 5.0F) ? colors.alert() : colors.warning();
            guiGraphics.text(mc.font, fuelAlert, (int) d - mc.font.width(fuelAlert) / 2, (int) d1 - 70, cAlert, false);
        }

        // 使用ツールの耐久値表示
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.isDamageableItem()) {
            int durability = mainHand.getMaxDamage() - mainHand.getDamageValue();
            int textColor = colors.number();
            if ((float) mainHand.getDamageValue() / (float) mainHand.getMaxDamage() > 0.9F) {
                textColor = colors.alert();
            }
            String toolName = mainHand.getItem().getName(mainHand).getString() + " /";
            guiGraphics.text(mc.font, toolName, (int) d - mc.font.width(toolName) + 25, j - 58, textColor, false);

            String durStr = String.format("%7d", durability);
            guiGraphics.text(mc.font, durStr, (int) d - mc.font.width(durStr) + 60, j - 58, textColor, false);
        }

        // 弾薬表示 (弓 / クロスボウ所持時, Streamリファクタリング)
        if (!mainHand.isEmpty() && (mainHand.is(Items.BOW) || mainHand.is(Items.CROSSBOW))) {
            boolean isBow = mainHand.is(Items.BOW);
            int ammoCount = IntStream.range(0, player.getInventory().getContainerSize())
                    .mapToObj(player.getInventory()::getItem)
                    .filter(stack -> !stack.isEmpty())
                    .filter(stack -> isBow ? stack.is(ItemTags.ARROWS) : (stack.is(ItemTags.ARROWS) || stack.is(Items.FIREWORK_ROCKET)))
                    .mapToInt(ItemStack::getCount)
                    .sum();

            int textColor = (ammoCount <= 10) ? colors.alert() : colors.number();
            String ammoLabel = (isBow ? "Arrow /" : "Ammo /");
            guiGraphics.text(mc.font, ammoLabel, (int) d - mc.font.width(ammoLabel) + 25, j - 68, textColor, false);

            String ammoCountStr = (ammoCount == 0) ? "EMPTY" : String.format("%7d", ammoCount);
            guiGraphics.text(mc.font, ammoCountStr, (int) d - mc.font.width(ammoCountStr) + 60, j - 68, textColor, false);
        }

        // アーマー描画 (Stream & Switch Pattern Matching リファクタリング)
        List<ArmorStatus> armors = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(slot -> new ArmorStatus(player.getItemBySlot(slot), slot))
                .toList();

        int totalArmorMaxDamage = armors.stream().filter(a -> !a.isEmpty()).mapToInt(a -> a.stack().getMaxDamage()).sum();
        int totalArmorDamageValue = armors.stream().filter(a -> !a.isEmpty()).mapToInt(a -> a.stack().getDamageValue()).sum();
        boolean hasAnyArmor = armors.stream().anyMatch(a -> !a.isEmpty());

        int armorDrawX = 17;
        int armorDrawY = 17;

        for (ArmorStatus armor : armors) {
            if (!armor.isEmpty()) {
                int c = getArmorColor(armor.stack(), colors);
                switch (armor.slot()) {
                    case HEAD -> {
                        guiGraphics.fill(armorDrawX, armorDrawY, armorDrawX + 11, armorDrawY + 5, c);
                        guiGraphics.fill(armorDrawX, armorDrawY + 5, armorDrawX + 3, armorDrawY + 11, c);
                        guiGraphics.fill(armorDrawX + 8, armorDrawY + 5, armorDrawX + 11, armorDrawY + 11, c);
                    }
                    case CHEST -> {
                        guiGraphics.fill(armorDrawX + 13, armorDrawY, armorDrawX + 24, armorDrawY + 5, c);
                        guiGraphics.fill(armorDrawX + 15, armorDrawY + 5, armorDrawX + 22, armorDrawY + 11, c);
                    }
                    case LEGS -> {
                        guiGraphics.fill(armorDrawX + 26, armorDrawY, armorDrawX + 37, armorDrawY + 4, c);
                        guiGraphics.fill(armorDrawX + 26, armorDrawY + 4, armorDrawX + 31, armorDrawY + 11, c);
                        guiGraphics.fill(armorDrawX + 32, armorDrawY + 4, armorDrawX + 37, armorDrawY + 11, c);
                    }
                    case FEET -> {
                        int bootsDrawY = armorDrawY + 4;
                        guiGraphics.fill(armorDrawX + 40, bootsDrawY, armorDrawX + 44, bootsDrawY + 7, c);
                        guiGraphics.fill(armorDrawX + 39, bootsDrawY + 4, armorDrawX + 40, bootsDrawY + 7, c);
                        guiGraphics.fill(armorDrawX + 45, bootsDrawY, armorDrawX + 49, bootsDrawY + 7, c);
                        guiGraphics.fill(armorDrawX + 49, bootsDrawY + 4, armorDrawX + 50, bootsDrawY + 7, c);
                    }
                    default -> {}
                }
            }
        }

        // AP (Armor Point / 総耐久値)
        if (hasAnyArmor) {
            int apVal = totalArmorMaxDamage - totalArmorDamageValue;
            guiGraphics.text(mc.font, "AP ", 20, 30, colors.number(), false);
            guiGraphics.text(mc.font, String.format("%d", apVal), 40, 30, colors.number(), false);
        }

        // ブーストゲージ
        double bst1 = j * 0.9D;
        double bst2 = j * 0.8D;
        double bst0 = (((player.getFoodData().getFoodLevel() * 10) - currentSpeed) * (j / 240.0D));

        if (bst0 >= 0.0D) {
            if (bst0 >= 20.0D) {
                guiGraphics.fill(5, (int) (bst1 - bst0), 10, (int) bst2, colors.number());
                guiGraphics.fill(5, (int) bst2, 10, (int) bst1, colors.alert());
            } else {
                guiGraphics.fill(5, (int) (bst1 - bst0), 10, (int) bst1, colors.alert());
            }
        }
    }

    private void renderTypeB(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int w, int j, double d, double d1,
                             HudColors colors, float lineR, float lineG, float lineB, float lineAlpha) {

        int frameColor = ((int) (lineAlpha * 255) << 24) | (colors.normal() & 0x00FFFFFF);

        // 方位計 (Orientation / Heading)
        float yaw = player.getYRot();
        double d2 = 0.0D - (double) (yaw % 10.0F);
        int k = (int) (yaw / 10.0F) * 2;

        for (int l = -w / 10; l < w / 10; l++) {
            double posX = d2 - d;
            double posY = d1 - 90.0D - posX * posX * 0.0008D; // 放物線カーブ

            double tickX = d2 + 2;
            if (tickX >= 0 && tickX <= w) {
                int len = ((k + l) % 18 == 0) ? 7 : (((l & 1) == 1) ? 3 : 5);
                drawLine(guiGraphics, (float) tickX, (float) (posY - len), (float) tickX, (float) posY, frameColor);
            }
            d2 += 5.0D;
        }

        // 中央マーカー
        double d3 = d1 - 90.0D;
        drawPolygon(guiGraphics, frameColor, d, d3, d + 2.0D, d3 + 3.0D, d - 2.0D, d3 + 3.0D);

        // ピッチ計
        float pitch = player.getXRot();
        d2 = d;
        d3 = d1 - 105.0D - ((double) (pitch % 10.0F)) * 3.0D;
        int i1 = (int) (pitch / 10.0F) * 2;

        for (int j1 = -7; j1 < 7; j1++) {
            if (d3 > d1 - 60.0D && d3 < d1 + 61.0D) {
                int k1 = i1 + j1;
                if (k1 % 18 == 0) {
                    guiGraphics.horizontalLine((int) (d2 - 90.0D), (int) (d2 - 50.0D), (int) d3, frameColor);
                    guiGraphics.horizontalLine((int) (d2 + 50.0D), (int) (d2 + 90.0D), (int) d3, frameColor);
                } else {
                    int step = ((j1 & 1) == 0) ? 8 : 6;
                    for (int l1 = (step == 8 ? 0 : 1); l1 < step; l1++) {
                        double d7 = l1 * 4;
                        guiGraphics.fill((int) (d2 - 90.0D + d7), (int) d3, (int) (d2 - 88.0D + d7), (int) d3 + 1, frameColor);
                        guiGraphics.fill((int) (d2 + 90.0D - d7), (int) d3, (int) (d2 + 88.0D - d7), (int) d3 + 1, frameColor);
                    }
                    if (step == 8) {
                        int edgeDir = (k1 < 0 && k1 > -18 || k1 > 18) ? 3 : -3;
                        guiGraphics.verticalLine((int) (d2 - 90.0D), (int) d3, (int) d3 + edgeDir, frameColor);
                        guiGraphics.verticalLine((int) (d2 + 90.0D), (int) d3, (int) d3 + edgeDir, frameColor);
                    }
                }
            }
            d3 += 15.0D;
        }

        // 中央自機マーク
        guiGraphics.horizontalLine((int) (d2 - 40.0D), (int) (d2 - 15.0D), (int) d1, frameColor);
        guiGraphics.horizontalLine((int) (d2 + 15.0D), (int) (d2 + 40.0D), (int) d1, frameColor);

        // スピードメーター (左の縦目盛り)
        d3 = 0.0D + (currentSpeed * 10.0D) % 10.0D;
        for (int i2 = 0; i2 < j / 5; i2++) {
            if (d3 < d1 - 4.0D || d3 > d1 + 7.0D) {
                double diffY = d3 - d1;
                double posX = (d - 160.0D) + diffY * diffY * 0.003D; // 放物線カーブ
                if (d3 > d1 - 100.0D && d3 < (double) j - 80.0D) {
                    int len = ((i2 & 1) == 1) ? 3 : 5;
                    drawLine(guiGraphics, (float) posX, (float) d3, (float) (posX - len), (float) d3, frameColor);
                }
            }
            d3 += 5.0D;
        }
        // スピード表示用数値枠
        drawLineLoop(guiGraphics, frameColor,
                d - 160.0D, d1 - 4.0D,
                d - 115.0D, d1 - 4.0D,
                d - 115.0D, d1 + 7.0D,
                d - 160.0D, d1 + 7.0D);

        // 高度メーター (右の縦目盛り)
        d3 = 0.0D + (player.getY() * 10.0D) % 10.0D;
        for (int j2 = 0; j2 < j / 5; j2++) {
            if (d3 < d1 - 4.0D || d3 > d1 + 7.0D) {
                double diffY = d3 - d1;
                double posX = (d + 160.0D) - diffY * diffY * 0.003D; // 放物線カーブ
                if (d3 > d1 - 100.0D && d3 < (double) j - 60.0D) {
                    int len = ((j2 & 1) == 1) ? 3 : 5;
                    drawLine(guiGraphics, (float) posX, (float) d3, (float) (posX + len), (float) d3, frameColor);
                }
            }
            d3 += 5.0D;
        }
        // 高度表示用数値枠
        drawLineLoop(guiGraphics, frameColor,
                d + 115.0D, d1 - 4.0D,
                d + 160.0D, d1 - 4.0D,
                d + 160.0D, d1 + 7.0D,
                d + 115.0D, d1 + 7.0D);

        // アーマー枠
        drawLineLoop(guiGraphics, frameColor,
                d - 160.0D, (double) j - 70.0D,
                d - 115.0D, (double) j - 70.0D,
                d - 115.0D, (double) j - 10.0D,
                d - 160.0D, (double) j - 10.0D);

        // 文字と数値の描画
        // 方位文字
        d2 = 0.0D - (double) (yaw % 10.0F);
        for (int i3 = -w / 10; i3 < w / 10; i3++) {
            double posX = d2 - d;
            double posY = d1 - 105.0D - posX * posX * 0.0008D;
            if (tickXInRange(d2, 0, w)) {
                if ((k + i3) % 18 == 0) {
                    int dirIndex = ((k + i3) / 2 + Config.DEG_OFFSET.get()) % 36;
                    if (dirIndex < 0) dirIndex += 36;
                    String dirStr = switch (dirIndex) {
                        case 0 -> "North";
                        case 9 -> "East";
                        case 18 -> "South";
                        case 27 -> "West";
                        default -> "";
                    };

                    if (!dirStr.isEmpty()) {
                        guiGraphics.text(mc.font, dirStr, (int) d2 + 2 - mc.font.width(dirStr) / 2, (int) posY, colors.number(), false);
                    }
                }
            }
            d2 += 5.0D;
        }

        // スピードメーター内数値
        d3 = 0.0D + (currentSpeed * 10.0D) % 10.0D;
        for (int j3 = -j / 20; j3 < j / 20; j3++) {
            int speedVal = (int) currentSpeed - j3;
            if ((d3 < d1 - 9.0D || d3 > d1 + 12.0D) && speedVal % 5 == 0) {
                double diffY = d3 - d1;
                double posX = (d - 200.0D) + diffY * diffY * 0.003D;
                String speedValStr = String.format("%4d", speedVal);
                if (d3 > d1 - 100.0D && d3 < (double) j - 80.0D) {
                    guiGraphics.text(mc.font, speedValStr, (int) posX + 10, (int) d3 - 2, colors.number(), false);
                }
            }
            d3 += 10.0D;
        }
        // 現在のスピード
        String currentSpeedStr = String.format("%.1f", currentSpeed);
        guiGraphics.text(mc.font, currentSpeedStr, (int) d - 120 - mc.font.width(currentSpeedStr), j / 2 - 2, colors.number(), false);
        guiGraphics.text(mc.font, "Speed", (int) d - mc.font.width("Speed") / 2 - 140, (int) d1 - 15, colors.number(), false);

        // 高度メーター内数値
        d3 = 0.0D + (player.getY() * 10.0D) % 10.0D;
        for (int i4 = -j / 20; i4 < j / 20; i4++) {
            int heightVal = (int) player.getY() - i4;
            if ((d3 < d1 - 9.0D || d3 > d1 + 12.0D) && heightVal % 5 == 0) {
                double diffY = d3 - d1;
                double posX = (d + 155.0D) - diffY * diffY * 0.003D;
                String hValStr = String.format("%4d", heightVal);
                if (d3 > d1 - 100.0D && d3 < (double) j - 60.0D) {
                    guiGraphics.text(mc.font, hValStr, (int) posX + 10, (int) d3 - 2, colors.number(), false);
                }
            }
            d3 += 10.0D;
        }
        // 現在の高度
        String currentHeightStr = String.format("%.1f", player.getY());
        guiGraphics.text(mc.font, currentHeightStr, (int) d + 155 - mc.font.width(currentHeightStr), j / 2 - 2, colors.number(), false);
        guiGraphics.text(mc.font, "Height", (int) d - mc.font.width("Height") / 2 + 135, (int) d1 - 15, colors.number(), false);

        // Pitch角度の数値
        d3 = (double) j / 2.0D - 105.0D - ((double) (pitch % 10.0F)) * 3.0D - 3.0D;
        for (int k4 = -7; k4 < 7; k4++) {
            if (d3 > d1 - 63.0D && d3 < d1 + 60.0D && (k4 & 1) == 0) {
                int pVal = Math.abs(i1 + k4) / 2;
                if (pVal > 9) pVal = 18 - pVal;
                String pStr = String.format("%d0", pVal);
                guiGraphics.text(mc.font, pStr, (int) d - 104, (int) d3, colors.number(), false);
                guiGraphics.text(mc.font, pStr, (int) d + 94, (int) d3, colors.number(), false);
            }
            d3 += 15.0D;
        }

        // 探知可能鉱石の表示
        int oreAlphaVal = colors.normal() & 0xFF000000;
        int ironColor = oreAlphaVal | (colors.iron() & 0x00FFFFFF);
        int goldColor = oreAlphaVal | (colors.gold() & 0x00FFFFFF);
        int diamondColor = oreAlphaVal | (colors.diamond() & 0x00FFFFFF);
        int lavaColor = oreAlphaVal | (colors.lava() & 0x00FFFFFF);

        double posY = player.getY();
        if (posY < 64.0D) {
            guiGraphics.text(mc.font, "Iron", w - mc.font.width("Iron") - 6, (int) d1 - 20, ironColor, false);
        }
        if (posY < 30.0D) {
            guiGraphics.text(mc.font, "Gold", w - mc.font.width("Gold") - 6, (int) d1 - 10, goldColor, false);
        }
        if (posY < 16.0D) {
            guiGraphics.text(mc.font, "Diamond", w - mc.font.width("Diamond") - 6, (int) d1, diamondColor, false);
        }
        if (posY < 12.0D) {
            guiGraphics.text(mc.font, "Lava", w - mc.font.width("Lava") - 6, (int) d1 + 15, lavaColor, false);
        }

        // ワールド時計 (getDayTime() の代わりに getGameTime() を使用)
        long time = mc.level.getGameTime() % 24000L;
        int j6 = (int) (time / 10L);
        int k6 = j6 / 100 + 6;
        if (k6 >= 24) k6 -= 24;
        int l6 = (int) ((j6 % 100) / 1.66666665D);
        String timeStr = String.format("%02d:%02d", k6, l6);
        guiGraphics.text(mc.font, timeStr, 15, j - 18, colors.number(), false);

        // ポーション効果 (Record + Loop リファクタリング, SPEED/STRENGTHに修正)
        record EffectDisplay(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, String label, int color) {}
        List<EffectDisplay> effects = List.of(
            new EffectDisplay(MobEffects.FIRE_RESISTANCE, "FireResistance", colors.number()),
            new EffectDisplay(MobEffects.REGENERATION, "Regeneration", colors.number()),
            new EffectDisplay(MobEffects.STRENGTH, "Strength", colors.number()),
            new EffectDisplay(MobEffects.SPEED, "Speed", colors.number()),
            new EffectDisplay(MobEffects.HUNGER, "Hunger", colors.warning())
        );

        int effectOffset = 0;
        for (var e : effects) {
            if (player.hasEffect(e.effect())) {
                effectOffset++;
                guiGraphics.text(mc.font, e.label(), 5, 5 + (effectOffset * 11), e.color(), false);
            }
        }

        // 毒の点滅警告
        if (player.hasEffect(MobEffects.POISON) && (j6 % 3 != 1)) {
            String poisonAlert = "ALERT POISON!";
            guiGraphics.text(mc.font, poisonAlert, (int) d - mc.font.width(poisonAlert) / 2, (int) d1 - 60, colors.alert(), false);
        }

        // 体力警告
        if (player.getHealth() < 9.0F && (j6 % 3 != 1)) {
            String healthAlert = "ALERT HEALTH";
            int cAlert = (player.getHealth() < 5.0F) ? colors.alert() : colors.warning();
            guiGraphics.text(mc.font, healthAlert, (int) d - mc.font.width(healthAlert) / 2, (int) d1 - 80, cAlert, false);
        }

        // 空腹度警告
        if (player.getFoodData().getFoodLevel() < 7 && (j6 % 3 != 1)) {
            String fuelAlert = "ALERT FOOD";
            int cAlert = (player.getHealth() < 5.0F) ? colors.alert() : colors.warning();
            guiGraphics.text(mc.font, fuelAlert, (int) d - mc.font.width(fuelAlert) / 2, (int) d1 - 70, cAlert, false);
        }

        // 使用ツールの耐久値表示
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.isDamageableItem()) {
            int durability = mainHand.getMaxDamage() - mainHand.getDamageValue();
            int textColor = colors.number();
            if ((float) mainHand.getDamageValue() / (float) mainHand.getMaxDamage() > 0.9F) {
                textColor = colors.alert();
            }
            String toolName = mainHand.getItem().getName(mainHand).getString() + " /";
            guiGraphics.text(mc.font, toolName, (int) d - mc.font.width(toolName) + 25, (int) d1 + 63, textColor, false);

            String durStr = String.format("%7d", durability);
            guiGraphics.text(mc.font, durStr, (int) d - mc.font.width(durStr) + 60, (int) d1 + 63, textColor, false);
        }

        // 弾薬表示 (弓 / クロスボウ所持時, Streamリファクタリング)
        if (!mainHand.isEmpty() && (mainHand.is(Items.BOW) || mainHand.is(Items.CROSSBOW))) {
            boolean isBow = mainHand.is(Items.BOW);
            int ammoCount = IntStream.range(0, player.getInventory().getContainerSize())
                    .mapToObj(player.getInventory()::getItem)
                    .filter(stack -> !stack.isEmpty())
                    .filter(stack -> isBow ? stack.is(ItemTags.ARROWS) : (stack.is(ItemTags.ARROWS) || stack.is(Items.FIREWORK_ROCKET)))
                    .mapToInt(ItemStack::getCount)
                    .sum();

            int textColor = (ammoCount <= 10) ? colors.alert() : colors.number();
            String ammoLabel = (isBow ? "Arrow /" : "Ammo /");
            guiGraphics.text(mc.font, ammoLabel, (int) d - mc.font.width(ammoLabel) + 15, (int) d1 + 54, textColor, false);

            String ammoCountStr = (ammoCount == 0) ? "EMPTY" : String.format("%7d", ammoCount);
            guiGraphics.text(mc.font, ammoCountStr, (int) d - mc.font.width(ammoCountStr) + 50, (int) d1 + 54, textColor, false);
        }

        // アーマー枠内防具描画 (TypeB, Stream & Switch Pattern Matching リファクタリング)
        List<ArmorStatus> armors = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(slot -> new ArmorStatus(player.getItemBySlot(slot), slot))
                .toList();

        int armorDrawX = (int) d - 143;
        int armorDrawY = j - 62;

        for (ArmorStatus armor : armors) {
            if (!armor.isEmpty()) {
                int c = getArmorColor(armor.stack(), colors);
                switch (armor.slot()) {
                    case HEAD -> guiGraphics.fill(armorDrawX, armorDrawY, armorDrawX + 11, armorDrawY + 11, c);
                    case CHEST -> {
                        guiGraphics.fill(armorDrawX, armorDrawY + 12, armorDrawX + 11, armorDrawY + 22, c);
                        guiGraphics.fill(armorDrawX - 6, armorDrawY + 12, armorDrawX - 1, armorDrawY + 28, c);
                        guiGraphics.fill(armorDrawX + 12, armorDrawY + 12, armorDrawX + 17, armorDrawY + 28, c);
                    }
                    case LEGS -> {
                        int legsDrawY = armorDrawY + 23;
                        guiGraphics.fill(armorDrawX, legsDrawY, armorDrawX + 11, legsDrawY + 5, c);
                        guiGraphics.fill(armorDrawX, legsDrawY + 5, armorDrawX + 5, legsDrawY + 13, c);
                        guiGraphics.fill(armorDrawX + 6, legsDrawY + 5, armorDrawX + 11, legsDrawY + 13, c);
                    }
                    case FEET -> {
                        int bootsDrawY = armorDrawY + 37;
                    guiGraphics.fill(armorDrawX, bootsDrawY, armorDrawX + 5, bootsDrawY + 7, c);
                        guiGraphics.fill(armorDrawX + 6, bootsDrawY, armorDrawX + 11, bootsDrawY + 7, c);
                    }
                    default -> {}
                }
            }
        }
    }

    private int getArmorColor(ItemStack stack, HudColors colors) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return colors.normal();
        float ratio = (float) stack.getDamageValue() / (float) stack.getMaxDamage();
        if (ratio < 0.2F) return colors.normal();
        if (ratio < 0.5F) return colors.number();
        if (ratio < 0.8F) return colors.warning();
        return colors.alert();
    }

    private int parseColor(String hex, int defaultColor) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    private boolean tickXInRange(double x, int min, int max) {
        return x >= min && x <= max;
    }

    private void addValLine(VertexConsumer consumer, org.joml.Matrix3x2f pose, float x1, float y1, float x2, float y2, float width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;

        float nx = -dy / len * (width / 2.0f);
        float ny = dx / len * (width / 2.0f);

        float x1_l = x1 + nx;
        float y1_l = y1 + ny;
        float x1_r = x1 - nx;
        float y1_r = y1 - ny;

        float x2_l = x2 + nx;
        float y2_l = y2 + ny;
        float x2_r = x2 - nx;
        float y2_r = y2 - ny;

        consumer.addVertexWith2DPose(pose, x1_l, y1_l).setColor(color);
        consumer.addVertexWith2DPose(pose, x2_l, y2_l).setColor(color);
        consumer.addVertexWith2DPose(pose, x2_r, y2_r).setColor(color);
        consumer.addVertexWith2DPose(pose, x1_r, y1_r).setColor(color);
    }

    // Modern 2D Polygon Render using addVertexWith2DPose and BufferUploader
    private void drawPolygon(GuiGraphicsExtractor guiGraphics, int color, double... vertices) {
        if (vertices.length < 6 || vertices.length % 2 != 0) return;

        final org.joml.Matrix3x2f poseCopy = new org.joml.Matrix3x2f(guiGraphics.pose());

        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                int n = vertices.length / 2;
                for (int i = 0; i < n - 2; i++) {
                    float x0 = (float) vertices[0];
                    float y0 = (float) vertices[1];
                    float x1 = (float) vertices[(i + 1) * 2];
                    float y1 = (float) vertices[(i + 1) * 2 + 1];
                    float x2 = (float) vertices[(i + 2) * 2];
                    float y2 = (float) vertices[(i + 2) * 2 + 1];

                    consumer.addVertexWith2DPose(poseCopy, x0, y0).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x1, y1).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x2, y2).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x2, y2).setColor(color);
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        });
    }

    // Modern 2D Line Loop Render using addVertexWith2DPose and BufferUploader
    private void drawLineLoop(GuiGraphicsExtractor guiGraphics, int color, double... vertices) {
        if (vertices.length < 4 || vertices.length % 2 != 0) return;

        final org.joml.Matrix3x2f poseCopy = new org.joml.Matrix3x2f(guiGraphics.pose());

        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                int n = vertices.length / 2;
                float width = 1.0f;
                for (int i = 0; i < n; i++) {
                    float x1 = (float) vertices[i * 2];
                    float y1 = (float) vertices[i * 2 + 1];
                    float x2 = (float) vertices[((i + 1) % n) * 2];
                    float y2 = (float) vertices[((i + 1) % n) * 2 + 1];

                    addValLine(consumer, poseCopy, x1, y1, x2, y2, width, color);
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        });
    }

    // Modern 2D Line Render using addVertexWith2DPose and BufferUploader
    private void drawLine(GuiGraphicsExtractor guiGraphics, float x1, float y1, float x2, float y2, int color) {
        final org.joml.Matrix3x2f poseCopy = new org.joml.Matrix3x2f(guiGraphics.pose());

        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                addValLine(consumer, poseCopy, x1, y1, x2, y2, 1.0f, color);
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        });
    }

    private void drawArch(GuiGraphicsExtractor guiGraphics, int color, double centerX, double centerY,
                          double radius, double thick, double startAngle, double endAngle) {
        if (Math.abs(endAngle - startAngle) < 0.001) {
            return;
        }
        double rad = Math.PI / 180.0;
        double start = startAngle * rad;
        double end = endAngle * rad;
        if (end < start) {
            double temp = start;
            start = end;
            end = temp;
        }

        double outer = radius + thick;
        double inner = radius - thick;

        final double finalStart = start;
        final double finalEnd = end;
        final org.joml.Matrix3x2f poseCopy = new org.joml.Matrix3x2f(guiGraphics.pose());

        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                double step = 5.0 * rad;
                double current = finalStart;

                while (current < finalEnd) {
                    double next = Math.min(current + step, finalEnd);

                    double sinCurr = Math.sin(current);
                    double cosCurr = Math.cos(current);
                    double sinNext = Math.sin(next);
                    double cosNext = Math.cos(next);

                    float x1_out = (float) (centerX + sinCurr * outer);
                    float y1_out = (float) (centerY - cosCurr * outer);
                    float x1_in  = (float) (centerX + sinCurr * inner);
                    float y1_in  = (float) (centerY - cosCurr * inner);

                    float x2_out = (float) (centerX + sinNext * outer);
                    float y2_out = (float) (centerY - cosNext * outer);
                    float x2_in  = (float) (centerX + sinNext * inner);
                    float y2_in  = (float) (centerY - cosNext * inner);

                    consumer.addVertexWith2DPose(poseCopy, x1_out, y1_out).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x2_out, y2_out).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x2_in, y2_in).setColor(color);
                    consumer.addVertexWith2DPose(poseCopy, x1_in, y1_in).setColor(color);

                    current = next;
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        });
    }

    private void drawArchLine(GuiGraphicsExtractor guiGraphics, int color, double centerX, double centerY,
                              double radius, double startAngle, double endAngle) {
        if (Math.abs(endAngle - startAngle) < 0.001) {
            return;
        }
        double rad = Math.PI / 180.0;
        double start = startAngle * rad;
        double end = endAngle * rad;
        if (end < start) {
            double temp = start;
            start = end;
            end = temp;
        }

        final double finalStart = start;
        final double finalEnd = end;
        final org.joml.Matrix3x2f poseCopy = new org.joml.Matrix3x2f(guiGraphics.pose());

        guiGraphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer consumer) {
                double step = 5.0 * rad;
                double current = finalStart;
                float width = 1.0f;

                float lastX = (float) (centerX + Math.sin(current) * radius);
                float lastY = (float) (centerY - Math.cos(current) * radius);

                while (current < finalEnd) {
                    double next = Math.min(current + step, finalEnd);
                    float nextX = (float) (centerX + Math.sin(next) * radius);
                    float nextY = (float) (centerY - Math.cos(next) * radius);

                    addValLine(consumer, poseCopy, lastX, lastY, nextX, nextY, width, color);

                    lastX = nextX;
                    lastY = nextY;
                    current = next;
                }
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        });
    }

    private void renderACV(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int width, int height, double centerX, double centerY,
                           HudColors colors, float lineR, float lineG, float lineB, float lineAlpha) {
        float scale = height / 240.0F;
        float radius = 55.0F * scale;
        float thick = 1.4F * scale;
        float offset = 2.0F * scale;
        float frameOuterRadius = radius + thick;
        float frameInnerRadius = radius - thick;

        int whiteColor = ((int) (lineAlpha * 255) << 24) | 0x00FFFFFF;
        int mainColor = colors.normal();
        int subColor = colors.number();
        int redColor = colors.alert();

        float outLineThick = thick * 1.5F;
        float outerRadius = radius + 5.0F * scale;

        // 右下 *90-180 (満腹度)
        float hungerVal = (player.getFoodData().getFoodLevel() + player.getFoodData().getSaturationLevel()) / 40.0F;
        hungerVal = Math.max(0.0F, Math.min(1.0F, hungerVal));
        drawArch(guiGraphics, mainColor, centerX + offset, centerY, outerRadius, thick, 90.0D, 90.0D + 90.0D * hungerVal);
        
        // フレーム
        drawArch(guiGraphics, subColor, centerX + offset, centerY, outerRadius - thick, outLineThick, 90.0D, 180.0D);
        drawLine(guiGraphics, (float) (centerX + offset + outerRadius - thick + thick * 4), (float) centerY,
                (float) (centerX + offset + outerRadius - thick), (float) centerY, subColor);
        drawLine(guiGraphics, (float) (centerX + offset), (float) (centerY + frameOuterRadius),
                (float) (centerX + offset), (float) (centerY + frameOuterRadius + thick * 4), subColor);

        // 左下 180-*270 (体力)
        float healthVal = Math.max(player.getHealth() / player.getMaxHealth(), 0.0F);
        drawArch(guiGraphics, mainColor, centerX - offset, centerY, outerRadius, thick, 180.0D, 180.0D + 90.0D * healthVal);
        
        // フレーム
        drawArch(guiGraphics, subColor, centerX - offset, centerY, outerRadius - thick, outLineThick, 180.0D, 270.0D);
        drawLine(guiGraphics, (float) (centerX - offset - outerRadius + thick - thick * 4), (float) centerY,
                (float) (centerX - offset - outerRadius + thick), (float) centerY, subColor);
        drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY + frameOuterRadius),
                (float) (centerX - offset), (float) (centerY + frameOuterRadius + thick * 4), subColor);

        float spikeThick = 0.6F * scale;
        float spikeLength = 6.0F * scale;

        // 右下内側 (速度)
        double speedVal = currentSpeed / 100.0D;
        float speedFactor = (float) (1.0D - Math.min(Math.sqrt(speedVal) / 2.0D, 1.0D));
        drawArch(guiGraphics, mainColor, centerX + offset, centerY, radius, thick, 105.0D + 75.0D * speedFactor, 180.0D);
        
        // フレーム
        drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameOuterRadius, 100.0D, 180.0D);
        drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameInnerRadius, 100.0D, 180.0D);
        
        // フレーム端
        float startX = (float) (Math.sin(100.0 * Math.PI / 180.0) * frameOuterRadius);
        float startY = (float) (Math.cos(100.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX + offset + startX), (float) (centerY - startY),
                (float) (centerX + offset + startX - thick * 5), (float) (centerY - startY), whiteColor);
        float endX = (float) (Math.sin(180.0 * Math.PI / 180.0) * frameOuterRadius);
        float endY = (float) (Math.cos(180.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX + offset + endX), (float) (centerY - endY),
                (float) (centerX + offset + endX), (float) (centerY - endY - thick * 5), whiteColor);

        // とげ
        float spikeSin = (float) Math.sin(135.0 * Math.PI / 180.0);
        float spikeCos = (float) Math.cos(135.0 * Math.PI / 180.0);
        float spikeBottomX = (float) (centerX + offset + spikeSin * frameInnerRadius);
        float spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        float spikeTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength));
        float spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        float spikeTopTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        float spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 左下内側 (高度)
        float posHeightVal = (float) player.getY();
        drawArch(guiGraphics, mainColor, centerX - offset, centerY, radius, thick, 180.0D, 180.0D + 75.0D * Math.max(0.0F, Math.min(posHeightVal, 256.0F)) / 256.0F);
        
        // フレーム
        drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameOuterRadius, 180.0D, 260.0D);
        drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameInnerRadius, 180.0D, 260.0D);
        
        // フレーム端
        startX = (float) (Math.sin(180.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(180.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX - offset + startX), (float) (centerY - startY),
                (float) (centerX - offset + startX), (float) (centerY - startY - thick * 5), whiteColor);
        endX = (float) (Math.sin(260.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(260.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX - offset + endX), (float) (centerY - endY),
                (float) (centerX - offset + endX + thick * 5), (float) (centerY - endY), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(225.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(225.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX - offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 右上 0-*75 (メインハンドツール耐久値)
        ItemStack mainHand = player.getMainHandItem();
        int heldDurableVal = mainHand.getMaxDamage() - mainHand.getDamageValue();
        if (mainHand.isDamageableItem()) {
            drawArch(guiGraphics, mainColor, centerX + offset, centerY, radius, thick, 0.0D, 75.0D * heldDurableVal / mainHand.getMaxDamage());
        }
        // フレーム
        drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameOuterRadius, 0.0D, 80.0D);
        drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameInnerRadius, 0.0D, 80.0D);
        startX = (float) (Math.sin(0.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(0.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX + offset + startX), (float) (centerY - startY),
                (float) (centerX + offset + startX), (float) (centerY - startY + thick * 5), whiteColor);
        endX = (float) (Math.sin(80.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(80.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX + offset + endX), (float) (centerY - endY),
                (float) (centerX + offset + endX - thick * 5), (float) (centerY - endY), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(45.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(45.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX + offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 左上 *285-360 (アーマー値)
        float armorVal = player.getArmorValue() / 20.0F;
        if (armorVal > 0.0F) {
            drawArch(guiGraphics, mainColor, centerX - offset, centerY, radius, thick, 285.0D + 75.0D * (1.0F - armorVal), 360.0D);
        }
        // フレーム
        drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameOuterRadius, 280.0D, 360.0D);
        drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameInnerRadius, 280.0D, 360.0D);
        startX = (float) (Math.sin(280.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(280.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX - offset + startX), (float) (centerY - startY),
                (float) (centerX - offset + startX + thick * 5), (float) (centerY - startY), whiteColor);
        endX = (float) (Math.sin(360.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(360.0 * Math.PI / 180.0) * frameOuterRadius);
        drawLine(guiGraphics, (float) (centerX - offset + endX), (float) (centerY - endY),
                (float) (centerX - offset + endX), (float) (centerY - endY + thick * 5), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(315.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(315.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX - offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 残りのフレーム (上下の水平線)
        drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY - frameOuterRadius),
                (float) (centerX + offset), (float) (centerY - frameOuterRadius), whiteColor);
        drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY + frameOuterRadius),
                (float) (centerX + offset), (float) (centerY + frameOuterRadius), whiteColor);

        // 三角形 (方位指針)
        float yaw = -player.getYRot();
        float triBaseX = (float) (Math.sin(yaw * Math.PI / 180.0) * (radius + 12.5F * scale));
        float triBaseY = (float) (Math.cos(yaw * Math.PI / 180.0) * (radius + 12.5F * scale));
        float triRightX = (float) (Math.sin((yaw + 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triRightY = (float) (Math.cos((yaw + 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triLeftX = (float) (Math.sin((yaw - 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triLeftY = (float) (Math.cos((yaw - 2) * Math.PI / 180.0) * (radius + 15.0F * scale));

        drawPolygon(guiGraphics, whiteColor,
                centerX + triBaseX, centerY - triBaseY,
                centerX + triRightX, centerY - triRightY,
                centerX + triLeftX, centerY - triLeftY);

        drawPolygon(guiGraphics, whiteColor,
                centerX - triBaseX, centerY + triBaseY,
                centerX - triRightX, centerY + triRightY,
                centerX - triLeftX, centerY + triLeftY);

        // テキスト表示
        float fontHeight = mc.font.lineHeight;

        // 左下 (Hunger)
        String hungerStr = String.format("%06.2f", hungerVal * 100.0F);
        guiGraphics.text(mc.font, hungerStr, (int) (centerX + 60.0F * scale), (int) (centerY - fontHeight), subColor, false);

        // 右下 (Health)
        String healthStr = String.format("%06.2f", healthVal * 100.0F);
        guiGraphics.text(mc.font, healthStr, (int) (centerX - 60.0F * scale - mc.font.width(healthStr)), (int) (centerY - fontHeight), subColor, false);

        // 速度
        String speedStr = String.format("%.2f", currentSpeed);
        guiGraphics.text(mc.font, speedStr, (int) (centerX + 50.0F * scale - mc.font.width(speedStr)), (int) (centerY + 25.0F * scale - fontHeight), subColor, false);

        // 高度
        String posHeightStr = String.format("%06.2f", posHeightVal);
        guiGraphics.text(mc.font, posHeightStr, (int) (centerX - 50.0F * scale), (int) (centerY + 25.0F * scale - fontHeight), subColor, false);

        // メインハンド耐久値
        if (mainHand.isDamageableItem()) {
            String length = String.valueOf(String.valueOf(mainHand.getMaxDamage()).length());
            String durableStr = String.format("%0" + length + "d", heldDurableVal);
            guiGraphics.text(mc.font, durableStr, (int) (centerX + 50.0F * scale - mc.font.width(durableStr)), (int) (centerY - 25.0F * scale), subColor, false);
        }

        // アーマー値
        if (armorVal > 0.0F) {
            String armorStr = String.format("%06.2f", armorVal * 100.0F);
            guiGraphics.text(mc.font, armorStr, (int) (centerX - 50.0F * scale), (int) (centerY - 25.0F * scale), subColor, false);
        }

        // 防具の個別表示
        int armorInv = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack stack = player.getItemBySlot(slot);
            armorInv++;
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int durableVal = stack.getMaxDamage() - stack.getDamageValue();
                String length = String.valueOf(String.valueOf(stack.getMaxDamage()).length());
                String durableStr = String.format("%0" + length + "d", durableVal);
                guiGraphics.text(mc.font, durableStr,
                        (int) (centerX - (15.0F * (5 - armorInv) + 10.0F) * scale - mc.font.width(durableStr)),
                        (int) (centerY - (15.0F * armorInv + 10.0F) * scale - fontHeight),
                        subColor, false);
            }
        }
    }

    private void renderAC6(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int width, int height, double centerX, double centerY,
                           HudColors colors, float lineR, float lineG, float lineB, float lineAlpha) {
        float scale = height / 240.0F;
        int frameColor = ((int) (lineAlpha * 255) << 24) | (colors.normal() & 0x00FFFFFF);
        int textColor = colors.number();

        // 1. 中央レティクル
        double retRadius = 35.0D * scale;
        double retThick = 1.0D * scale;

        // 左側円弧 (体力 AP %, 120〜240度)
        float hpRate = player.getHealth() / player.getMaxHealth();
        double leftStart = 120.0D;
        double leftEnd = 120.0D + 120.0D * hpRate;
        drawArch(guiGraphics, frameColor, centerX - 5.0D * scale, centerY, retRadius, retThick, leftStart, leftEnd);
        drawArchLine(guiGraphics, frameColor, centerX - 5.0D * scale, centerY, retRadius + retThick, 120.0D, 240.0D);

        // 右側円弧 (メインハンドツールの耐久値、または満腹度、300〜420度)
        ItemStack mainHand = player.getMainHandItem();
        float rightRate = 1.0F;
        if (mainHand.isDamageableItem()) {
            rightRate = 1.0F - ((float) mainHand.getDamageValue() / (float) mainHand.getMaxDamage());
        } else {
            rightRate = player.getFoodData().getFoodLevel() / 20.0F;
        }
        double rightStart = 300.0D;
        double rightEnd = 300.0D + 120.0D * rightRate;
        drawArch(guiGraphics, frameColor, centerX + 5.0D * scale, centerY, retRadius, retThick, rightStart, rightEnd);
        drawArchLine(guiGraphics, frameColor, centerX + 5.0D * scale, centerY, retRadius + retThick, 300.0D, 420.0D);

        // 中央レティクル下の高度
        String altStr = String.format("%.0fm", player.getY());
        guiGraphics.text(mc.font, altStr, (int) (centerX - mc.font.width(altStr) / 2.0D), (int) (centerY + retRadius + 5.0D * scale), textColor, false);

        // 2. 左下表示 (AP)
        double apX = 30.0D * scale;
        double apY = height - 60.0D * scale;

        guiGraphics.text(mc.font, "AP", (int) apX, (int) apY, textColor, false);
        String apValStr = String.format("%05d", (int) (player.getHealth() * 500));
        guiGraphics.text(mc.font, apValStr, (int) (apX + 20.0D * scale), (int) (apY - 2.0D * scale), textColor, false);

        // APゲージバー
        double barWidth = 100.0D * scale;
        double barHeight = 2.0D * scale;
        guiGraphics.fill((int) apX, (int) (apY + 12.0D * scale), (int) (apX + barWidth), (int) (apY + 12.0D * scale + barHeight), 0x33FFFFFF & frameColor);
        guiGraphics.fill((int) apX, (int) (apY + 12.0D * scale), (int) (apX + barWidth * hpRate), (int) (apY + 12.0D * scale + barHeight), frameColor);

        // REPAIR, EXPANSION のカウント
        int repairCount = 0;
        int expansionCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(Items.POTION) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    repairCount += stack.getCount();
                } else if (stack.is(Items.TOTEM_OF_UNDYING)) {
                    expansionCount += stack.getCount();
                }
            }
        }
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            expansionCount += player.getOffhandItem().getCount();
        }

        double subY = apY + 20.0D * scale;
        guiGraphics.text(mc.font, "EXPANSION", (int) apX, (int) subY, textColor, false);
        guiGraphics.text(mc.font, String.valueOf(expansionCount), (int) (apX + 60.0D * scale), (int) subY, textColor, false);

        guiGraphics.text(mc.font, "REPAIR", (int) apX, (int) (subY + 10.0D * scale), textColor, false);
        guiGraphics.text(mc.font, String.valueOf(repairCount), (int) (apX + 60.0D * scale), (int) (subY + 10.0D * scale), textColor, false);

        // 3. 中央下表示 (ENゲージバー)
        double enWidth = 120.0D * scale;
        double enHeight = 3.0D * scale;
        double enX = centerX - enWidth / 2.0D;
        double enY = height - 35.0D * scale;
        float foodRate = player.getFoodData().getFoodLevel() / 20.0F;
        guiGraphics.fill((int) enX, (int) enY, (int) (enX + enWidth), (int) (enY + enHeight), 0x33FFFFFF & frameColor);
        guiGraphics.fill((int) enX, (int) enY, (int) (enX + enWidth * foodRate), (int) (enY + enHeight), frameColor);
        guiGraphics.horizontalLine((int) (enX - 5.0D), (int) (enX + enWidth + 5.0D), (int) (enY + enHeight + 2.0D), frameColor);

        // 4. 右下表示 (武器残弾数 / 耐久値)
        double wpX = width - 120.0D * scale;
        double wpY = height - 50.0D * scale;

        if (!mainHand.isEmpty()) {
            String raName = mainHand.getItem().getName(mainHand).getString();
            if (raName.length() > 10) raName = raName.substring(0, 10);
            guiGraphics.text(mc.font, "RA  " + raName, (int) wpX, (int) wpY, textColor, false);
            String raAmmo = "-";
            if (mainHand.isDamageableItem()) {
                raAmmo = String.valueOf(mainHand.getMaxDamage() - mainHand.getDamageValue());
            } else if (mainHand.is(Items.BOW) || mainHand.is(Items.CROSSBOW)) {
                boolean isBow = mainHand.is(Items.BOW);
                int ammoCount = IntStream.range(0, player.getInventory().getContainerSize())
                        .mapToObj(player.getInventory()::getItem)
                        .filter(stack -> !stack.isEmpty())
                        .filter(stack -> isBow ? stack.is(ItemTags.ARROWS) : (stack.is(ItemTags.ARROWS) || stack.is(Items.FIREWORK_ROCKET)))
                        .mapToInt(ItemStack::getCount)
                        .sum();
                raAmmo = String.valueOf(ammoCount);
            }
            guiGraphics.text(mc.font, raAmmo, (int) (wpX + 80.0D * scale), (int) wpY, textColor, false);
        }

        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty()) {
            String laName = offHand.getItem().getName(offHand).getString();
            if (laName.length() > 10) laName = laName.substring(0, 10);
            guiGraphics.text(mc.font, "LA  " + laName, (int) wpX, (int) (wpY + 10.0D * scale), textColor, false);
            String laAmmo = "-";
            if (offHand.isDamageableItem()) {
                laAmmo = String.valueOf(offHand.getMaxDamage() - offHand.getDamageValue());
            }
            guiGraphics.text(mc.font, laAmmo, (int) (wpX + 80.0D * scale), (int) (wpY + 10.0D * scale), textColor, false);
        }

        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestArmor.isEmpty()) {
            String rbName = chestArmor.getItem().getName(chestArmor).getString();
            if (rbName.length() > 10) rbName = rbName.substring(0, 10);
            guiGraphics.text(mc.font, "RB  " + rbName, (int) wpX, (int) (wpY - 20.0D * scale), textColor, false);
            String rbVal = chestArmor.isDamageableItem() ? String.valueOf(chestArmor.getMaxDamage() - chestArmor.getDamageValue()) : "-";
            guiGraphics.text(mc.font, rbVal, (int) (wpX + 80.0D * scale), (int) (wpY - 20.0D * scale), textColor, false);
        }

        ItemStack legsArmor = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!legsArmor.isEmpty()) {
            String lbName = legsArmor.getItem().getName(legsArmor).getString();
            if (lbName.length() > 10) lbName = lbName.substring(0, 10);
            guiGraphics.text(mc.font, "LB  " + lbName, (int) wpX, (int) (wpY - 10.0D * scale), textColor, false);
            String lbVal = legsArmor.isDamageableItem() ? String.valueOf(legsArmor.getMaxDamage() - legsArmor.getDamageValue()) : "-";
            guiGraphics.text(mc.font, lbVal, (int) (wpX + 80.0D * scale), (int) (wpY - 10.0D * scale), textColor, false);
        }
    }

}
