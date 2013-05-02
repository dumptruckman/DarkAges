package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityDetails;
import com.dumptruckman.minecraft.darkages.ability.CustomEnchantment;
import com.dumptruckman.minecraft.darkages.ability.skills.Ambush;
import com.dumptruckman.minecraft.darkages.ability.skills.ShadowFigure;
import com.dumptruckman.minecraft.darkages.ability.special.SoulStone;
import com.dumptruckman.minecraft.darkages.ability.spells.BeagIoc;
import com.dumptruckman.minecraft.darkages.ability.spells.Dachaidh;
import com.dumptruckman.minecraft.darkages.arena.Arena;
import com.dumptruckman.minecraft.darkages.arena.ArenaListener;
import com.dumptruckman.minecraft.darkages.character.CharacterData;
import com.dumptruckman.minecraft.darkages.listeners.AbilityUseListener;
import com.dumptruckman.minecraft.darkages.listeners.DeathHandler;
import com.dumptruckman.minecraft.darkages.listeners.ItemUpdateAndDropListener;
import com.dumptruckman.minecraft.darkages.listeners.PlayerCancelCastListener;
import com.dumptruckman.minecraft.darkages.listeners.PlayerMoveListener;
import com.dumptruckman.minecraft.darkages.listeners.PortalListener;
import com.dumptruckman.minecraft.darkages.menu.LearningMenu;
import com.dumptruckman.minecraft.darkages.menu.SkillMenu;
import com.dumptruckman.minecraft.darkages.menu.SpellMenu;
import com.dumptruckman.minecraft.darkages.tasks.TickTask;
import com.dumptruckman.minecraft.darkages.util.CitizensLink;
import com.dumptruckman.minecraft.darkages.util.ImmutableLocation;
import com.dumptruckman.minecraft.darkages.util.Log;
import com.dumptruckman.minecraft.darkages.util.StringTools;
import com.dumptruckman.minecraft.darkages.util.TownyLink;
import com.dumptruckman.minecraft.darkages.website.WebsiteConnection;
import com.dumptruckman.minecraft.pluginbase.logging.LoggablePlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DarkAgesPlugin extends JavaPlugin implements LoggablePlugin, Listener {

    private SingleViewMenu mainMenu;
    private DeathHandler deathHandler;
    private Permission permission;
    private TownyLink townyLink;
    private WorldGuardPlugin worldGuard;

    private final Map<Player, PlayerSession> playerSessions = new HashMap<Player, PlayerSession>(Bukkit.getMaxPlayers());
    private final Map<String, Arena> arenas = new HashMap<String, Arena>(5);

    @Override
    public void onLoad() {
        Log.init(this);
        ConfigurationSerialization.registerClass(CharacterData.class);
        ConfigurationSerialization.registerClass(Arena.class);
        ConfigurationSerialization.registerClass(ImmutableLocation.class);
    }

    @Override
    public void onEnable() {
        //fixUpBukkitEnchantment(); This doesn't work yet!

        // Setup vault permissions
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        // Setup WorldGuard
        worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

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
        new PortalListener(this);
        getServer().getPluginManager().registerEvents(this, this);

        // Setup recipes
        ShapelessRecipe soulStoneRecipe = new ShapelessRecipe(new ItemStack(AbilityDetails.SOUL_STONE.getItemStack()));
        soulStoneRecipe.addIngredient(1, Material.GHAST_TEAR);
        Bukkit.addRecipe(soulStoneRecipe);

        // Load arenas
        loadArenas();
        new ArenaListener(this);

        // Setup npc traits
        try {
            Class.forName("net.citizensnpcs.api.CitizensAPI");
            CitizensLink.registerTraits();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Setup tick task
        new TickTask(this).runTaskTimer(this, 1L, 1L);
    }

    private File getArenasFile() {
        return new File(getDataFolder(), "arenas.yml");
    }

    private void loadArenas() {
        arenas.clear();
        FileConfiguration config = YamlConfiguration.loadConfiguration(getArenasFile());
        for (String arenaName : config.getKeys(false)) {
            try {
                arenas.put(arenaName, (Arena) config.get(arenaName));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        try {
            final WebsiteConnection connection = new WebsiteConnection(this, player);
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!connection.isUserRegistered()) {
                            Bukkit.getScheduler().runTask(DarkAgesPlugin.this, new Runnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.AQUA + "If you like the server, check out the website " + ChatColor.BLUE + ChatColor.UNDERLINE + "http://gnarbros.dyndns.org");
                                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "To register for a website account type " + ChatColor.GOLD + "/register");
                                }
                            });
                        }
                        connection.closeConnection();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, 30L);
        } catch (ClassNotFoundException ignore) { }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        if (playerSessions.containsKey(event.getPlayer())) {
            playerSessions.get(event.getPlayer()).endSession();
            playerSessions.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void playerCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].startsWith("/") && args[0].length() > 1) {
            args[0] = args[0].substring(1);
        }
        final Player player = event.getPlayer();
        if (args[0].equalsIgnoreCase("register")) {
            event.setCancelled(true);
            if (args.length < 3) {
                player.sendMessage("usage: /register <email> <password>");
                return;
            }
            try {
                final String email = args[1];
                final String password = args[2];
                final WebsiteConnection connection = new WebsiteConnection(this, player);
                if (!connection.isValidEmailAddress(email)) {
                    player.sendMessage(ChatColor.RED + "The email address you entered does not appear to be valid!");
                    return;
                }
                if (password.length() < 6) {
                    player.sendMessage(ChatColor.RED + "Your password should be at least 6 characters in length!");
                    return;
                }
                player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Just a moment...");
                Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.registerUser(password, email);
                            Bukkit.getScheduler().runTask(DarkAgesPlugin.this, new Runnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.GREEN + "You have successfully registered for the website!");
                                    player.sendMessage(ChatColor.GRAY + "Your username is the same as your minecraft username!");
                                }
                            });
                        } catch (final Exception e) {
                            Bukkit.getScheduler().runTask(DarkAgesPlugin.this, new Runnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.RED + e.getMessage());
                                }
                            });
                        }
                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Yikes! Something went wrong.  Contact dumptruckman!");
            }
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
        new ShadowFigure(this);
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
        loadArenas();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
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
        } else if (command.getName().equals("arena")) {
            args = StringTools.joinArgs(args);
            if (args.length < 2) {
                return false;
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (args.length < 3) {
                    return false;
                }
                Arena arena = getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "No region named: " + args[1]);
                } else {
                    arena.setLocation(args[2], new ImmutableLocation(player.getLocation()));
                    player.sendMessage(ChatColor.GREEN + "Set spawn area '" + args[2] +"' for arena '" + args[1] + "' to your location");
                    saveArenas();
                }
                return true;
            } else if (args[0].equalsIgnoreCase("clear")) {
                if (args.length < 2) {
                    return false;
                }
                Arena arena = getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "No region named: " + args[1]);
                } else {
                    arena.clearLocations();
                    player.sendMessage(ChatColor.GREEN + "Cleared spawn areas for '" + args[1] + "'!");
                    saveArenas();
                }
                return true;
            } else if (args[0].equalsIgnoreCase("respawn")) {
                if (args.length < 2) {
                    return false;
                }
                Arena arena = getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "No region named: " + args[1]);
                } else {
                    arena.setRespawnLocation(new ImmutableLocation(player.getLocation()));
                    player.sendMessage(ChatColor.GREEN + "Set respawn area for arena '" + args[1] + "' to your location");
                    saveArenas();
                }
                return true;
            } else if (args[0].equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    return false;
                }
                ProtectedRegion region = getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[1]);
                if (region == null) {
                    player.sendMessage(ChatColor.RED + "No region named: " + args[1]);
                } else {
                    createArena(args[1], args[2]);
                    player.sendMessage(ChatColor.GREEN + "Assigned arena '" + args[2] + "' to region '" + args[1] + "'");
                    saveArenas();
                }
                return true;
            }
            return false;
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
    public PlayerSession getPlayerSession(@NotNull final Player player) {
        PlayerSession session = playerSessions.get(player);
        if (session == null) {
            File folder = new File(getDataFolder(), "players");
            folder.mkdirs();
            File file = new File(folder, player.getName() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            CharacterData data;
            if (config.contains("data")) {
                Object obj = config.get("data");
                if (obj instanceof CharacterData) {
                    data = (CharacterData) obj;
                    Log.fine("Loaded character data for %s", player.getName());
                } else {
                    Log.severe("Could not load character data!");
                    data = new CharacterData();
                    config.set("data", data);
                }
            } else {
                Log.fine("Creating new character data for %s", player.getName());
                data = new CharacterData();
                config.set("data", data);
            }
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            session = new PlayerSession(this, player, data);
            playerSessions.put(player, session);
        }
        return session;
    }

    @Nullable
    public PlayerSession getPlayerSessionWhereTargeted(@NotNull final Player player) {
        for (PlayerSession session : playerSessions.values()) {
            if (session.getTarget() != null && session.getTarget().equals(player)) {
                return session;
            }
        }
        return null;
    }

    public Collection<PlayerSession> getPlayerSessions() {
        return playerSessions.values();
    }

    @Nullable
    public TownyLink getTownyLink() {
        return townyLink;
    }

    @Nullable
    public Arena getArena(String name) {
        return arenas.get(name);
    }

    @NotNull
    public Arena createArena(String regionId, String name) {
        Arena arena = new Arena(regionId, name);
        arenas.put(regionId, arena);
        return arena;
    }

    public void saveArenas() {
        File file = getArenasFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String name : arenas.keySet()) {
            config.set(name, arenas.get(name));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }
}
