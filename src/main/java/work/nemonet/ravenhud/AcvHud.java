package work.nemonet.ravenhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class AcvHud {

    public void render(GuiGraphicsExtractor guiGraphics, LocalPlayer player, Minecraft mc, int width, int height,
                       double centerX, double centerY, RavenHUDOverlay.HudColors colors,
                       float lineR, float lineG, float lineB, float lineAlpha, double currentSpeed) {
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
        HudDraw.drawArch(guiGraphics, mainColor, centerX + offset, centerY, outerRadius, thick, 90.0D, 90.0D + 90.0D * hungerVal);

        // フレーム
        HudDraw.drawArch(guiGraphics, subColor, centerX + offset, centerY, outerRadius - thick, outLineThick, 90.0D, 180.0D);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset + outerRadius - thick + thick * 4), (float) centerY,
                (float) (centerX + offset + outerRadius - thick), (float) centerY, subColor);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset), (float) (centerY + frameOuterRadius),
                (float) (centerX + offset), (float) (centerY + frameOuterRadius + thick * 4), subColor);

        // 左下 180-*270 (体力)
        float healthVal = Math.max(player.getHealth() / player.getMaxHealth(), 0.0F);
        HudDraw.drawArch(guiGraphics, mainColor, centerX - offset, centerY, outerRadius, thick, 180.0D, 180.0D + 90.0D * healthVal);

        // フレーム
        HudDraw.drawArch(guiGraphics, subColor, centerX - offset, centerY, outerRadius - thick, outLineThick, 180.0D, 270.0D);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset - outerRadius + thick - thick * 4), (float) centerY,
                (float) (centerX - offset - outerRadius + thick), (float) centerY, subColor);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY + frameOuterRadius),
                (float) (centerX - offset), (float) (centerY + frameOuterRadius + thick * 4), subColor);

        float spikeThick = 0.6F * scale;
        float spikeLength = 6.0F * scale;

        // 右下内側 (速度)
        double speedVal = currentSpeed / 100.0D;
        float speedFactor = (float) (1.0D - Math.min(Math.sqrt(speedVal) / 2.0D, 1.0D));
        HudDraw.drawArch(guiGraphics, mainColor, centerX + offset, centerY, radius, thick, 105.0D + 75.0D * speedFactor, 180.0D);

        // フレーム
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameOuterRadius, 100.0D, 180.0D);
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameInnerRadius, 100.0D, 180.0D);

        // フレーム端
        float startX = (float) (Math.sin(100.0 * Math.PI / 180.0) * frameOuterRadius);
        float startY = (float) (Math.cos(100.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset + startX), (float) (centerY - startY),
                (float) (centerX + offset + startX - thick * 5), (float) (centerY - startY), whiteColor);
        float endX = (float) (Math.sin(180.0 * Math.PI / 180.0) * frameOuterRadius);
        float endY = (float) (Math.cos(180.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset + endX), (float) (centerY - endY),
                (float) (centerX + offset + endX), (float) (centerY - endY - thick * 5), whiteColor);

        // とげ
        float spikeSin = (float) Math.sin(135.0 * Math.PI / 180.0);
        float spikeCos = (float) Math.cos(135.0 * Math.PI / 180.0);
        float spikeBottomX = (float) (centerX + offset + spikeSin * frameInnerRadius);
        float spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        float spikeTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength));
        float spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        HudDraw.drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        float spikeTopTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        float spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        HudDraw.drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 左下内側 (高度)
        float posHeightVal = (float) player.getY();
        HudDraw.drawArch(guiGraphics, mainColor, centerX - offset, centerY, radius, thick, 180.0D, 180.0D + 75.0D * Math.max(0.0F, Math.min(posHeightVal, 256.0F)) / 256.0F);

        // フレーム
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameOuterRadius, 180.0D, 260.0D);
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameInnerRadius, 180.0D, 260.0D);

        // フレーム端
        startX = (float) (Math.sin(180.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(180.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset + startX), (float) (centerY - startY),
                (float) (centerX - offset + startX), (float) (centerY - startY - thick * 5), whiteColor);
        endX = (float) (Math.sin(260.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(260.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset + endX), (float) (centerY - endY),
                (float) (centerX - offset + endX + thick * 5), (float) (centerY - endY), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(225.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(225.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX - offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        HudDraw.drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        HudDraw.drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 右上 0-*75 (メインハンドツール耐久値)
        ItemStack mainHand = player.getMainHandItem();
        int heldDurableVal = mainHand.getMaxDamage() - mainHand.getDamageValue();
        if (mainHand.isDamageableItem()) {
            HudDraw.drawArch(guiGraphics, mainColor, centerX + offset, centerY, radius, thick, 0.0D, 75.0D * heldDurableVal / mainHand.getMaxDamage());
        }
        // フレーム
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameOuterRadius, 0.0D, 80.0D);
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX + offset, centerY, frameInnerRadius, 0.0D, 80.0D);
        startX = (float) (Math.sin(0.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(0.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset + startX), (float) (centerY - startY),
                (float) (centerX + offset + startX), (float) (centerY - startY + thick * 5), whiteColor);
        endX = (float) (Math.sin(80.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(80.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX + offset + endX), (float) (centerY - endY),
                (float) (centerX + offset + endX - thick * 5), (float) (centerY - endY), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(45.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(45.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX + offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        HudDraw.drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX + offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        HudDraw.drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 左上 *285-360 (アーマー値)
        float armorVal = player.getArmorValue() / 20.0F;
        if (armorVal > 0.0F) {
            HudDraw.drawArch(guiGraphics, mainColor, centerX - offset, centerY, radius, thick, 285.0D + 75.0D * (1.0F - armorVal), 360.0D);
        }
        // フレーム
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameOuterRadius, 280.0D, 360.0D);
        HudDraw.drawArchLine(guiGraphics, whiteColor, centerX - offset, centerY, frameInnerRadius, 280.0D, 360.0D);
        startX = (float) (Math.sin(280.0 * Math.PI / 180.0) * frameOuterRadius);
        startY = (float) (Math.cos(280.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset + startX), (float) (centerY - startY),
                (float) (centerX - offset + startX + thick * 5), (float) (centerY - startY), whiteColor);
        endX = (float) (Math.sin(360.0 * Math.PI / 180.0) * frameOuterRadius);
        endY = (float) (Math.cos(360.0 * Math.PI / 180.0) * frameOuterRadius);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset + endX), (float) (centerY - endY),
                (float) (centerX - offset + endX), (float) (centerY - endY + thick * 5), whiteColor);

        // とげ
        spikeSin = (float) Math.sin(315.0 * Math.PI / 180.0);
        spikeCos = (float) Math.cos(315.0 * Math.PI / 180.0);
        spikeBottomX = (float) (centerX - offset + spikeSin * frameInnerRadius);
        spikeBottomY = (float) (centerY - spikeCos * frameInnerRadius);
        spikeTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength));
        spikeTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength));
        HudDraw.drawLine(guiGraphics, spikeBottomX, spikeBottomY, spikeTopX, spikeTopY, redColor);
        spikeTopTopX = (float) (centerX - offset + spikeSin * (frameInnerRadius - spikeLength * 2));
        spikeTopTopY = (float) (centerY - spikeCos * (frameInnerRadius - spikeLength * 2));
        HudDraw.drawLine(guiGraphics, spikeTopX, spikeTopY, spikeTopTopX, spikeTopTopY, redColor);

        // 残りのフレーム (上下の水平線)
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY - frameOuterRadius),
                (float) (centerX + offset), (float) (centerY - frameOuterRadius), whiteColor);
        HudDraw.drawLine(guiGraphics, (float) (centerX - offset), (float) (centerY + frameOuterRadius),
                (float) (centerX + offset), (float) (centerY + frameOuterRadius), whiteColor);

        // 三角形 (方位指針)
        float yaw = -player.getYRot();
        float triBaseX = (float) (Math.sin(yaw * Math.PI / 180.0) * (radius + 12.5F * scale));
        float triBaseY = (float) (Math.cos(yaw * Math.PI / 180.0) * (radius + 12.5F * scale));
        float triRightX = (float) (Math.sin((yaw + 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triRightY = (float) (Math.cos((yaw + 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triLeftX = (float) (Math.sin((yaw - 2) * Math.PI / 180.0) * (radius + 15.0F * scale));
        float triLeftY = (float) (Math.cos((yaw - 2) * Math.PI / 180.0) * (radius + 15.0F * scale));

        HudDraw.drawPolygon(guiGraphics, whiteColor,
                centerX + triBaseX, centerY - triBaseY,
                centerX + triRightX, centerY - triRightY,
                centerX + triLeftX, centerY - triLeftY);

        HudDraw.drawPolygon(guiGraphics, whiteColor,
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
}
