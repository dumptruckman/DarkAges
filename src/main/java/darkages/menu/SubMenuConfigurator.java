package darkages.menu;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class SubMenuConfigurator {

    private final static ItemStack GO_BACK_ITEM_STACK = new ItemStack(Material.PORTAL);

    static {
        ItemMeta meta = GO_BACK_ITEM_STACK.getItemMeta();
        meta.setDisplayName("Go Back");
        GO_BACK_ITEM_STACK.setItemMeta(meta);
    }

    static SingleViewMenu createNewSubMenu(final Plugin plugin,
                                           final String title,
                                           final int inventorySize) {
        final SingleViewMenu menu = Menus.createSimpleInventoryMenu(plugin, "Click to see " + title, inventorySize);
        menu.setAction(new Action() {
            @Override
            public void performAction(@Nullable final org.bukkit.entity.Player player) {
                if (player != null) {
                    menu.updateView(menu, player);
                }
            }
        }).setItemStack(getAbilityContainerItem(title));
        return menu;
    }

    private static ItemStack getAbilityContainerItem(String title) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        item.setItemMeta(meta);
        return item;
    }

    static MenuItem createGoBackMenuItem(final SingleViewMenu parentMenu) {
        return new MenuItem("Click to go back to the previous menu")
                .setItemStack(new ItemStack(GO_BACK_ITEM_STACK))
                .setAction(new Action() {
                    @Override
                    public void performAction(@Nullable final org.bukkit.entity.Player player) {
                        if (player != null) {
                            parentMenu.updateView(parentMenu, player);
                        }
                    }
                });
    }
}
