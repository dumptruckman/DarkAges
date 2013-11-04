package darkages.session;

import darkages.DarkAgesPlugin;
import darkages.character.PlayerCharacter;
import darkages.util.Log;
import darkages.website.WebsiteConnection;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.messages.ChatColor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SessionManager implements Listener {

    private DarkAgesPlugin plugin;
    private final Map<Player, PlayerSession> playerSessions = new HashMap<>(Bukkit.getMaxPlayers());

    public SessionManager(@NotNull DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new RegenTickTask(this).runTaskTimer(plugin, 1L, 1L);
    }

    @NotNull
    public PlayerSession getPlayerSession(@NotNull Player player) {
        PlayerSession session = playerSessions.get(player);
        if (session == null) {
            session = cachePlayerSession(player);
        }
        return session;
    }

    private PlayerSession cachePlayerSession(@NotNull Player player) {
        /*
        File folder = new File(plugin.getDataFolder(), "players");
        folder.mkdirs();
        File file = new File(folder, player.getName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        CharacterStats data;
        if (config.contains("data")) {
            Object obj = config.get("data");
            if (obj instanceof CharacterStats) {
                data = (CharacterStats) obj;
                Log.fine("Loaded character data for %s", player.getName());
            } else {
                Log.severe("Could not load character data!");
                data = new CharacterStats();
                config.set("data", data);
            }
        } else {
            Log.fine("Creating new character data for %s", player.getName());
            data = new CharacterStats();
            config.set("data", data);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        //PlayerSession session = new PlayerSession(plugin, player, data);
        PlayerCharacter character = plugin.getDAO().getSelectedCharacter(player.getName());
        if (character == null) {
            character = plugin.getDAO().createCharacter(player.getName());
            Log.fine("Created character '%s' for '%s'", character, player.getName());
        } else {
            Log.finer("Got character '%s' for '%s'", character, player.getName());
        }
        PlayerSession session = new PlayerSession(plugin, player, character);

        playerSessions.put(player, session);
        return session;
    }

    public boolean isBlockACastingIndicator(@NotNull Block block) {
        for (PlayerSession session : getPlayerSessions()) {
            if (block.equals(session.getCastingIndicatorBlock())) {
                return true;
            }
        }
        return false;
    }

    private Collection<PlayerSession> getPlayerSessions() {
        return playerSessions.values();
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

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        cachePlayerSession(player);
        try {
            final WebsiteConnection connection = new WebsiteConnection(plugin, player);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!connection.isUserRegistered()) {
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
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
        PlayerSession session = playerSessions.get(event.getPlayer());
        session.endSession();
        playerSessions.remove(event.getPlayer());
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
                final WebsiteConnection connection = new WebsiteConnection(plugin, player);
                if (!connection.isValidEmailAddress(email)) {
                    player.sendMessage(ChatColor.RED + "The email address you entered does not appear to be valid!");
                    return;
                }
                if (password.length() < 6) {
                    player.sendMessage(ChatColor.RED + "Your password should be at least 6 characters in length!");
                    return;
                }
                player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Just a moment...");
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.registerUser(password, email);
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.GREEN + "You have successfully registered for the website!");
                                    player.sendMessage(ChatColor.GRAY + "Your username is the same as your minecraft username!");
                                }
                            });
                        } catch (final Exception e) {
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
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

    private static class RegenTickTask extends BukkitRunnable {

        private final SessionManager sessionManager;

        private long lastRegenTime;

        private static final long REGEN_PERIOD = 20000L;

        public RegenTickTask(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
            lastRegenTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRegenTime >= REGEN_PERIOD) {
                lastRegenTime = currentTime;
                for (PlayerSession session : sessionManager.getPlayerSessions()) {
                    session.regenerateHealthAndMana();
                }
            }
        }
    }
}
