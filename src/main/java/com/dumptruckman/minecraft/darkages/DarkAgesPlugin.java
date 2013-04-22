package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.abilities.spells.SoulStone;
import com.dumptruckman.minecraft.pluginbase.logging.LoggablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DarkAgesPlugin extends JavaPlugin implements LoggablePlugin {

    SingleViewMenu mainMenu;
    DeathHandler deathHandler;

    public ItemStack soulStoneItem;

    @Override
    public void onEnable() {
        configureMainMenu();
        new AbilityUseListener(this);
        deathHandler = new DeathHandler(this);

        // Setup souls tone recipe
        ShapelessRecipe soulStoneRecipe = new ShapelessRecipe(new ItemStack(soulStoneItem));
        soulStoneRecipe.addIngredient(1, Material.GHAST_TEAR);
        Bukkit.addRecipe(soulStoneRecipe);
    }

    @Override
    public void onDisable() {
        Ability.ABILITY_ITEMS.clear();
        Ability.LEARNING_ITEMS.clear();
    }

    private void configureMainMenu() {
        final ItemStack goBackItem = new ItemStack(Material.PORTAL);
        ItemMeta meta = goBackItem.getItemMeta();
        meta.setDisplayName("Go back");
        goBackItem.setItemMeta(meta);
        mainMenu = Menus.createSimpleInventoryMenu(this, "All Abilities", 54);
        final SingleViewMenu skillsMenu = Menus.createSimpleInventoryMenu(this, "Right click to see All Skills", 54);
        skillsMenu.setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player != null) {
                    skillsMenu.updateView(skillsMenu, player);
                }
            }
        });
        ItemStack item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName("All Skills");
        item.setItemMeta(meta);
        skillsMenu.setItemStack(item);
        skillsMenu.addItem(new MenuItem("Right click to go back to the previous menu").setItemStack(new ItemStack(goBackItem)).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player != null) {
                    mainMenu.updateView(mainMenu, player);
                }
            }
        }));
        mainMenu.addItem(skillsMenu);
        final SingleViewMenu spellsMenu = Menus.createSimpleInventoryMenu(this, "Right click to see All Spells", 54);
        spellsMenu.setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player != null) {
                    spellsMenu.updateView(spellsMenu, player);
                }
            }
        });
        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName("All Spells");
        item.setItemMeta(meta);
        spellsMenu.setItemStack(item);
        mainMenu.addItem(spellsMenu);
        MenuItem soulStoneMenuItem = new SoulStone(this).createLearningMenuItem();
        soulStoneItem = soulStoneMenuItem.getItemStack();
        spellsMenu.addItem(soulStoneMenuItem);
        spellsMenu.addItem(new MenuItem("Right click to go back to the previous menu").setItemStack(new ItemStack(goBackItem)).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player != null) {
                    mainMenu.updateView(mainMenu, player);
                }
            }
        }));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be in game.");
            return true;
        }
        final Player player = (Player) sender;
        if (command.getName().equals("darkages")) {
            openMasterMenu(player);
        } else if (command.getName().equals("abilitybook")) {

        }
        return true;
    }

    public void openMasterMenu(@NotNull final Player player) {
        mainMenu.updateView(mainMenu, player);
    }

    public DeathHandler getDeathHandler() {
        return deathHandler;
    }
}
