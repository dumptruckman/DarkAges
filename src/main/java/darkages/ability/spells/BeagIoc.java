package darkages.ability.spells;

import darkages.DarkAgesPlugin;
import darkages.ability.Ability;
import darkages.ability.AbilityDetails;
import darkages.ability.AbilityInfo;
import darkages.util.EntityTools;
import net.minecraft.server.v1_6_R3.EntityFireworks;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Field;

@AbilityInfo(
        details = AbilityDetails.BEAG_IOC,
        description = "Heals you or another player\nfor a small amount.",
        range = 51,
        castTime = 1,
        consumesAbilityItem = true,
        allowDrop = true,
        requiresTarget = true
)
public class BeagIoc extends Ability {

    private static final int HEAL_AMOUNT = 2;

    public BeagIoc(final DarkAgesPlugin plugin) {
        super(plugin);
    }

    @Override
    protected int getLevel() {
        return 1;
    }

    @Override
    protected boolean canUseAbility(final Player player) {
        LivingEntity target = EntityTools.getTargetedLivingEntity(player, info.range());
        if (target == null) {
            target = player;
        } else {
            if (!(target instanceof Player)) {
                target = player;
                //player.sendMessage(ChatColor.RED + "Can only target players!");
                //return false;
            }
        }
        plugin.getSessionManager().getPlayerSession(player).setTarget(target);
        return true;
    }

    @Override
    protected boolean onAbilityUse(final Player player) {
        LivingEntity target = plugin.getSessionManager().getPlayerSession(player).getTarget();
        double newHealth = target.getHealth();
        newHealth += HEAL_AMOUNT;
        if (newHealth > 20) {
            newHealth = 20;
        }
        target.setHealth(newHealth);
        Location fxLoc = target.getEyeLocation();
        Firework firework = (Firework) fxLoc.getWorld().spawnEntity(fxLoc, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.BLUE, Color.BLUE, Color.WHITE)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL).build());
        firework.setFireworkMeta(meta);
        EntityFireworks eFirework = ((CraftFirework) firework).getHandle();
        eFirework.expectedLifespan = 1;
        try {
            Field field = eFirework.getClass().getDeclaredField("ticksFlown");
            field.setAccessible(true);
            field.set(eFirework, 1);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

}
