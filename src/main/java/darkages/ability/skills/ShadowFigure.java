package darkages.ability.skills;

import darkages.DarkAgesPlugin;
import darkages.ability.AbilityDetails;
import darkages.ability.AbilityInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityInfo(
        details = AbilityDetails.SHADOW_FIGURE,
        description = "Teleports you to the other\nside of a very close enemy.\nDeals light damage.",
        range = 2,
        inventoryLimit = 1,
        consumesAbilityItem = false,
        destroyedOnDeath = true,
        requiresTarget = true
)
public class ShadowFigure extends Ambush {

    public ShadowFigure(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 5;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        if (super.onAbilityUse(player)) {
            LivingEntity target = plugin.getPlayerSession(player).getTarget();
            target.damage(1, player);
            return true;
        }
        return false;
    }
}
