package work.nemonet.ravenhud;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.lwjgl.glfw.GLFW;

@Mod(value = RavenHUD.MODID, dist = Dist.CLIENT)
public class RavenHUDClient {

    public static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.ravenhud.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KeyMapping.Category.MISC
    );

    public RavenHUDClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // Registered manually from RavenHUD main class on the mod bus
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            RavenHUD.LOGGER.info("RavenHUD client setup completed.");
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(CONFIG_KEY);
        }

        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            RavenHUDOverlay overlay = new RavenHUDOverlay();
            event.registerAboveAll(
                    Identifier.fromNamespaceAndPath(RavenHUD.MODID, "hud_overlay"),
                    (guiGraphics, deltaTracker) -> overlay.render(guiGraphics, deltaTracker)
            );
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(Registration.MODIFICATION_WORKBENCH_CONTAINER.get(), ModificationWorkbenchScreen::new);
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(Registration.MODIFICATION_WORKBENCH_ITEM.get());
            }
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(Registration.GOGGLE.get());
            }
        }
    }

    @EventBusSubscriber(modid = RavenHUD.MODID, value = Dist.CLIENT)
    public static class ClientGameEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && CONFIG_KEY.consumeClick()) {
                ModList.get().getModContainerById(RavenHUD.MODID).ifPresent(container -> {
                    mc.setScreen(new ConfigurationScreen(container, mc.screen));
                });
            }
        }
    }
}

