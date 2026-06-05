package net.minecraft.src;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class GuiRSHUD_AC extends GuiRSHUDConfigure {

    private GuiRSHUDSlider colornumber[];

    public int ColorInt_Number;
    public int ColorInt_Iron;
    public int ColorInt_Gold;
    public int ColorInt_Diamond;
    public int ColorInt_Lava;



    public GuiRSHUD_AC(BaseMod basemod){
		super(basemod);

		colornumber = new GuiRSHUDSlider[4];
	}

    @Override
    public String getHUDName() {
    	return "Raven";
    }

    @Override
    public void initGui()
    {
    	super.initGui();
        deg = (new GuiRSHUDSlider(50, hwSize + 60, (hhSize - 106) + 0, "Deg+", (float)mod_RSHUD_AC.DegOffset / 36F, 36F, 0.0F)).setStrFormat("%s : %.0f").setDisplayString();
        controlList.add(deg);
        linea = (new GuiRSHUDSlider(51, hwSize + 60, (hhSize - 106) + 24, "Line A", mod_RSHUD_AC.LineAlpha)).setDisplayString();
        controlList.add(linea);
        linew = (new GuiRSHUDSlider(52, hwSize + 60, (hhSize - 106) + 48, "LineWidth", (mod_RSHUD_AC.LineWidth - 0.5F) / 5F, 5F, 0.5F)).setDisplayString();
        controlList.add(linew);

        String as[] = {
            "\2474R", "\2472G", "\2471B", "A"
        };
        float af[] = getRGBA(ColorInt_Normal);

        for(int i = 0; i < 4; i++)
        {
            colornormal[i] = (new GuiRSHUDSlider(10 + i, hwSize - 205, hhSize + 14 + 24 * i, as[i], af[i])).setDisplayString();
            controlList.add(colornormal[i]);
        }

        af = getRGBA(ColorInt_Warning);
        for(int j = 0; j < 4; j++)
        {
            colorwarning[j] = (new GuiRSHUDSlider(20 + j, hwSize - 100, hhSize + 14 + 24 * j, as[j], af[j])).setDisplayString();
            controlList.add(colorwarning[j]);
        }

        af = getRGBA(ColorInt_Alert);
        for(int k = 0; k < 4; k++)
        {
            coloralert[k] = (new GuiRSHUDSlider(30 + k, hwSize + 5, hhSize + 14 + 24 * k, as[k], af[k])).setDisplayString();
            controlList.add(coloralert[k]);
        }

        af = getRGBA(ColorInt_Number);
        for(int l = 0; l < 4; l++)
        {
            colornumber[l] = (new GuiRSHUDSlider(40 + l, hwSize + 110, hhSize + 14 + 24 * l, as[l], af[l])).setDisplayString();
            controlList.add(colornumber[l]);
        }

    }

    @Override
    public void drawDefaultBackground() {
        drawGradientRect(hwSize + 50, hhSize - 116, hwSize + 170, hhSize - 4, 0xd0101010, 0xc0101010);
        drawGradientRect(hwSize - 210, hhSize - 16, hwSize + 210, hhSize + 116, 0xc0101010, 0xd0101010);
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        mod_RSHUD_AC.DegOffset = (int)deg.getSliderValue();
        mod_RSHUD_AC.LineAlpha = linea.getSliderValue();
        mod_RSHUD_AC.LineWidth = linew.getSliderValue();
        LineR = colornormal[0].getSliderValue();
        LineG = colornormal[1].getSliderValue();
        LineB = colornormal[2].getSliderValue();
        ColorInt_Normal = setRGBA(colornormal[0].getSliderValue(), colornormal[1].getSliderValue(), colornormal[2].getSliderValue(), colornormal[3].getSliderValue());
        ColorInt_Warning = setRGBA(colorwarning[0].getSliderValue(), colorwarning[1].getSliderValue(), colorwarning[2].getSliderValue(), colorwarning[3].getSliderValue());
        ColorInt_Alert = setRGBA(coloralert[0].getSliderValue(), coloralert[1].getSliderValue(), coloralert[2].getSliderValue(), coloralert[3].getSliderValue());
        ColorInt_Number = setRGBA(colornumber[0].getSliderValue(), colornumber[1].getSliderValue(), colornumber[2].getSliderValue(), colornumber[3].getSliderValue());
    	mod_RSHUD_AC.Color_Normal	= String.format("%08x", ColorInt_Normal);
    	mod_RSHUD_AC.Color_Warning	= String.format("%08x", ColorInt_Warning);
    	mod_RSHUD_AC.Color_Alert	= String.format("%08x", ColorInt_Alert);
    	mod_RSHUD_AC.Color_Number	= String.format("%08x", ColorInt_Number);

        String s = "LINE Color";
        drawString(fontRenderer, s, hwSize - 155 - fontRenderer.getStringWidth(s) / 2, hhSize - 12, ColorInt_Normal);
        s = "ORE Alpha";
        int NormalAl = (ColorInt_Normal / 0x1000000);
        int c1= (NormalAl * 0x1000000) + 0x807050;
        drawString(fontRenderer, s, hwSize - 155 - fontRenderer.getStringWidth(s) / 2, hhSize + 2, c1);

        s = "WARNING";
        drawString(fontRenderer, s, hwSize - 55 - fontRenderer.getStringWidth(s) / 2, hhSize + 2, ColorInt_Warning);
        s = "ALERT";
        drawString(fontRenderer, s, (hwSize + 50) - fontRenderer.getStringWidth(s) / 2, hhSize + 2, ColorInt_Alert);
        s = "NUMBER";
        drawString(fontRenderer, s, (hwSize + 155) - fontRenderer.getStringWidth(s) / 2, hhSize + 2, ColorInt_Number);


        GL11.glEnable(3042 /*GL_BLEND*/);

        s = "WARNING";
        fontRenderer.drawString(s, hwSize - fontRenderer.getStringWidth(s) / 2, hhSize - 50, ColorInt_Warning);
        s = "ALERT";
        fontRenderer.drawString(s, hwSize - fontRenderer.getStringWidth(s) / 2, hhSize - 70, ColorInt_Alert);
        s = "NUMBER";
        fontRenderer.drawString(s, hwSize - fontRenderer.getStringWidth(s) / 2, hhSize - 90, ColorInt_Number);


        GL11.glDisable(3042 /*GL_BLEND*/);
    }

    @Override
    public void renderRSHUD(Minecraft minecraft, int i, int j) {
        double d = (double)i / 2D;
        double d1 = (double)j / 2D;
        EntityPlayerSP entityplayersp = minecraft.thePlayer;

        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(3042 /*GL_BLEND*/);
        GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
        GL11.glBlendFunc(770, 771);

        GL11.glLineWidth(mod_RSHUD_AC.LineWidth);

//Line &Box Drawing

        GL11.glColor4f((LineR * 0.1F), (LineG * 0.1F), (LineB * 0.1F),0.3F);
//speed Numbers Frame Shadow
        tessellator.startDrawing(6);
        double d2 = 15D;
        double d3 = j - 52D;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2, d3 + 29D, 0.0D);//1
        tessellator.addVertex(d2 + 50D, d3 + 29D, 0.0D);//2
        tessellator.addVertex(d2 + 50D, d3 + 15D, 0.0D);//3
        tessellator.addVertex(d2 + 35D, d3, 0.0D);//4

        tessellator.draw();

//Height Numbers Frame Shadow
        tessellator.startDrawing(6);
        d2 = i - 55;
        d3 = 130;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2, d3 + 12D, 0.0D);
        tessellator.addVertex(d2 + 13D, d3 +25D, 0.0D);
        tessellator.addVertex(d2 + 45D, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 45D, d3, 0.0D);

        tessellator.draw();

//Armor frame Shadow
        tessellator.startDrawing(6);
        d2 = 15;
        d3 = 15;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 70D, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 70D, d3 + 20D, 0.0D);
        tessellator.addVertex(d2 + 50D, d3, 0.0D);
        tessellator.draw();
//boost frame Shadow
        tessellator.startDrawing(6);
        d2 = 3;
        d3 = 15;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2 + 10D, d3, 0.0D);
        tessellator.addVertex(d2 + 10D, d3 + (double)j -38D, 0.0D);
        tessellator.addVertex(d2, d3 + (double)j -38D, 0.0D);
        tessellator.draw();


//Orientation(hougaku)
        GL11.glColor4f(LineR, LineG, LineB, mod_RSHUD_AC.LineAlpha);

        d2 = 0.0D - (double)((EntityPlayer) (entityplayersp)).rotationYaw % 10D;
        int k = (int)(((EntityPlayer) (entityplayersp)).rotationYaw / 10F) * 2;
        tessellator.startDrawing(1);

        for(int l = -i / 20; l < i / 20; l++)
        {

        	double d4 = d2 - d;
            d4 = 12D;
            if((k + l) % 18 == 0)
            {
                tessellator.addVertex(d/2 + d2+2, d4 + 7D, 0.0D);
            } else
            {
                tessellator.addVertex(d/2 + d2+2, d4 + ((l & 1) == 1 ? 3D : 5D), 0.0D);
            }
            tessellator.addVertex(d/2 + d2+2, d4, 0.0D);
            d2 += 5D;
            }
        tessellator.draw();

//Orientation Line
        tessellator.startDrawing(1);
        tessellator.addVertex(d - (i/4), 12, 0.0D);
        tessellator.addVertex(d + (i/4), 12, 0.0D);
        tessellator.draw();

//Center marker
        d3 = 15D;//d1 - 90D;
        tessellator.startDrawing(2);
        tessellator.addVertex(d, d3, 0.0D);
        tessellator.addVertex(d + 4D, d3 + 6D, 0.0D);
        tessellator.addVertex(d - 4D, d3 + 6D, 0.0D);
        tessellator.draw();

//Pitch
        d2 = d;
        d3 = d1 - 105D - ((double)((EntityPlayer) (entityplayersp)).rotationPitch % 10D) * 3D;
        int i1 = (int)(((EntityPlayer) (entityplayersp)).rotationPitch / 10F) * 2;
        tessellator.startDrawing(1);
        for(int j1 = -7; j1 < 7; j1++)
        {
            if(d3 > d1 - 60D && d3 < d1 + 61D)
            {
                int k1 = i1 + j1;
                if(k1 % 9 == 0)
                {
                    tessellator.addVertex(d2 - 90D, d3, 0.0D);
                    tessellator.addVertex(d2 - 55D, d3, 0.0D);
                    tessellator.addVertex(d2 + 90D, d3, 0.0D);
                    tessellator.addVertex(d2 + 55D, d3, 0.0D);
                } else
                {
                    byte byte0 = (byte)((j1 & 1) == 0 ? 8 : 6);
                    for(int l1 = byte0 == 8 ? 0 : 1; l1 < byte0; l1++)
                    {
                        double d7 = l1 * 2;
                        tessellator.addVertex((d2 - 90D) + d7, d3, 0.0D);
                        tessellator.addVertex((d2 - 89D) + d7, d3, 0.0D);
                        tessellator.addVertex((d2 + 90D) - d7, d3, 0.0D);
                        tessellator.addVertex((d2 + 89D) - d7, d3, 0.0D);
                    }

                    if(byte0 == 8)
                    {
                        if(k1 < 0 && k1 > -18 || k1 > 18)
                        {
                            tessellator.addVertex(d2 - 90D, d3, 0.0D);
                            tessellator.addVertex(d2 - 90D, d3 + 3D, 0.0D);
                            tessellator.addVertex(d2 + 90D, d3, 0.0D);
                            tessellator.addVertex(d2 + 90D, d3 + 3D, 0.0D);
                        } else
                        {
                            tessellator.addVertex(d2 - 90D, d3, 0.0D);
                            tessellator.addVertex(d2 - 90D, d3 - 3D, 0.0D);
                            tessellator.addVertex(d2 + 90D, d3, 0.0D);
                            tessellator.addVertex(d2 + 90D, d3 - 3D, 0.0D);
                        }
                    }
                }
            }
            d3 += 15D;
        }

        tessellator.addVertex(d2 - 40D, d1, 0.0D);
        tessellator.addVertex(d2 - 15D, d1, 0.0D);
        tessellator.addVertex(d2 + 40D, d1, 0.0D);
        tessellator.addVertex(d2 + 15D, d1, 0.0D);
        tessellator.draw();
//Speed
        double d5 = ((EntityPlayer) (entityplayersp)).lastTickPosX - ((EntityPlayer) (entityplayersp)).posX;
        double d6 = ((EntityPlayer) (entityplayersp)).lastTickPosY - ((EntityPlayer) (entityplayersp)).posY;
        double d8 = ((EntityPlayer) (entityplayersp)).lastTickPosZ - ((EntityPlayer) (entityplayersp)).posZ;
        double d9 = Math.sqrt(d5 * d5 + d6 * d6 + d8 * d8) * 100D;

//Height
        tessellator.startDrawing(1);
        tessellator.addVertex(i - 3, 92, 0.0D);
        tessellator.addVertex(i - 3, j - 21, 0.0D);
        tessellator.draw();

        tessellator.startDrawing(1);
        d3 = 0.0D + (minecraft.thePlayer.posY * 10D) % 10D;
        int heit = j - 90 - 23;
        for(int j2 = 0; j2 < 129; j2++)
        {
            d3 = (j-21) - (j2 * heit / 128 );

                    tessellator.addVertex(i-3, d3, 0.0D);
                    if(j2 % 32 == 0)
                    {tessellator.addVertex(i-10, d3, 0.0D);}else
                    {tessellator.addVertex(i-3 - ((j2 % 8) == 0 ? 3D : 0D), d3, 0.0D);}
        }
        tessellator.draw();

//Height marker
        double h0 = (j-21) - ((minecraft.thePlayer.posY) * heit / 128 );
        tessellator.startDrawing(4);
        tessellator.addVertex(i-4, h0, 0.0D);
        tessellator.addVertex(i-10, h0 - 3D, 0.0D);
        tessellator.addVertex(i-10, h0 + 3D, 0.0D);
        tessellator.draw();

//speed Numbers Frame
        tessellator.startDrawing(2);
        d2 = 15D;
        d3 = j - 52D;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2 + 35D, d3, 0.0D);
        tessellator.addVertex(d2 + 50D, d3 + 15D, 0.0D);
        tessellator.addVertex(d2 + 50D, d3 + 29D, 0.0D);
        tessellator.addVertex(d2, d3 + 29D, 0.0D);
        tessellator.draw();

//Height Numbers Frame
        tessellator.startDrawing(2);
        d2 = i - 55;
        d3 = 130;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2 + 45D, d3, 0.0D);
        tessellator.addVertex(d2 + 45D, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 13D, d3 +25D, 0.0D);
        tessellator.addVertex(d2, d3 + 12D, 0.0D);
        tessellator.draw();

//Armor frame
        tessellator.startDrawing(2);
        d2 = 15;
        d3 = 15;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 70D, d3 + 25D, 0.0D);
        tessellator.addVertex(d2 + 70D, d3 + 20D, 0.0D);
        tessellator.addVertex(d2 + 50D, d3, 0.0D);
        tessellator.draw();
//boost frame
        tessellator.startDrawing(2);
        d2 = 3D;
        d3 = 15D;
        tessellator.addVertex(d2, d3, 0.0D);
        tessellator.addVertex(d2 + 10D, d3, 0.0D);
        tessellator.addVertex(d2 + 10D, d3 + (double)j -38D, 0.0D);
        tessellator.addVertex(d2, d3 + (double)j -38D, 0.0D);
        tessellator.draw();

//Cleaning up
        GL11.glEnable(3553 /*GL_TEXTURE_2D*/);

//Character Drawing
//Orientation(hougaku)
        int k2 = ColorInt_Number;
        int l2 = (int)(((EntityPlayer) (entityplayersp)).rotationYaw / 10F) * 2;
        d2 = 0.0D - (double)((EntityPlayer) (entityplayersp)).rotationYaw % 10D;

        for(int i3 = -i / 20; i3 < i / 20; i3++)
        {
            d3 = 2D;
            if((k + i3) % 18 == 0)
            {
                int k3 = ((l2 + i3) / 2 + mod_RSHUD_AC.DegOffset) % 36;
                if(k3 < 0)
                {
                    k3 += 36;
                }
                //Orientation logo
                if(k3 == 0)
                {
                    String s2 = "N";
                    minecraft.fontRenderer.drawString(s2, (int)(d / 2 + d2), (int)d3, k2);
                }
                if(k3 == 9)
                {
                    String s3 = "E";
                    minecraft.fontRenderer.drawString(s3, (int)(d / 2 + d2), (int)d3, k2);
                }
                if(k3 == 18)
                {
                    String s4 = "S";
                    minecraft.fontRenderer.drawString(s4, (int)(d / 2 + d2), (int)d3, k2);
                }
                if(k3 == 27)
                {
                    String s5 = "W";
                    minecraft.fontRenderer.drawString(s5, (int)(d / 2 + d2), (int)d3, k2);
                }
            }
            d2 += 5D;
        }
//Speed
        d3 = 0.0D + (d9 * 10D) % 10D;
        String s = String.format("%.1f", new Object[] {
            Double.valueOf(d9)
        });

        minecraft.fontRenderer.drawString(s, 17, j - 35, k2);
//Logo Speed
        String s1 = "Speed";
        minecraft.fontRenderer.drawString(s1, 17 ,j - 49, k2);
//Height

        s = String.format("%.1f", new Object[] {
            Double.valueOf(minecraft.thePlayer.posY)
        });
        minecraft.fontRenderer.drawString(s, ((int)i - minecraft.fontRenderer.getStringWidth(s))- 15, 145, k2);
//Logo Height
        String s7 = "Height";
        minecraft.fontRenderer.drawString(s7, ((int)i - minecraft.fontRenderer.getStringWidth(s7) / 2) -30, 132, k2);
//Pitch
        d2 = d;
        d3 = (double)j / 2D - 105D - ((double)((EntityPlayer) (entityplayersp)).rotationPitch % 10D) * 3D - 3D;
        for(int k4 = -7; k4 < 7; k4++)
        {
            if(d3 > d1 - 63D && d3 < d1 + 60D && (k4 & 1) == 0)
            {
                int i5 = Math.abs(i1 + k4) / 2;
                if(i5 > 9)
                {
                    i5 = 18 - i5;
                }
                minecraft.fontRenderer.drawString(String.format("%d0", new Object[] {
                    Integer.valueOf(i5)
                }), (int)d2 - 104, (int)d3, k2);
                minecraft.fontRenderer.drawString(String.format("%d0", new Object[] {
                    Integer.valueOf(i5)
                }), (int)d2 + 94, (int)d3, k2);
            }
            d3 += 15D;
        }
//Ore Range
        int l4 = ColorInt_Normal / 0x1000000;
        int j5 = l4 * 0x1000000 + ColorInt_Iron;
        int k5 = l4 * 0x1000000 + ColorInt_Gold;
        int l5 = l4 * 0x1000000 + ColorInt_Diamond;
        int i6 = l4 * 0x1000000 + ColorInt_Lava;
        if(minecraft.thePlayer.posY < 64D)
        {
            String s8 = "Iron";
            minecraft.fontRenderer.drawString(s8, i - minecraft.fontRenderer.getStringWidth(s8) - 10, 90, j5);
        }
        if(minecraft.thePlayer.posY < 30D)
        {
            String s9 = "Gold";
            minecraft.fontRenderer.drawString(s9, i - minecraft.fontRenderer.getStringWidth(s9) - 10, 100, k5);
        }
        if(minecraft.thePlayer.posY < 16D)
        {
            String s10 = "Diamond";
            minecraft.fontRenderer.drawString(s10, i - minecraft.fontRenderer.getStringWidth(s10) - 10, 110, l5);
        }
        if(minecraft.thePlayer.posY < 12D)
        {
            String s11 = "Lava";
            minecraft.fontRenderer.drawString(s11, i - minecraft.fontRenderer.getStringWidth(s11) - 10, 120, i6);
        }

//time
        int j6 = (int)(minecraft.theWorld.getWorldTime() % 24000L) / 10;
        int k6 = j6 / 100 + 6;
        if(k6 >= 24)
        {
            k6 -= 24;
        }
        String s12 = String.format("%02d", new Object[] {
            Integer.valueOf(k6)
        });
        minecraft.fontRenderer.drawString(s12, 5, j - 15, k2);
        int l6 = (int)((double)(j6 % 100) / 1.66666665D);
        String s13 = String.format("%02d", new Object[] {
            Integer.valueOf(l6)
        });
        minecraft.fontRenderer.drawString(s13, 21, j - 15, k2);
        String s14 = ":";
        minecraft.fontRenderer.drawString(s14, 18, j - 15, k2);

//Status display
//Potion
        int PC = 0;
        if (minecraft.renderViewEntity.isPotionActive(Potion.fireResistance))
        {String po0 = "ANTI-Flame";
         PC++;
         minecraft.fontRenderer.drawString(po0, 20, 34+(PC * 11), k2);}
        if (minecraft.renderViewEntity.isPotionActive(Potion.moveSpeed))
        {String po0 = "SPD-Boost";
         PC++;
         minecraft.fontRenderer.drawString(po0, 20, 34+(PC * 11), k2);}
        if (minecraft.renderViewEntity.isPotionActive(Potion.damageBoost))
        {String po0 = "ATK-Boost";
         PC++;
         minecraft.fontRenderer.drawString(po0, 20, 34+(PC * 11), k2);}
        if (minecraft.renderViewEntity.isPotionActive(Potion.regeneration))
        {String po0 = "AUTO-Repair";
         PC++;
         minecraft.fontRenderer.drawString(po0, 20, 34+(PC * 11), k2);}
        if (minecraft.renderViewEntity.isPotionActive(Potion.hunger))
        {String po0 = "FUIL-Leakage";
         PC++;
         minecraft.fontRenderer.drawString(po0, 20, 34+(PC * 11), ColorInt_Warning);}


        if(j6 % 3 != 1){
        if (minecraft.renderViewEntity.isPotionActive(Potion.poison))
        	{String po0 = "ALEART POISON!";
        	minecraft.fontRenderer.drawString(po0, (int)d - minecraft.fontRenderer.getStringWidth(po0) / 2, (int)d1 - 60, ColorInt_Alert);}
//Health
        if(((EntityPlayer) (entityplayersp)).health < 9)
        {
            String s15 = "ALEART GCS DOWN!";
            int i7 = ((EntityPlayer) (entityplayersp)).health < 5 ? ColorInt_Alert : ColorInt_Warning;
            minecraft.fontRenderer.drawString(s15, (int)d - minecraft.fontRenderer.getStringWidth(s15) / 2, (int)d1 - 80, i7);
        }
//Food
        if(((EntityPlayer) (entityplayersp)).foodStats.getFoodLevel() < 7)
        	{
            String s16 = "ALEART LESS FUEL";
            int j7 = ((EntityPlayer) (entityplayersp)).health < 5 ? ColorInt_Alert : ColorInt_Warning;
            minecraft.fontRenderer.drawString(s16, (int)d - minecraft.fontRenderer.getStringWidth(s16) / 2, (int)d1 - 70, j7);
        	}
        }

//Use Item
        ItemStack itemstack = ((EntityPlayer) (entityplayersp)).inventory.getCurrentItem();
        if(itemstack != null && itemstack.isItemStackDamageable())
        {
            int k7 = itemstack.getMaxDamage() - itemstack.getItemDamage();
            int j8 = ColorInt_Number;
            if((float)itemstack.getItemDamage() / (float)itemstack.getMaxDamage() > 0.9F)
            {
                j8 = ColorInt_Alert;
            }
            String s17 = (new StringBuilder()).append(itemstack.getItem().getStatName()).append(" /").toString();
            minecraft.fontRenderer.drawString(s17, ((int)d - minecraft.fontRenderer.getStringWidth(s17)) + 25, j - 58, j8);
            s17 = String.format("%7d", new Object[] {
                Integer.valueOf(k7)
            });
            minecraft.fontRenderer.drawString(s17, ((int)d - minecraft.fontRenderer.getStringWidth(s17)) + 60, j - 58, j8);
        }
//Use Mod Item
        if(itemstack != null && projectorList.containsKey(Integer.valueOf(itemstack.getItem().shiftedIndex)))
        {
            int l7 = 0;
            List list = (List)projectorList.get(Integer.valueOf(itemstack.getItem().shiftedIndex));
            for(int l8 = 0; l8 < ((EntityPlayer) (entityplayersp)).inventory.mainInventory.length; l8++)
            {
                itemstack = ((EntityPlayer) (entityplayersp)).inventory.mainInventory[l8];
                if(itemstack != null && list.contains(Integer.valueOf(itemstack.getItem().shiftedIndex)))
                {
                    l7 += itemstack.stackSize;
                }
            }

            int i9 = ColorInt_Number;
            if(l7 <= 10)
            {
                i9 = ColorInt_Alert;
            }
//Ammo
            String s18 = (new StringBuilder()).append(Item.itemsList[((Integer)list.get(0)).intValue()].getStatName()).append(" /").toString();
            minecraft.fontRenderer.drawString(s18, ((int)d - minecraft.fontRenderer.getStringWidth(s18)) + 25, j - 68, i9);
            if(l7 == 0)
            {
                s18 = "EMPTY";
            } else
            {
                s18 = String.format("%7d", new Object[] {
                    Integer.valueOf(l7)
                });
            }
            minecraft.fontRenderer.drawString(s18, ((int)d - minecraft.fontRenderer.getStringWidth(s18)) + 60, j - 68, i9);
        }
//Armor
        int i8 = 17;
        int k8 = 17;
        int AP = 0;
        itemstack = ((EntityPlayer) (entityplayersp)).inventory.armorInventory[3];
        if(itemstack != null)
        {
        	AP += (itemstack.getMaxDamage()) - (itemstack.getItemDamage());
            int j9 = getArmorColor(itemstack);
            drawRectL(i8, k8, i8 + 11, k8 + 5, j9);
            drawRectL(i8, k8 + 5, i8 + 3, k8 + 11, j9);
            drawRectL(i8 + 8, k8 + 5, i8 + 11, k8 + 11, j9);

        }
        i8 += 13;
        itemstack = ((EntityPlayer) (entityplayersp)).inventory.armorInventory[2];
        if(((EntityPlayer) (entityplayersp)).inventory.armorInventory[2] != null)
        {
        	AP += (itemstack.getMaxDamage()) - (itemstack.getItemDamage());
            int k9 = getArmorColor(itemstack);
            drawRectL(i8, k8, i8 + 11, k8 + 5, k9);
            drawRectL(i8 + 2, k8 + 5, i8 + 9, k8 + 11, k9);
        }
        i8 += 13;
        itemstack = ((EntityPlayer) (entityplayersp)).inventory.armorInventory[1];
        if(((EntityPlayer) (entityplayersp)).inventory.armorInventory[1] != null)
        {
        	AP += (itemstack.getMaxDamage()) - (itemstack.getItemDamage());
            int l9 = getArmorColor(itemstack);
            drawRectL(i8, k8, i8 + 11, k8 + 4, l9);
            drawRectL(i8, k8 + 4, i8 + 5, k8 + 11, l9);
            drawRectL(i8 + 6, k8 + 4, i8 + 11, k8 + 11, l9);

        }
        i8 += 13;
        k8 += 4;
        itemstack = ((EntityPlayer) (entityplayersp)).inventory.armorInventory[0];
        if(((EntityPlayer) (entityplayersp)).inventory.armorInventory[0] != null)
        {
        	AP += (itemstack.getMaxDamage()) - (itemstack.getItemDamage());
            int i10 = getArmorColor(itemstack);
            drawRectL(i8 + 1, k8, i8 + 5, k8 + 7, i10);
            drawRectL(i8, k8 + 4, i8 + 1, k8 + 7, i10);

            drawRectL(i8 + 6, k8, i8 + 10, k8 + 7, i10);
            drawRectL(i8 + 10, k8 + 4, i8 + 11, k8 + 7, i10);

        }
//boost gage
        int cl1 = ColorInt_Number;
        int cl2 = ColorInt_Alert;
        double bst1 = j * 0.9D;
        double bst2 = j * 0.8D;

        double bst0 =((((EntityPlayer) (entityplayersp)).foodStats.getFoodLevel() * 10)- d9) * (j / 240D);

        if(bst0 >= 0D)
        {
        if(bst0 >= 20D)
            {
            	drawRectL(10, (int)bst1 -(int)bst0, 5, (int)bst2, cl1);
            	drawRectL(10, (int)bst2, 5, (int)bst1, cl2);
            }else
            {
            	drawRectL(10, (int)bst1 -(int)bst0, 5, (int)bst1, cl2);
            }
        }
//Ap
        if(AP != 0)
        {
        d2 = d - 146D;
        d3 = (double)j - 80D;
        String ap0 = "AP ";
        minecraft.fontRenderer.drawString(ap0, 20, 30, k2);
        minecraft.fontRenderer.drawString(String.format("%d0", new Object[] {
                Integer.valueOf(AP) }), 40, 30, k2);
        }

//Cleaning up
        GL11.glDisable(3042 /*GL_BLEND*/);
    }

}
