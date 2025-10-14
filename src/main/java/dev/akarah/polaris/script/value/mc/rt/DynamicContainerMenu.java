package dev.akarah.polaris.script.value.mc.rt;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RText;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class DynamicContainerMenu extends ChestMenu {
    public RText name;

    public DynamicContainerMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int j, RText name) {
        super(menuType, i, inventory, container, j);
        this.name = name;
    }

    @Override
    public void clicked(int slot, int j, ClickType clickType, Player player) {
        if(this.getContainer() instanceof DynamicContainer dynamicContainer) {

            ItemStack item = null;
            if(this.isValidSlotIndex(slot)) {
                item = this.getContainer().getItem(slot);
            } else {
                var newSlot = slot - this.getContainer().getContainerSize() + 9;
                if(player.inventoryMenu.isValidSlotIndex(newSlot)) {
                    item = player.inventoryMenu.getSlot(newSlot).getItem();
                } else {
                    item = player.inventoryMenu.getSlot(slot - this.getContainer().getContainerSize()).getItem();
                }
            }

            var p = (ServerPlayer) player;


            var result = Resources.actionManager().performEvents(
                    "item.menu_click",
                    REntity.of(p),
                    RItem.of(item)
            );
            var alt_result = Resources.actionManager().performEvents(
                    "item.menu_click_typed",
                    REntity.of(p),
                    RItem.of(item),
                    RString.of(clickType.name())
            );
            if(dynamicContainer.cancelClicks() || !result || !alt_result) {
                this.sendAllDataToRemote();
                return;
            }
        }
        super.clicked(slot, j, clickType, player);
    }

    @Override
    public void removed(Player player) {
        Resources.actionManager().performEvents(
                "player.close_inventory",
                REntity.of(player)
        );
        super.removed(player);
    }
}
