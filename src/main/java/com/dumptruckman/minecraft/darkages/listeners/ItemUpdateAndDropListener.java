package com.dumptruckman.minecraft.darkages.listeners;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
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
import java.util.Set;

public class ItemUpdateAndDropListener implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;

    public ItemUpdateAndDropListener(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (event.getWhoClicked() instanceof Player) {
            if (currentItem != null && currentItem.hasItemMeta()) {
                ItemMeta meta = currentItem.getItemMeta();
                if (meta != null && meta.getLore() != null && meta.getLore().isEmpty()) {
                    final String tag = meta.getLore().get(0);
                    Set<Map.Entry<ItemStack, Ability>> abilitySet;
                    if (currentItem.getType() == Material.PAPER) {
                        abilitySet = Ability.LEARNING_ITEMS.entrySet();
                    } else {
                        abilitySet = Ability.ABILITY_ITEMS.entrySet();
                    }
                    for (Map.Entry<ItemStack, Ability> ability : abilitySet) {
                        if (tag.equals(ability.getValue().getTag()) && !currentItem.isSimilar(ability.getKey())) {
                            int amount = currentItem.getAmount();
                            currentItem = new ItemStack(ability.getKey());
                            currentItem.setAmount(amount);
                            event.setCurrentItem(currentItem);
                            final Player player = (Player) event.getWhoClicked();
                            player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "An item has been updated to reflect changes in it's properties.");
                            event.setCancelled(true);
                            event.setResult(Result.DENY);
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    player.updateInventory();
                                }
                            });
                            return;
                        }
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
            if (meta != null && meta.getLore() != null && !meta.getLore().isEmpty()) {
                final String tag = meta.getLore().get(0);
                Set<Map.Entry<ItemStack, Ability>> abilitySet;
                if (currentItem.getType() == Material.PAPER) {
                    abilitySet = Ability.LEARNING_ITEMS.entrySet();
                } else {
                    abilitySet = Ability.ABILITY_ITEMS.entrySet();
                }
                for (Map.Entry<ItemStack, Ability> ability : abilitySet) {
                    if (tag.equals(ability.getValue().getTag()) && !currentItem.isSimilar(ability.getKey())) {
                        int amount = currentItem.getAmount();
                        currentItem = new ItemStack(ability.getKey());
                        currentItem.setAmount(amount);
                        event.getPlayer().setItemInHand(currentItem);
                        event.getPlayer().sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "This item has been updated to reflect changes in it's properties.  Please try again.");
                        break;
                    }
                }
            }
        }
    }
}
