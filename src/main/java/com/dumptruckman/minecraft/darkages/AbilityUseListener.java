package com.dumptruckman.minecraft.darkages;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AbilityUseListener implements Listener {

    @NotNull
    private final DarkAgesPlugin plugin;

    public AbilityUseListener(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.isCancelled()) {
            return;
        }
        if (event.getItem() != null) {
            ItemStack item = event.getItem();
            if (item.getAmount() != 1) {
                item = new ItemStack(item);
                item.setAmount(1);
            }
            Ability ability = Ability.ABILITY_ITEMS.get(item);
            if (ability != null) {
                ability.useAbility(event);
            } else {
                ability = Ability.LEARNING_ITEMS.get(item);
                if (ability != null) {
                    ability.learnAbility(event.getPlayer());
                }
            }
        }
    }
}
