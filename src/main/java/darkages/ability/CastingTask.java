package darkages.ability;

import darkages.session.PlayerSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CastingTask extends BukkitRunnable {

    private final PlayerSession playerSession;
    private final Ability ability;
    private final boolean hasCastTime;

    public CastingTask(final PlayerSession playerSession, final Ability ability) {
        this.playerSession = playerSession;
        this.ability = ability;
        this.hasCastTime = ability.info.castTime() > 0;
    }

    public Ability getCastingAbility() {
        return ability;
    }

    private Player getPlayer() {
        return playerSession.getPlayer();
    }

    @Override
    public synchronized void cancel() {
        try {
            super.cancel();
            endCasting();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            getPlayer().sendMessage(ChatColor.RED + "An error occurred when canceling your cast.  Please tell dumptruckman!");
        }
    }

    @Override
    public void run() {
        if (hasCastTime && ability.info.requiresTarget() && playerSession.getTarget() == null) {
            getPlayer().sendMessage(ChatColor.RED + "Your target was lost!");
            endCasting();
            return;
        }
        if (ability.onAbilityUse(getPlayer())) {
            ability.abilityUsed(getPlayer());
        }
        endCasting();
    }

    private void endCasting() {
        playerSession.setTarget(null);
        playerSession.clearCastTask();
    }
}