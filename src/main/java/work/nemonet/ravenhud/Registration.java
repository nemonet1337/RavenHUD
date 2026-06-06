package work.nemonet.ravenhud;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public class Registration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RavenHUD.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RavenHUD.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, RavenHUD.MODID);

    public static final DeferredBlock<ModificationWorkbenchBlock> MODIFICATION_WORKBENCH = BLOCKS.register(
            "modification_workbench",
            () -> new ModificationWorkbenchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RavenHUD.MODID, "modification_workbench")))
                    .strength(2.5F)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<BlockItem> MODIFICATION_WORKBENCH_ITEM = ITEMS.register(
            "modification_workbench",
            () -> new BlockItem(MODIFICATION_WORKBENCH.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavenHUD.MODID, "modification_workbench"))))
    );

    public static final DeferredItem<GoggleItem> GOGGLE = ITEMS.register(
            "goggle",
            () -> new GoggleItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavenHUD.MODID, "goggle")))
                    .stacksTo(1))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ModificationWorkbenchContainer>> MODIFICATION_WORKBENCH_CONTAINER = MENU_TYPES.register(
            "modification_workbench",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new ModificationWorkbenchContainer(windowId, inv))
    );

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
    }
}
