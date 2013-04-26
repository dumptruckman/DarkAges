package com.dumptruckman.minecraft.darkages.menu;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ArenaMenu {

    private final DarkAgesPlugin plugin;
    private final String arenaName;
    private final Arena arena;

    public ArenaMenu(final DarkAgesPlugin plugin, final String arenaName) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.arena = plugin.getArena(arenaName);
        if (arena == null) {
            throw new IllegalArgumentException("Arena by that name doesn't exist!");
        }
    }

    public SingleViewMenu buildMenu() {
        // Create the main menu
        final SingleViewMenu menu = Menus.createSimpleInventoryMenu(plugin, "Arena: " + arena.getName(), 9);

        for (String locName : arena.getLocations()) {
            final Location location = arena.getLocation(locName);
            if (location == null) {
                continue;
            }
            ItemStack item = new ItemStack(Material.PORTAL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Go to " + locName);
            item.setItemMeta(meta);

            menu.addItem(new MenuItem(locName).setItemStack(item).setAction(new Action() {
                @Override
                public void performAction(@Nullable final Player player) {
                    if (player != null) {
                        player.closeInventory();
                        player.teleport(location);
                    }
                }
            }));
        }

        final Location loc = arena.getRespawnLocation();
        if (loc != null) {
            ItemStack item = new ItemStack(Material.PORTAL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Leave arena");
            item.setItemMeta(meta);

            menu.addItem(new MenuItem("Leave arena").setItemStack(item).setAction(new Action() {
                @Override
                public void performAction(@Nullable final Player player) {
                    if (player != null) {
                        player.closeInventory();
                        player.teleport(loc);
                    }
                }
            }));
        }
        return menu;
    }
}
