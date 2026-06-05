package work.nemonet.ravenhud;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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

        // カラー値のパースと不変レコード化
        HudColors colors = new HudColors(
                parseColor(Config.COLOR_NORMAL.get(), 0xC7FFAE00),
                parseColor(Config.COLOR_WARNING.get(), 0xE5FFFF00),
                parseColor(Config.COLOR_ALERT.get(), 0xE5FF0000),
                parseColor(Config.COLOR_NUMBER.get(), 0xCCD09000),
                parseColor(Config.COLOR_IRON.get(), 0x807050),
                parseColor(Config.COLOR_GOLD.get(), 0x999000),
                parseColor(Config.COLOR_DIAMOND.get(), 0x009999),
                parseColor(Config.COLOR_LAVA.get(), 0xFF3000)
        );

        float lineR = ((colors.normal() >> 16) & 0xFF) / 255.0F;
        float lineG = ((colors.normal() >> 8) & 0xFF) / 255.0F;
        float lineB = (colors.normal() & 0xFF) / 255.0F;
        float lineAlpha = Config.LINE_ALPHA.get().floatValue();

        Config.HudMode mode = Config.HUD_MODE.get();

        if (mode == Config.HudMode.RAVEN) {
            renderRaven(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
        } else {
            renderTypeB(guiGraphics, player, mc, width, height, midX, midY, colors, lineR, lineG, lineB, lineAlpha);
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

    // Modern 2D Polygon Render using addVertexWith2DPose and RenderTypes
    private void drawPolygon(GuiGraphicsExtractor guiGraphics, int color, double... vertices) {
        if (vertices.length < 6 || vertices.length % 2 != 0) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        for (int k = 0; k < vertices.length; k += 2) {
            builder.addVertexWith2DPose(guiGraphics.pose(), (float) vertices[k], (float) vertices[k + 1]).setColor(color);
        }

        RenderTypes.debugTriangleFan().draw(builder.buildOrThrow());
    }

    // Modern 2D Line Loop Render using addVertexWith2DPose and RenderTypes
    private void drawLineLoop(GuiGraphicsExtractor guiGraphics, int color, double... vertices) {
        if (vertices.length < 4 || vertices.length % 2 != 0) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int k = 0; k < vertices.length; k += 2) {
            builder.addVertexWith2DPose(guiGraphics.pose(), (float) vertices[k], (float) vertices[k + 1]).setColor(color);
        }
        builder.addVertexWith2DPose(guiGraphics.pose(), (float) vertices[0], (float) vertices[1]).setColor(color);

        RenderTypes.linesTranslucent().draw(builder.buildOrThrow());
    }

    // Modern 2D Line Render using addVertexWith2DPose and RenderTypes
    private void drawLine(GuiGraphicsExtractor guiGraphics, float x1, float y1, float x2, float y2, int color) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        builder.addVertexWith2DPose(guiGraphics.pose(), x1, y1).setColor(color);
        builder.addVertexWith2DPose(guiGraphics.pose(), x2, y2).setColor(color);

        RenderTypes.linesTranslucent().draw(builder.buildOrThrow());
    }
}
