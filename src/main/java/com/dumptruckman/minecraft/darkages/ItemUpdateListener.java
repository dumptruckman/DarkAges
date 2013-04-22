package com.dumptruckman.minecraft.darkages;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemUpdateListener implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;

    public ItemUpdateListener(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && currentItem.hasItemMeta()) {
            ItemMeta meta = currentItem.getItemMeta();
            if (!meta.getLore().isEmpty()) {
                for (Map.Entry<ItemStack, Ability> ability : Ability.ABILITY_ITEMS.entrySet()) {
                    if (meta.getLore().get(0).equals(ability.getValue().getAbilityTag())) {
                        int amount = currentItem.getAmount();
                        currentItem = new ItemStack(ability.getKey());
                        currentItem.setAmount(amount);
                        event.setCurrentItem(currentItem);
                        break;
                    }
                }
            }
        }
        currentItem = event.getCursor();
        if (currentItem != null && currentItem.hasItemMeta()) {
            for (Map.Entry<ItemStack, Ability> ability : Ability.ABILITY_ITEMS.entrySet()) {
                if (!ability.getValue().isAllowedToDrop() && currentItem.isSimilar(ability.getKey())) {
                    if (event.getSlotType() == SlotType.OUTSIDE) {
                        event.setCursor(null);
                    } else if (event.getSlotType() != SlotType.OUTSIDE) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        ItemStack currentItem = event.getPlayer().getItemInHand();
        if (currentItem != null && currentItem.hasItemMeta()) {
            ItemMeta meta = currentItem.getItemMeta();
            if (!meta.getLore().isEmpty()) {
                for (Map.Entry<ItemStack, Ability> ability : Ability.ABILITY_ITEMS.entrySet()) {
                    if (meta.getLore().get(0).equals(ability.getValue().getAbilityTag())) {
                        int amount = currentItem.getAmount();
                        currentItem = new ItemStack(ability.getKey());
                        currentItem.setAmount(amount);
                        event.getPlayer().setItemInHand(currentItem);
                        break;
                    }
                }
            }
        }
    }
}
