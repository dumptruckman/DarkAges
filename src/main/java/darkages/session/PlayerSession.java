package darkages.session;

import darkages.DarkAgesPlugin;
import darkages.ability.Ability;
import darkages.ability.CastingTask;
import darkages.character.PlayerCharacter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PlayerSession {

    private static final PotionEffectType CAST_EFFECT = PotionEffectType.CONFUSION;

    private class CancelPortalLimitation implements Runnable {
        @Override
        public void run() {
            castingBlock = null;
        }
    }

    private final DarkAgesPlugin plugin;
    private final Player player;

    private final PlayerCharacter character;

    private LivingEntity target = null;
    private CastingTask castingTask = null;
    private BlockState preCastingBlockState = null;
    private Block castingBlock = null;
    private boolean confusedBeforeCasting = false;
    private final CancelPortalLimitation portalLimitationCanceller = new CancelPortalLimitation();

    PlayerSession(final DarkAgesPlugin plugin, final Player player, final PlayerCharacter character) {
        this.plugin = plugin;
        this.player = player;
        this.character = character;
    }

    public Player getPlayer() {
        return player;
    }

    public long getPlayerId() {
        return character.getPlayerId();
    }

    public PlayerCharacter getCharacter() {
        return character;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(final LivingEntity target) {
        this.target = target;
    }

    public boolean isCasting() {
        return castingTask != null;
    }

    public CastingTask getCastingTask() {
        return castingTask;
    }

    public void beginCast(final Ability ability) {
        castingTask = new CastingTask(this, ability);
        if (ability.getInfo().castTime() > 0) {
            try {
                castingTask.runTaskLater(plugin, ability.getInfo().castTime() * 20L);
                createCastingEffect(ability);
                sendCastingMessage(ability);
            } catch (IllegalStateException e) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Something went wrong!!!");
            }
        } else {
            castingTask.run();
            castingTask = null;
        }
    }

    private void createCastingEffect(Ability ability) {
        castingBlock = player.getLocation().getBlock();
        preCastingBlockState = castingBlock.getState();
        castingBlock.setTypeId(Material.PORTAL.getId(), false);
        castingBlock.getState().update(true);
        confusedBeforeCasting = getPlayer().hasPotionEffect(CAST_EFFECT);
        getPlayer().addPotionEffect(new PotionEffect(CAST_EFFECT, ability.getInfo().castTime() * 20, 1, true));
    }

    private void sendCastingMessage(Ability ability) {
        if (target != null) {
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                if (targetPlayer.equals(player)) {
                    player.sendMessage("Casting " + ability.getDetails().getColor() + ability.getDetails().getName() + ChatColor.RESET + " on " + ChatColor.DARK_GRAY + "self" + ChatColor.RESET + "...");
                } else {
                    player.sendMessage("Casting " + ability.getDetails().getColor() + ability.getDetails().getName() + ChatColor.RESET + " on " + ChatColor.BOLD + targetPlayer.getName() + ChatColor.RESET + "...");
                }
            } else {
                player.sendMessage("Casting " + ability.getDetails().getColor() + ability.getDetails().getName() + ChatColor.RESET + " on " + ChatColor.BOLD + target.getType().getName() + ChatColor.RESET + "...");
            }
        } else {
            player.sendMessage("Casting " + ability.getDetails().getColor() + ability.getDetails().getName() + ChatColor.RESET + "...");
        }
    }

    public void endSession() {
        cancelCast();
    }

    public void cancelCast() {
        if (castingTask != null) {
            castingTask.cancel();
            clearCastTask();
        }
    }

    public void clearCastTask() {
        if (castingTask != null && !confusedBeforeCasting) {
            getPlayer().removePotionEffect(CAST_EFFECT);
        }
        castingTask = null;
        if (preCastingBlockState != null) {
            preCastingBlockState.update(true);
        }
        preCastingBlockState = null;
        Bukkit.getScheduler().runTaskLater(plugin, portalLimitationCanceller, 1L);
    }

    public boolean isAllowedToPortal() {
        return !isCasting() && castingBlock == null;
    }

    public Block getCastingIndicatorBlock() {
        return castingBlock;
    }

    public void regenerateHealthAndMana() {
        if (!player.isDead() && (player.getHealth() != player.getMaxHealth() || player.getFoodLevel() == 0)) {
            temporaryRegenHealth();
            //regenerateHealth();
            //regenerateMana();
        }
    }

    private void temporaryRegenHealth() {
        double regen = getPlayer().getMaxHealth() * getRegenPercent(11);
        double healthAmount;
        if (regen < 0) {
            healthAmount = (int) Math.floor(regen);
        } else {
            healthAmount = (int) Math.round(regen);
        }
        if (healthAmount + getPlayer().getHealth() > getPlayer().getMaxHealth()) {
            healthAmount = getPlayer().getMaxHealth() - getPlayer().getHealth();
        }

        if (healthAmount < 0) {
            getPlayer().damage(-healthAmount);
        } else if (healthAmount > 0) {
            getPlayer().setHealth(healthAmount + getPlayer().getHealth());
        }
    }

    private double getRegenPercent(int stat) {
        int diff = stat - getCharacter().getStats().getLevel();
        if (diff > 10) {
            diff = 10;
        } else if (diff < 0) {
            diff = 0;
        }
        return (diff / 100D + .1D) * getHungerFactor();
    }

    private static final double FOOD_LEVEL_FACTOR = 1.2D;
    private static final double MAX_FOOD = 20;
    private static final double HUNGER_REGEN_OFFSET = 0.05D;

    private double getHungerFactor() {
        return getPlayer().getFoodLevel() * FOOD_LEVEL_FACTOR / MAX_FOOD - HUNGER_REGEN_OFFSET;
    }

    private void regenerateHealth() {
        int diff = getCharacter().getStats().getConstitution() - getCharacter().getStats().getLevel();
        if (diff > 10) {
            diff = 10;
        } else if (diff < 0) {
            diff = 0;
        }
        int health = (int) (getCharacter().getStats().getHealth() + (getCharacter().getStats().getMaxHealth() * (diff / 100D + .1D)));
        if (health > getCharacter().getStats().getMaxHealth()) {
            health = getCharacter().getStats().getMaxHealth();
        }
        getCharacter().getStats().setHealth(health);
    }

    private void regenerateMana() {
        int diff = getCharacter().getStats().getWisdom() - getCharacter().getStats().getLevel();
        if (diff > 10) {
            diff = 10;
        } else if (diff < 0) {
            diff = 0;
        }
        int mana = (int) (getCharacter().getStats().getMana() + (getCharacter().getStats().getMaxMana() * (diff / 100D + .1D)));
        if (mana > getCharacter().getStats().getMaxMana()) {
            mana = getCharacter().getStats().getMaxMana();
        }
        getCharacter().getStats().setMana(mana);
    }
}
