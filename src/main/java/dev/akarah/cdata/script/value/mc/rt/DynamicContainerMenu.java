package dev.akarah.cdata.script.value.mc.rt;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;

public class DynamicContainerMenu extends ChestMenu {
    public DynamicContainerMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int j) {
        super(menuType, i, inventory, container, j);
    }

    @Override
    public void clicked(int slot, int j, ClickType clickType, Player player) {
        if(this.getContainer() instanceof DynamicContainer dynamicContainer) {
            if(dynamicContainer.cancelClicks()) {
                this.sendAllDataToRemote();
                return;
            }
        }
        super.clicked(slot, j, clickType, player);
    }
}
