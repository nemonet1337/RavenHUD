package work.nemonet.ravenhud;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(RavenHUD.MODID)
public class RavenHUD {
    public static final String MODID = "ravenhud";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RavenHUD(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);

        // Register our mod's ModConfigSpec as CLIENT config
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            modEventBus.register(RavenHUDClient.ClientModEvents.class);
        }
    }
}

