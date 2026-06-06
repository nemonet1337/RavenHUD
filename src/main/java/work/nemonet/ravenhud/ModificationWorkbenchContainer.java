package work.nemonet.ravenhud;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.Tags;

public class ModificationWorkbenchContainer extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Container inputSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ModificationWorkbenchContainer.this.slotsChanged(this);
        }
    };
    private final ResultContainer outputSlot = new ResultContainer();

    public ModificationWorkbenchContainer(int id, Inventory playerInventory) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModificationWorkbenchContainer(int id, Inventory playerInventory, ContainerLevelAccess access) {
        super(Registration.MODIFICATION_WORKBENCH_CONTAINER.get(), id);
        this.access = access;

        // スロット 0: ヘルメット
        this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
        // スロット 1: インゴット
        this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
        // スロット 2: 出力
        this.addSlot(new Slot(this.outputSlot, 0, 134, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                ModificationWorkbenchContainer.this.inputSlots.setItem(0, ItemStack.EMPTY);
                ItemStack itemstack = ModificationWorkbenchContainer.this.inputSlots.getItem(1);
                if (!itemstack.isEmpty()) {
                    itemstack.shrink(1);
                    ModificationWorkbenchContainer.this.inputSlots.setItem(1, itemstack);
                } else {
                    ModificationWorkbenchContainer.this.inputSlots.setItem(1, ItemStack.EMPTY);
                }

                access.execute((level, pos) -> level.levelEvent(1030, pos, 0));
                super.onTake(player, stack);
            }
        });

        // プレイヤーインベントリ
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }
        // ホットバー
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.inputSlots) {
            this.updateOutput();
        }
    }

    public void updateOutput() {
        ItemStack mainSlot = this.inputSlots.getItem(0);
        ItemStack subSlot = this.inputSlots.getItem(1);
        if (mainSlot.isEmpty() || subSlot.isEmpty()
                || mainSlot.getItem().getEquipmentSlot(mainSlot) != EquipmentSlot.HEAD
                || !subSlot.is(Tags.Items.INGOTS)) {
            this.outputSlot.setItem(0, ItemStack.EMPTY);
            return;
        }
        ItemStack output = mainSlot.copy();
        CustomData customData = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.getBoolean("ravenhudCanRender").orElse(false)) {
            this.outputSlot.setItem(0, ItemStack.EMPTY);
            return;
        }
        tag.putBoolean("ravenhudCanRender", true);
        output.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        this.outputSlot.setItem(0, output);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Registration.MODIFICATION_WORKBENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 2) {
                // 出力スロットからインベントリへ
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                // インベントリから入力スロットへ
                if (itemstack1.getItem().getEquipmentSlot(itemstack1) == EquipmentSlot.HEAD) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(Tags.Items.INGOTS)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                // 入力スロットからインベントリへ
                if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
