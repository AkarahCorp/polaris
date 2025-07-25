package dev.akarah.cdata.script.value.mc.rt;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.value.RText;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.mc.RItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;

public class DynamicContainerMenu extends ChestMenu {
    public RText name;

    public DynamicContainerMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int j, RText name) {
        super(menuType, i, inventory, container, j);
        this.name = name;
    }

    @Override
    public void clicked(int slot, int j, ClickType clickType, Player player) {
        if(this.getContainer() instanceof DynamicContainer dynamicContainer) {
            var item = dynamicContainer.getItem(slot);
            var p = (ServerPlayer) player;

            var result = Resources.actionManager().performEvents(
                    "item.menu_click",
                    REntity.of(p),
                    RItem.of(item)
            );
            if(dynamicContainer.cancelClicks() || !result) {
                this.sendAllDataToRemote();
                return;
            }
        }
        super.clicked(slot, j, clickType, player);
    }
}
