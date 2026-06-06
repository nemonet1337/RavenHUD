package work.nemonet.ravenhud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ModificationWorkbenchScreen extends AbstractContainerScreen<ModificationWorkbenchContainer> {

    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(RavenHUD.MODID, "textures/gui/modification_workbench_gui.png");

    public ModificationWorkbenchScreen(ModificationWorkbenchContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.titleLabelX = 28;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = this.leftPos;
        int yo = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }
}
