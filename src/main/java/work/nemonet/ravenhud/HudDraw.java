package work.nemonet.ravenhud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.joml.Matrix3x2f;

public final class HudDraw {
    private HudDraw() {}

    static void addValLine(VertexConsumer consumer, Matrix3x2f pose,
                           float x1, float y1, float x2, float y2, float width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;
        float nx = -dy / len * (width / 2.0f);
        float ny =  dx / len * (width / 2.0f);
        consumer.addVertexWith2DPose(pose, x1 + nx, y1 + ny).setColor(color);
        consumer.addVertexWith2DPose(pose, x2 + nx, y2 + ny).setColor(color);
        consumer.addVertexWith2DPose(pose, x2 - nx, y2 - ny).setColor(color);
        consumer.addVertexWith2DPose(pose, x1 - nx, y1 - ny).setColor(color);
    }

    private static GuiElementRenderState makeState(GuiGraphicsExtractor g,
                                                    java.util.function.Consumer<VertexConsumer> builder) {
        return new GuiElementRenderState() {
            @Override public void buildVertices(VertexConsumer c) { builder.accept(c); }
            @Override public RenderPipeline pipeline() { return RenderPipelines.GUI; }
            @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
            @Override public ScreenRectangle scissorArea() { return null; }
            @Override public ScreenRectangle bounds() {
                return new ScreenRectangle(0, 0, g.guiWidth(), g.guiHeight());
            }
        };
    }

    public static void drawPolygon(GuiGraphicsExtractor g, int color, double... vertices) {
        if (vertices.length < 6 || vertices.length % 2 != 0) return;
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer -> {
            int n = vertices.length / 2;
            for (int i = 0; i < n - 2; i++) {
                float x0 = (float) vertices[0],      y0 = (float) vertices[1];
                float x1 = (float) vertices[(i+1)*2], y1 = (float) vertices[(i+1)*2+1];
                float x2 = (float) vertices[(i+2)*2], y2 = (float) vertices[(i+2)*2+1];
                consumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
                consumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
                consumer.addVertexWith2DPose(pose, x2, y2).setColor(color);
                consumer.addVertexWith2DPose(pose, x2, y2).setColor(color);
            }
        }));
    }

    public static void drawLineLoop(GuiGraphicsExtractor g, int color, double... vertices) {
        if (vertices.length < 4 || vertices.length % 2 != 0) return;
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer -> {
            int n = vertices.length / 2;
            for (int i = 0; i < n; i++) {
                float x1 = (float) vertices[i*2],           y1 = (float) vertices[i*2+1];
                float x2 = (float) vertices[((i+1)%n)*2],   y2 = (float) vertices[((i+1)%n)*2+1];
                addValLine(consumer, pose, x1, y1, x2, y2, 1.0f, color);
            }
        }));
    }

    public static void drawLine(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, int color) {
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer ->
            addValLine(consumer, pose, x1, y1, x2, y2, 1.0f, color)
        ));
    }

    public static void drawLine(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float width, int color) {
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer ->
            addValLine(consumer, pose, x1, y1, x2, y2, width, color)
        ));
    }

    public static void drawArch(GuiGraphicsExtractor g, int color,
                                 double cx, double cy, double radius, double thick,
                                 double startAngle, double endAngle) {
        if (Math.abs(endAngle - startAngle) < 0.001) return;
        double rad = Math.PI / 180.0;
        double s = startAngle * rad, e = endAngle * rad;
        if (e < s) { double t = s; s = e; e = t; }
        final double fs = s, fe = e;
        double outer = radius + thick, inner = radius - thick;
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer -> {
            double step = 5.0 * rad, cur = fs;
            while (cur < fe) {
                double nxt = Math.min(cur + step, fe);
                float x1o = (float)(cx + Math.sin(cur) * outer), y1o = (float)(cy - Math.cos(cur) * outer);
                float x1i = (float)(cx + Math.sin(cur) * inner), y1i = (float)(cy - Math.cos(cur) * inner);
                float x2o = (float)(cx + Math.sin(nxt) * outer), y2o = (float)(cy - Math.cos(nxt) * outer);
                float x2i = (float)(cx + Math.sin(nxt) * inner), y2i = (float)(cy - Math.cos(nxt) * inner);
                consumer.addVertexWith2DPose(pose, x1o, y1o).setColor(color);
                consumer.addVertexWith2DPose(pose, x2o, y2o).setColor(color);
                consumer.addVertexWith2DPose(pose, x2i, y2i).setColor(color);
                consumer.addVertexWith2DPose(pose, x1i, y1i).setColor(color);
                cur = nxt;
            }
        }));
    }

    public static void drawArchLine(GuiGraphicsExtractor g, int color,
                                     double cx, double cy, double radius,
                                     double startAngle, double endAngle) {
        if (Math.abs(endAngle - startAngle) < 0.001) return;
        double rad = Math.PI / 180.0;
        double s = startAngle * rad, e = endAngle * rad;
        if (e < s) { double t = s; s = e; e = t; }
        final double fs = s, fe = e;
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        g.submitGuiElementRenderState(makeState(g, consumer -> {
            double step = 5.0 * rad, cur = fs;
            float lastX = (float)(cx + Math.sin(cur) * radius);
            float lastY = (float)(cy - Math.cos(cur) * radius);
            while (cur < fe) {
                double nxt = Math.min(cur + step, fe);
                float nx2 = (float)(cx + Math.sin(nxt) * radius);
                float ny2 = (float)(cy - Math.cos(nxt) * radius);
                addValLine(consumer, pose, lastX, lastY, nx2, ny2, 1.0f, color);
                lastX = nx2; lastY = ny2; cur = nxt;
            }
        }));
    }
}
