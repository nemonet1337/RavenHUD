package work.nemonet.ravenhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public final class AC6Hud {

    private LivingEntity lockedTarget = null;
    private long lockStartTick = 0;

    public void render(GuiGraphicsExtractor g, LocalPlayer player, Minecraft mc,
                       int width, int height, double centerX, double centerY,
                       RavenHUDOverlay.HudColors colors, float lineAlpha,
                       double currentSpeed) {

        float scale = height / 240.0F;
        long gameTick = mc.level != null ? mc.level.getGameTime() : 0;
        int frameColor = ((int)(lineAlpha * 255) << 24) | (colors.normal() & 0x00FFFFFF);
        int textColor  = colors.number();
        int alertColor = colors.alert();
        int warnColor  = colors.warning();

        // --- ターゲット更新 ---
        updateLock(player, mc, gameTick);
        float hpRate = player.getHealth() / player.getMaxHealth();

        // =========================================================
        // A. ミッション目標テキスト (左上)
        // =========================================================
        if (Config.AC6_SHOW_MISSION.get()) {
            String mission = Config.AC6_MISSION_TEXT.get();
            g.text(mc.font, mission, (int)(15 * scale), (int)(15 * scale), textColor, false);
        }

        // =========================================================
        // SPEED 縦型速度計 (左端)
        // =========================================================
        renderSpeedGauge(g, mc, width, height, scale, currentSpeed, frameColor, textColor);

        // =========================================================
        // ALT 縦型高度計 (右端)
        // =========================================================
        renderAltGauge(g, mc, width, height, scale, player.getY(), frameColor, textColor);

        // =========================================================
        // 中央レティクル (F/K/L + ターゲットG/H/I/J/M)
        // =========================================================
        renderReticle(g, mc, player, width, height, centerX, centerY, scale, gameTick,
                      frameColor, textColor, alertColor, warnColor, hpRate);

        // =========================================================
        // B. AP + APゲージ (左下)
        // =========================================================
        double apX = 20.0D * scale;
        double apY = height - 70.0D * scale;
        renderAPSection(g, mc, player, apX, apY, scale, frameColor, textColor, hpRate);

        // =========================================================
        // C/D. EXPANSION / REPAIR / SCAN (左下、APの下)
        // =========================================================
        renderSubStatus(g, mc, player, apX, apY + 22.0D * scale, scale, frameColor, textColor, gameTick);

        // =========================================================
        // N. ENゲージ (中央下)
        // =========================================================
        double enWidth  = 150.0D * scale;
        double enHeight = 3.0D * scale;
        double enX = centerX - enWidth / 2.0D;
        double enY = height - 30.0D * scale;
        float foodRate = player.getFoodData().getFoodLevel() / 20.0F;
        g.fill((int)enX, (int)enY, (int)(enX + enWidth), (int)(enY + enHeight),
               0x33FFFFFF & frameColor);
        g.fill((int)enX, (int)enY, (int)(enX + enWidth * foodRate), (int)(enY + enHeight),
               frameColor);
        g.horizontalLine((int)(enX - 5), (int)(enX + enWidth + 5), (int)(enY + enHeight + 1), frameColor);

        // E. 武器readyドット列 (ENバーの上)
        renderWeaponReadyDots(g, player, mc, centerX, enY - 6 * scale, scale, frameColor, alertColor, gameTick);

        // =========================================================
        // R. 装甲パネル (右下)
        // =========================================================
        renderArmorPanel(g, mc, player, width, height, scale, frameColor, textColor, alertColor, gameTick);

        // =========================================================
        // Q. レーダー (右上)
        // =========================================================
        if (Config.AC6_SHOW_RADAR.get()) {
            renderRadar(g, player, mc, width, scale, frameColor, textColor, alertColor);
        }
    }

    // ---- ターゲット追跡 ----
    private void updateLock(LocalPlayer player, Minecraft mc, long gameTick) {
        if (!Config.AC6_LOCKON_ENABLED.get()) {
            lockedTarget = null;
            return;
        }

        LivingEntity candidate = null;

        // 既存のhitResult (3ブロック圏内優先)
        if (mc.hitResult instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof LivingEntity le
                && le.isAlive()) {
            candidate = le;
        }

        // 長距離レイトレース: 視線方向で最も近いエンティティを探す
        if (candidate == null) {
            double reach = Config.AC6_LOCK_RANGE.get();
            Vec3 eye  = player.getEyePosition(1.0f);
            Vec3 look = player.getViewVector(1.0f);
            Vec3 end  = eye.add(look.scale(reach));
            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(2.0);
            List<LivingEntity> nearby = mc.level.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator());
            double bestDist = Double.MAX_VALUE;
            for (LivingEntity e : nearby) {
                AABB eb = e.getBoundingBox().inflate(0.5);
                var hitOpt = eb.clip(eye, end);
                if (hitOpt.isPresent()) {
                    double d = eye.distanceToSqr(hitOpt.get());
                    if (d < bestDist) { bestDist = d; candidate = e; }
                }
            }
        }

        if (candidate != null) {
            if (lockedTarget != candidate) { lockStartTick = gameTick; }
            lockedTarget = candidate;
        } else {
            lockedTarget = null;
        }
    }

    // ---- 中央レティクル + ターゲット表示 ----
    private void renderReticle(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player,
                                int width, int height,
                                double centerX, double centerY, float scale, long gameTick,
                                int frameColor, int textColor, int alertColor, int warnColor,
                                float hpRate) {

        double retRadius = 35.0D * scale;
        double retThick  =  2.0D * scale;

        // L. 左弧 HP (120-240°)
        double leftEnd = 120.0D + 120.0D * hpRate;
        HudDraw.drawArch(g, frameColor, centerX - 5.0D * scale, centerY, retRadius, retThick, 120.0D, leftEnd);
        HudDraw.drawArchLine(g, frameColor, centerX - 5.0D * scale, centerY, retRadius + retThick, 120.0D, 240.0D);

        // L. 右弧 メインハンド耐久 or 満腹度 (300-420°)
        ItemStack mainHand = player.getMainHandItem();
        float rightRate = mainHand.isDamageableItem()
                ? 1.0F - (float) mainHand.getDamageValue() / mainHand.getMaxDamage()
                : player.getFoodData().getFoodLevel() / 20.0F;
        double rightEnd = 300.0D + 120.0D * rightRate;
        HudDraw.drawArch(g, frameColor, centerX + 5.0D * scale, centerY, retRadius, retThick, 300.0D, rightEnd);
        HudDraw.drawArchLine(g, frameColor, centerX + 5.0D * scale, centerY, retRadius + retThick, 300.0D, 420.0D);

        // F. 中央クロス
        float cs = (float)(4 * scale);
        HudDraw.drawLine(g, (float)(centerX - cs), (float)centerY, (float)(centerX + cs), (float)centerY, frameColor);
        HudDraw.drawLine(g, (float)centerX, (float)(centerY - cs), (float)centerX, (float)(centerY + cs), frameColor);

        // I/J. 小三角形装飾 (レティクル上下)
        float tri = (float)(3 * scale);
        HudDraw.drawPolygon(g, frameColor,
            centerX, centerY - retRadius - tri * 2,
            centerX - tri, centerY - retRadius - tri * 4,
            centerX + tri, centerY - retRadius - tri * 4);
        HudDraw.drawPolygon(g, frameColor,
            centerX, centerY + retRadius + tri * 2,
            centerX - tri, centerY + retRadius + tri * 4,
            centerX + tri, centerY + retRadius + tri * 4);

        // 高度テキスト (レティクル下)
        String altStr = String.format("%.0fm", player.getY());
        g.text(mc.font, altStr,
               (int)(centerX - mc.font.width(altStr) / 2.0D),
               (int)(centerY + retRadius + 5.0D * scale), textColor, false);

        // K. ロックオン円 (対象ありの場合アニメーション)
        if (lockedTarget != null) {
            float lockProgress = Math.min((gameTick - lockStartTick) / 10.0f, 1.0f);
            double lockRadius = retRadius * (2.0 - lockProgress); // 縮小アニメーション
            HudDraw.drawArchLine(g, frameColor, centerX, centerY, lockRadius, 0.0D, 360.0D);

            // G. ロック枠 (簡易: 中央からの方向にブラケット)
            float bSize = (float)(12 * scale);
            float bX = (float)centerX, bY = (float)centerY;
            // 4隅ブラケット
            drawBracket(g, bX, bY, bSize, frameColor);

            // H. 敵名
            String name = lockedTarget.getDisplayName().getString();
            if (lockedTarget instanceof Mob mob && mob.getTarget() == player) {
                name += " AWARE";
            } else if (lockedTarget instanceof Mob) {
                name += " UNAWARE";
            }
            g.text(mc.font, name,
                   (int)(centerX - mc.font.width(name) / 2.0D),
                   (int)(centerY - retRadius - 18 * scale), textColor, false);

            // 距離
            float dist = player.distanceTo(lockedTarget);
            String distStr = String.format("%.0fm", dist);
            g.text(mc.font, distStr,
                   (int)(centerX - mc.font.width(distStr) / 2.0D),
                   (int)(centerY + retRadius * 1.2 + 10 * scale), textColor, false);

            // M. ACS ANOMALY / スタッガーゲージ (敵HP低下を擬似スタッガーとして表示)
            float acsRate = 1.0f - lockedTarget.getHealth() / lockedTarget.getMaxHealth();
            double acsWidth = 80.0D * scale;
            double acsX = centerX - acsWidth / 2.0D;
            double acsY = centerY + retRadius + 20.0D * scale;
            g.fill((int)acsX, (int)acsY, (int)(acsX + acsWidth), (int)(acsY + 2 * scale),
                   0x33FFFFFF & frameColor);
            int acsColor = acsRate > 0.7f ? alertColor : warnColor;
            g.fill((int)acsX, (int)acsY, (int)(acsX + acsWidth * acsRate), (int)(acsY + 2 * scale),
                   acsColor);
            if (acsRate > 0.5f && gameTick % 3 != 1) {
                String acsText = "ACS ANOMALY";
                g.text(mc.font, acsText,
                       (int)(centerX - mc.font.width(acsText) / 2.0D),
                       (int)(acsY + 4 * scale), alertColor, false);
            }
        }
    }

    private void drawBracket(GuiGraphicsExtractor g, float cx, float cy, float size, int color) {
        float half = size / 2;
        float arm  = size / 3;
        // 左上
        HudDraw.drawLine(g, cx - half,      cy - half,      cx - half + arm, cy - half,      color);
        HudDraw.drawLine(g, cx - half,      cy - half,      cx - half,       cy - half + arm, color);
        // 右上
        HudDraw.drawLine(g, cx + half,      cy - half,      cx + half - arm, cy - half,      color);
        HudDraw.drawLine(g, cx + half,      cy - half,      cx + half,       cy - half + arm, color);
        // 左下
        HudDraw.drawLine(g, cx - half,      cy + half,      cx - half + arm, cy + half,      color);
        HudDraw.drawLine(g, cx - half,      cy + half,      cx - half,       cy + half - arm, color);
        // 右下
        HudDraw.drawLine(g, cx + half,      cy + half,      cx + half - arm, cy + half,      color);
        HudDraw.drawLine(g, cx + half,      cy + half,      cx + half,       cy + half - arm, color);
    }

    // ---- 速度計 ----
    private void renderSpeedGauge(GuiGraphicsExtractor g, Minecraft mc,
                                   int width, int height, float scale,
                                   double speed, int frameColor, int textColor) {
        int gaugeX = (int)(12 * scale);
        int gaugeTop = (int)(height * 0.3);
        int gaugeBot = (int)(height * 0.7);
        int gaugeH = gaugeBot - gaugeTop;

        g.verticalLine(gaugeX, gaugeTop, gaugeBot, frameColor);
        float maxSpeed = 200.0f;
        float fillRate = (float) Math.min(speed / maxSpeed, 1.0);
        int fillTop = gaugeBot - (int)(gaugeH * fillRate);
        g.fill(gaugeX - 2, fillTop, gaugeX + 2, gaugeBot, frameColor);

        for (int i = 0; i <= 4; i++) {
            int ty = gaugeTop + gaugeH * i / 4;
            g.horizontalLine(gaugeX - 3, gaugeX + 3, ty, frameColor);
        }

        g.text(mc.font, "SPEED", gaugeX - mc.font.width("SPEED") / 2, gaugeTop - 10, textColor, false);
        String speedStr = String.format("%.0f", speed);
        g.text(mc.font, speedStr, gaugeX - mc.font.width(speedStr) / 2, gaugeBot + 2, textColor, false);
    }

    // ---- 高度計 ----
    private void renderAltGauge(GuiGraphicsExtractor g, Minecraft mc,
                                  int width, int height, float scale,
                                  double playerY, int frameColor, int textColor) {
        int gaugeX = width - (int)(12 * scale);
        int gaugeTop = (int)(height * 0.3);
        int gaugeBot = (int)(height * 0.7);
        int gaugeH = gaugeBot - gaugeTop;

        g.verticalLine(gaugeX, gaugeTop, gaugeBot, frameColor);
        float maxAlt = 320.0f;
        float fillRate = (float) Math.min(Math.max(playerY / maxAlt, 0), 1.0);
        int fillTop = gaugeBot - (int)(gaugeH * fillRate);
        g.fill(gaugeX - 2, fillTop, gaugeX + 2, gaugeBot, frameColor);

        for (int i = 0; i <= 4; i++) {
            int ty = gaugeTop + gaugeH * i / 4;
            g.horizontalLine(gaugeX - 3, gaugeX + 3, ty, frameColor);
        }

        g.text(mc.font, "ALT", gaugeX - mc.font.width("ALT") / 2, gaugeTop - 10, textColor, false);
        String altStr = String.format("%.0f", playerY);
        g.text(mc.font, altStr, gaugeX - mc.font.width(altStr) / 2, gaugeBot + 2, textColor, false);
    }

    // ---- AP セクション ----
    private void renderAPSection(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player,
                                  double apX, double apY, float scale,
                                  int frameColor, int textColor, float hpRate) {
        g.text(mc.font, "AP", (int)apX, (int)apY, textColor, false);
        String apVal = String.format("%05d", (int)(player.getHealth() * 500));
        g.text(mc.font, apVal, (int)(apX + 20.0D * scale), (int)(apY - 2.0D * scale), textColor, false);

        double barW = 110.0D * scale, barH = 2.0D * scale;
        g.fill((int)apX, (int)(apY + 12.0D * scale), (int)(apX + barW), (int)(apY + 12.0D * scale + barH),
               0x33FFFFFF & frameColor);
        g.fill((int)apX, (int)(apY + 12.0D * scale), (int)(apX + barW * hpRate), (int)(apY + 12.0D * scale + barH),
               frameColor);
    }

    // ---- EXPANSION / REPAIR / SCAN ----
    private void renderSubStatus(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player,
                                  double apX, double subY, float scale,
                                  int frameColor, int textColor, long gameTick) {
        // EXPANSION (トーテム数)
        int expansionCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.TOTEM_OF_UNDYING)) expansionCount += s.getCount();
        }
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))
            expansionCount += player.getOffhandItem().getCount();

        g.text(mc.font, "EXPANSION", (int)apX, (int)subY, textColor, false);
        g.text(mc.font, String.valueOf(expansionCount), (int)(apX + 65.0D * scale), (int)subY, textColor, false);

        // REPAIR (空腹度バー)
        float foodRate = player.getFoodData().getFoodLevel() / 20.0F;
        double repairY = subY + 10.0D * scale;
        g.text(mc.font, "REPAIR", (int)apX, (int)repairY, textColor, false);
        double repW = 40.0D * scale, repH = 2.0D * scale;
        g.fill((int)(apX + 44 * scale), (int)(repairY + 2), (int)(apX + 44 * scale + repW), (int)(repairY + 2 + repH),
               0x33FFFFFF & frameColor);
        g.fill((int)(apX + 44 * scale), (int)(repairY + 2), (int)(apX + 44 * scale + repW * foodRate), (int)(repairY + 2 + repH),
               frameColor);

        // SCAN (攻撃クールダウン)
        float scanRate = player.getAttackStrengthScale(0.0f);
        double scanY = subY + 20.0D * scale;
        g.text(mc.font, "SCAN", (int)apX, (int)scanY, textColor, false);
        double scanW = 40.0D * scale, scanH = 2.0D * scale;
        g.fill((int)(apX + 44 * scale), (int)(scanY + 2), (int)(apX + 44 * scale + scanW), (int)(scanY + 2 + scanH),
               0x33FFFFFF & frameColor);
        g.fill((int)(apX + 44 * scale), (int)(scanY + 2), (int)(apX + 44 * scale + scanW * scanRate), (int)(scanY + 2 + scanH),
               frameColor);
    }

    // ---- 武器readyドット ----
    private void renderWeaponReadyDots(GuiGraphicsExtractor g, LocalPlayer player, Minecraft mc,
                                        double centerX, double y, float scale,
                                        int frameColor, int alertColor, long gameTick) {
        int slots = 4;
        double dotW = 6 * scale, dotH = 3 * scale, gap = 4 * scale;
        double totalW = slots * (dotW + gap) - gap;
        double startX = centerX - totalW / 2.0;
        for (int i = 0; i < slots; i++) {
            double dx = startX + i * (dotW + gap);
            boolean ready = player.getAttackStrengthScale(0.0f) >= 1.0f;
            int dotColor = ready ? frameColor : (0x55FFFFFF & frameColor);
            g.fill((int)dx, (int)y, (int)(dx + dotW), (int)(y + dotH), dotColor);
        }
    }

    // ---- 装甲パネル (R) ----
    private void renderArmorPanel(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player,
                                   int width, int height, float scale,
                                   int frameColor, int textColor, int alertColor, long gameTick) {
        double wpX = width - 130.0D * scale;
        double wpY = height - 55.0D * scale;

        // RB (胸防具)
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        renderWeaponRow(g, mc, "RB", chest, wpX, wpY - 20 * scale, scale, frameColor, textColor, alertColor, gameTick);

        // LB (足防具)
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        renderWeaponRow(g, mc, "LB", legs, wpX, wpY - 10 * scale, scale, frameColor, textColor, alertColor, gameTick);

        // RA (メインハンド)
        ItemStack mainHand = player.getMainHandItem();
        renderWeaponRowAmmo(g, mc, "RA", mainHand, player, wpX, wpY, scale, frameColor, textColor, alertColor, gameTick);

        // LA (オフハンド)
        ItemStack offHand = player.getOffhandItem();
        renderWeaponRow(g, mc, "LA", offHand, wpX, wpY + 10 * scale, scale, frameColor, textColor, alertColor, gameTick);
    }

    private void renderWeaponRow(GuiGraphicsExtractor g, Minecraft mc, String label,
                                  ItemStack stack, double x, double y, float scale,
                                  int frameColor, int textColor, int alertColor, long gameTick) {
        g.text(mc.font, label, (int)x, (int)y, textColor, false);
        if (!stack.isEmpty() && stack.isDamageableItem()) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            boolean overheat = (float)stack.getDamageValue() / stack.getMaxDamage() > 0.9f;
            if (overheat && gameTick % 3 != 1) {
                g.text(mc.font, "OVERHEAT", (int)(x + 18 * scale), (int)y, alertColor, false);
            } else {
                g.text(mc.font, String.valueOf(remaining), (int)(x + 18 * scale), (int)y, textColor, false);
            }
            // 耐久バー
            double barW = 60.0D * scale, barH = 1.5D * scale;
            float ratio = (float)remaining / stack.getMaxDamage();
            g.fill((int)(x + 18 * scale), (int)(y + mc.font.lineHeight + 1),
                   (int)(x + 18 * scale + barW), (int)(y + mc.font.lineHeight + 1 + barH),
                   0x33FFFFFF & frameColor);
            g.fill((int)(x + 18 * scale), (int)(y + mc.font.lineHeight + 1),
                   (int)(x + 18 * scale + barW * ratio), (int)(y + mc.font.lineHeight + 1 + barH),
                   overheat ? alertColor : frameColor);
        } else if (!stack.isEmpty()) {
            g.text(mc.font, "-", (int)(x + 18 * scale), (int)y, textColor, false);
        }
    }

    private void renderWeaponRowAmmo(GuiGraphicsExtractor g, Minecraft mc, String label,
                                      ItemStack stack, LocalPlayer player,
                                      double x, double y, float scale,
                                      int frameColor, int textColor, int alertColor, long gameTick) {
        if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW)) {
            boolean isBow = stack.is(Items.BOW);
            int ammo = IntStream.range(0, player.getInventory().getContainerSize())
                .mapToObj(player.getInventory()::getItem)
                .filter(s -> !s.isEmpty())
                .filter(s -> isBow ? s.is(ItemTags.ARROWS)
                                   : (s.is(ItemTags.ARROWS) || s.is(Items.FIREWORK_ROCKET)))
                .mapToInt(ItemStack::getCount).sum();
            g.text(mc.font, label, (int)x, (int)y, textColor, false);
            g.text(mc.font, String.valueOf(ammo), (int)(x + 18 * scale), (int)y,
                   ammo <= 10 ? alertColor : textColor, false);
        } else {
            renderWeaponRow(g, mc, label, stack, x, y, scale, frameColor, textColor, alertColor, gameTick);
        }
    }

    // ---- レーダー (Q) ----
    private void renderRadar(GuiGraphicsExtractor g, LocalPlayer player, Minecraft mc,
                              int width, float scale, int frameColor, int textColor, int alertColor) {
        double radarCX = width - 60.0D * scale;
        double radarCY = 45.0D * scale;
        double radarR  = 25.0D * scale;
        double range   = Config.AC6_RADAR_RANGE.get();

        HudDraw.drawArchLine(g, frameColor, radarCX, radarCY, radarR, 0.0D, 360.0D);

        // 十字線
        HudDraw.drawLine(g, (float)(radarCX - radarR * 0.3), (float)radarCY,
                         (float)(radarCX + radarR * 0.3), (float)radarCY, frameColor);
        HudDraw.drawLine(g, (float)radarCX, (float)(radarCY - radarR * 0.3),
                         (float)radarCX, (float)(radarCY + radarR * 0.3), frameColor);

        if (mc.level == null) return;
        AABB scan = player.getBoundingBox().inflate(range);
        List<LivingEntity> nearby = mc.level.getEntitiesOfClass(
            LivingEntity.class, scan,
            e -> e != player && e.isAlive() && !e.isSpectator());

        float yawRad = (float) Math.toRadians(-player.getYRot());
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);

        nearby.stream()
              .sorted(Comparator.comparingDouble(e -> player.distanceToSqr(e)))
              .limit(12)
              .forEach(e -> {
                  double dx = e.getX() - player.getX();
                  double dz = e.getZ() - player.getZ();
                  double rx = dx * cosYaw - dz * sinYaw;
                  double rz = dx * sinYaw + dz * cosYaw;
                  double plotX = radarCX + rx / range * radarR;
                  double plotY = radarCY + rz / range * radarR;
                  float ps = (float)(2 * scale);
                  boolean isHostile = e instanceof Mob mob && mob.getTarget() instanceof LocalPlayer;
                  int dotColor = isHostile ? alertColor : frameColor;
                  g.fill((int)(plotX - ps), (int)(plotY - ps), (int)(plotX + ps), (int)(plotY + ps), dotColor);
              });
    }
}
