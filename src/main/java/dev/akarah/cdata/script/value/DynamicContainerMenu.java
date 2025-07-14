package dev.akarah.cdata.script.value;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class DynamicContainerMenu extends ChestMenu {
    public DynamicContainerMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int j) {
        super(menuType, i, inventory, container, j);
    }
}
