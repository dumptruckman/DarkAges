package darkages.listeners;

import darkages.DarkAgesPlugin;
import darkages.session.PlayerSession;
import darkages.util.EntityTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerCancelCastListener implements Listener {

    private final DarkAgesPlugin plugin;

    public PlayerCancelCastListener(final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void switchItem(final PlayerItemHeldEvent event) {
        PlayerSession session = plugin.getSessionManager().getPlayerSession(event.getPlayer());
        session.cancelCast();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerTeleport(PlayerTeleportEvent event) {
        PlayerSession session = plugin.getSessionManager().getPlayerSessionWhereTargeted(event.getPlayer());
        if (session != null && session.isCasting() && !session.getTarget().equals(event.getPlayer()) && session.getCastingTask().getCastingAbility().getInfo().range() > 0) {
            if (EntityTools.getDistance(session.getPlayer(), event.getPlayer()) > session.getCastingTask().getCastingAbility().getInfo().range()) {
                session.setTarget(null);
            }
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        PlayerSession session = plugin.getSessionManager().getPlayerSessionWhereTargeted(event.getPlayer());
        if (session != null && session.isCasting() && !session.getTarget().equals(event.getPlayer()) && session.getCastingTask().getCastingAbility().getInfo().range() > 0) {
            session.setTarget(null);
        }
    }
}
