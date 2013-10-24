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
        details = AbilityDetails.SPAWN_TELEPORT,
        description = "Teleports you to the spawn.",
        retainOnDeath = true,
        allowDrop = true,
        castTime = 10,
        cooldown = 300
)
public class SpawnTeleport extends Ability {

    public SpawnTeleport(final DarkAgesPlugin plugin) {
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
        Location loc = Bukkit.getWorlds().get(0).getSpawnLocation();
        player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 0.5F, 0.5F);
        player.teleport(loc);
        return player.getLocation().equals(loc);
    }
}
