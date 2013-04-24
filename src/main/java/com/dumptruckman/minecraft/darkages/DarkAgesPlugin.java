package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.abilities.skills.Ambush;
import com.dumptruckman.minecraft.darkages.abilities.special.SoulStone;
import com.dumptruckman.minecraft.pluginbase.logging.LoggablePlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class DarkAgesPlugin extends JavaPlugin implements LoggablePlugin {

    private SingleViewMenu mainMenu;
    private DeathHandler deathHandler;
    private Permission permission;

    public ItemStack soulStoneItem;

    @Override
    public void onEnable() {
        //fixUpBukkitEnchantment(); This doesn't work yet!

        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        initializeAbilities();

        mainMenu = new LearningMenu(this).buildMenu();
        new AbilityUseListener(this);
        deathHandler = new DeathHandler(this);
        new ItemUpdateAndDropListener(this);

        // Setup souls tone recipe
        ShapelessRecipe soulStoneRecipe = new ShapelessRecipe(new ItemStack(soulStoneItem));
        soulStoneRecipe.addIngredient(1, Material.GHAST_TEAR);
        Bukkit.addRecipe(soulStoneRecipe);
    }

    private void fixUpBukkitEnchantment() {
        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            CustomEnchantment.okayToEnchant = true;
        } catch (Exception e) {
            getLogger().warning("CANNOT APPLY CUSTOM ENCHANT!");
            CustomEnchantment.okayToEnchant = false;
            return;
        }
    }

    private void initializeAbilities() {
        SoulStone soulStone = new SoulStone(this);
        this.soulStoneItem = soulStone.abilityItem;
        new Ambush(this);
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
        } else if (command.getName().equals("skills")) {
            openSkillMenu(player);
        } else if (command.getName().equals("spells")) {
            openSpellMenu(player);
        }
        return true;
    }

    public Permission getPermission() {
        return permission;
    }

    public void openMasterMenu(@NotNull final Player player) {
        mainMenu.updateView(mainMenu, player);
    }

    public void openSkillMenu(@NotNull final Player player) {
        SingleViewMenu menu = new SkillMenu(this, player).buildMenu();
        menu.updateView(menu, player);
    }

    public void openSpellMenu(@NotNull final Player player) {
        SingleViewMenu menu = new SpellMenu(this, player).buildMenu();
        menu.updateView(menu, player);
    }

    public DeathHandler getDeathHandler() {
        return deathHandler;
    }
}
