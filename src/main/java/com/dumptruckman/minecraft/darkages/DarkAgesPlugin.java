package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.pluginbase.logging.LoggablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DarkAgesPlugin extends JavaPlugin implements LoggablePlugin {

    private SingleViewMenu mainMenu;
    private DeathHandler deathHandler;

    public ItemStack soulStoneItem;

    @Override
    public void onEnable() {
        mainMenu = new LearningMenu(this).buildMenu();
        new AbilityUseListener(this);
        deathHandler = new DeathHandler(this);
        new ItemUpdateListener(this);

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
