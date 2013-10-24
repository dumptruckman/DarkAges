package darkages.ability.special;

import darkages.DarkAgesPlugin;
import darkages.ability.Ability;
import darkages.ability.AbilityDetails;
import darkages.ability.AbilityInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@AbilityInfo(
        details = AbilityDetails.BUTTERFLY_WING,
        description = "Teleports you to your home.",
        retainOnDeath = true,
        allowDrop = true,
        castTime = 5,
        cooldown = 300
)
public class ButterflyWing extends Ability {

    public ButterflyWing(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 1;
    }

    @Override
    protected boolean canUseAbility(final Player player) {
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        Location loc = player.getBedSpawnLocation();
        if (loc == null && plugin.getTownyLink() != null) {
            loc = plugin.getTownyLink().getTownSpawn(player);
        }
        if (loc == null) {
            loc = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 0.5F, 0.5F);
        player.teleport(loc);
        return player.getLocation().equals(loc);
    }
}
