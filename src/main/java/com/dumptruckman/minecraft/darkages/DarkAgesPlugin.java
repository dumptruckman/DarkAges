package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.PlayerSession;
import com.dumptruckman.minecraft.darkages.ability.skills.Ambush;
import com.dumptruckman.minecraft.darkages.ability.special.SoulStone;
import com.dumptruckman.minecraft.darkages.ability.spells.BeagIoc;
import com.dumptruckman.minecraft.darkages.ability.spells.Dachaidh;
import com.dumptruckman.minecraft.darkages.listeners.AbilityUseListener;
import com.dumptruckman.minecraft.darkages.listeners.DeathHandler;
import com.dumptruckman.minecraft.darkages.listeners.ItemUpdateAndDropListener;
import com.dumptruckman.minecraft.darkages.listeners.PlayerCancelCastListener;
import com.dumptruckman.minecraft.darkages.listeners.PlayerMoveListener;
import com.dumptruckman.minecraft.darkages.util.TownyLink;
import com.dumptruckman.minecraft.pluginbase.logging.LoggablePlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DarkAgesPlugin extends JavaPlugin implements LoggablePlugin, Listener {

    private SingleViewMenu mainMenu;
    private DeathHandler deathHandler;
    private Permission permission;
    private TownyLink townyLink;

    public final Map<Player, PlayerSession> playerSessions = new HashMap<Player, PlayerSession>(Bukkit.getMaxPlayers());

    @Override
    public void onEnable() {
        //fixUpBukkitEnchantment(); This doesn't work yet!

        // Setup vault permissions
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        // Setup towny
        Plugin plugin = getServer().getPluginManager().getPlugin("Towny");
        if (plugin != null) {
            townyLink = new TownyLink();
        }

        // Setup abilities
        initializeAbilities();

        // Setup admin menu
        mainMenu = new LearningMenu(this).buildMenu();

        // Setup listeners
        new AbilityUseListener(this);
        deathHandler = new DeathHandler(this);
        new ItemUpdateAndDropListener(this);
        new PlayerMoveListener(this);
        new PlayerCancelCastListener(this);
        getServer().getPluginManager().registerEvents(this, this);

        // Setup recipes
        ShapelessRecipe soulStoneRecipe = new ShapelessRecipe(new ItemStack(AbilityDetails.SOUL_STONE.getItemStack()));
        soulStoneRecipe.addIngredient(1, Material.GHAST_TEAR);
        Bukkit.addRecipe(soulStoneRecipe);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        if (playerSessions.containsKey(event.getPlayer())) {
            playerSessions.get(event.getPlayer()).endSession();
            playerSessions.remove(event.getPlayer());
        }
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
        new SoulStone(this);
        new Ambush(this);
        new BeagIoc(this);
        new Dachaidh(this);
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

    @NotNull
    public PlayerSession getSession(@NotNull final Player player) {
        PlayerSession session = playerSessions.get(player);
        if (session == null) {
            session = new PlayerSession(this, player);
            playerSessions.put(player, session);
        }
        return session;
    }

    @Nullable
    public PlayerSession getSessionWhereTargeted(@NotNull final Player player) {
        for (PlayerSession session : playerSessions.values()) {
            if (session.getTarget() != null && session.getTarget().equals(player)) {
                return session;
            }
        }
        return null;
    }

    @Nullable
    public TownyLink getTownyLink() {
        return townyLink;
    }
}
