package darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import darkages.ability.Ability;
import darkages.ability.AbilityDetails;
import darkages.ability.CustomEnchantment;
import darkages.ability.skills.Ambush;
import darkages.ability.skills.ShadowFigure;
import darkages.ability.special.ButterflyWing;
import darkages.ability.special.SoulStone;
import darkages.ability.spells.BeagIoc;
import darkages.ability.spells.Dachaidh;
import darkages.arena.Arena;
import darkages.arena.ArenaListener;
import darkages.character.CharacterStats;
import darkages.dao.Database;
import darkages.listeners.AbilityUseListener;
import darkages.listeners.DeathHandler;
import darkages.listeners.ItemUpdateAndDropListener;
import darkages.listeners.PlayerCancelCastListener;
import darkages.listeners.PlayerMoveListener;
import darkages.listeners.PortalListener;
import darkages.menu.LearningMenu;
import darkages.menu.SkillMenu;
import darkages.menu.SpellMenu;
import darkages.session.SessionManager;
import darkages.util.CitizensLink;
import darkages.util.ImmutableLocation;
import darkages.util.Log;
import darkages.util.StringTools;
import darkages.util.TownyLink;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.bukkit.AbstractBukkitPlugin;
import pluginbase.jdbc.SpringDatabaseSettings;
import pluginbase.jdbc.SpringJdbcAgent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DarkAgesPlugin extends AbstractBukkitPlugin {

    private SingleViewMenu mainMenu;
    private DeathHandler deathHandler;
    private Permission permission;
    private TownyLink townyLink;
    private WorldGuardPlugin worldGuard;

    private SessionManager sessionManager;

    private SpringJdbcAgent jdbcAgent;
    private DatabaseThread dbThread;

    private final Map<String, Arena> arenas = new HashMap<String, Arena>(5);

    @Override
    public void onPluginLoad() {
        Log.init(this);
        ConfigurationSerialization.registerClass(CharacterStats.class);
        ConfigurationSerialization.registerClass(Arena.class);
        ConfigurationSerialization.registerClass(ImmutableLocation.class);
    }

    @Override
    public void onPluginEnable() {
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

        sessionManager = new SessionManager(this);

        // Setup admin menu
        mainMenu = new LearningMenu(this).buildMenu();

        // Setup listeners
        new AbilityUseListener(this);
        deathHandler = new DeathHandler(this);
        new ItemUpdateAndDropListener(this);
        new PlayerMoveListener(this);
        new PlayerCancelCastListener(this);
        new PortalListener(this);

        // Setup recipes
        ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(AbilityDetails.SOUL_STONE.getItemStack()));
        recipe.addIngredient(1, Material.GHAST_TEAR);
        Bukkit.addRecipe(recipe);

        recipe = new ShapelessRecipe(new ItemStack(AbilityDetails.BUTTERFLY_WING.getItemStack()));
        recipe.addIngredient(5, Material.FEATHER);
        Bukkit.addRecipe(recipe);

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

        // Setup jdbc agent
        initializeDatabase();
        dbThread = new DatabaseThread(getJdbcAgent());
        dbThread.start();
    }

    private void initializeDatabase() {
        try {
            jdbcAgent = SpringJdbcAgent.createAgent(loadDatabaseSettings(new SpringDatabaseSettings()), getDataFolder(), getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public SpringJdbcAgent getJdbcAgent() {
        return jdbcAgent;
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
        new ButterflyWing(this);
        new Ambush(this);
        new ShadowFigure(this);
        new BeagIoc(this);
        new Dachaidh(this);
    }

    @Override
    public void onPluginDisable() {
        Ability.ABILITY_ITEMS.clear();
        Ability.LEARNING_ITEMS.clear();
        dbThread.interrupt();
    }

    @Override
    public void onReloadConfig() {
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
    public SessionManager getSessionManager() {
        return sessionManager;
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

    @NotNull
    @Override
    public String getCommandPrefix() {
        return "da";
    }

    @NotNull
    public Database getDAO() {
        return dbThread;
    }
}
