package work.nemonet.ravenhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class TypeBHud {

    public void render(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int w, int j, double d, double d1,
                       RavenHUDOverlay.HudColors colors, float lineR, float lineG, float lineB, float lineAlpha,
                       double currentSpeed) {

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
                HudDraw.drawLine(guiGraphics, (float) tickX, (float) (posY - len), (float) tickX, (float) posY, frameColor);
            }
            d2 += 5.0D;
        }

        // 中央マーカー
        double d3 = d1 - 90.0D;
        HudDraw.drawPolygon(guiGraphics, frameColor, d, d3, d + 2.0D, d3 + 3.0D, d - 2.0D, d3 + 3.0D);

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
                    HudDraw.drawLine(guiGraphics, (float) posX, (float) d3, (float) (posX - len), (float) d3, frameColor);
                }
            }
            d3 += 5.0D;
        }
        // スピード表示用数値枠
        HudDraw.drawLineLoop(guiGraphics, frameColor,
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
                    HudDraw.drawLine(guiGraphics, (float) posX, (float) d3, (float) (posX + len), (float) d3, frameColor);
                }
            }
            d3 += 5.0D;
        }
        // 高度表示用数値枠
        HudDraw.drawLineLoop(guiGraphics, frameColor,
                d + 115.0D, d1 - 4.0D,
                d + 160.0D, d1 - 4.0D,
                d + 160.0D, d1 + 7.0D,
                d + 115.0D, d1 + 7.0D);

        // アーマー枠
        HudDraw.drawLineLoop(guiGraphics, frameColor,
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
        List<RavenHUDOverlay.ArmorStatus> armors = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(slot -> new RavenHUDOverlay.ArmorStatus(player.getItemBySlot(slot), slot))
                .toList();

        int armorDrawX = (int) d - 143;
        int armorDrawY = j - 62;

        for (RavenHUDOverlay.ArmorStatus armor : armors) {
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

    private static int getArmorColor(ItemStack stack, RavenHUDOverlay.HudColors colors) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return colors.normal();
        float ratio = (float) stack.getDamageValue() / (float) stack.getMaxDamage();
        if (ratio < 0.2F) return colors.normal();
        if (ratio < 0.5F) return colors.number();
        if (ratio < 0.8F) return colors.warning();
        return colors.alert();
    }

    private static int parseColor(String hex, int defaultColor) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    private static boolean tickXInRange(double x, int min, int max) {
        return x >= min && x <= max;
    }
}
