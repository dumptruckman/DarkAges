package darkages.ability;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import darkages.DarkAgesPlugin;
import darkages.util.InventoryTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Ability {

    public static final int RANGE_OFFSET = 1;

    private static final Material LEARNING_MATERIAL = Material.PAPER;
    public static final String LEARN_STRING = ChatColor.GOLD + "Teaches: ";
    public static final String ABILITY_DESCRIPTION = ChatColor.GREEN + "Ability description: ";
    public static final String PREPARATION_COMPONENTS = ChatColor.YELLOW + "Preparation components: ";
    public static final String USAGE_COMPONENTS = ChatColor.GOLD + "Usage components: ";
    public static final String CASTTIME = ChatColor.WHITE + "Cast time: ";
    public static final String COOLDOWN = ChatColor.WHITE + "Cooldown: ";
    public static final String RANGE = ChatColor.WHITE + "Range: ";
    public static final String REQUIRES_LEVELS = ChatColor.AQUA + "Level cost: ";
    public static final String REQUIRED_SKILLS = ChatColor.DARK_PURPLE + "Must know: ";

    public static final Map<ItemStack, Ability> LEARNING_ITEMS = new HashMap<ItemStack, Ability>(10);
    public static final Map<ItemStack, Ability> ABILITY_ITEMS = new HashMap<ItemStack, Ability>(10);

    @NotNull
    protected final DarkAgesPlugin plugin;
    @NotNull
    protected final AbilityInfo info;
    @NotNull
    protected final ItemStack learningItem;
    @NotNull
    protected final ItemStack abilityItem;
    @NotNull
    protected final Map<String, Long> coolDowns = new HashMap<String, Long>(Bukkit.getMaxPlayers() * 2);

    protected Ability(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        final AbilityInfo abilityInfo = getClass().getAnnotation(AbilityInfo.class);
        if (abilityInfo == null) {
            throw new IllegalStateException("Ability must be annotated with AbilityInfo");
        }
        this.info = abilityInfo;

        this.learningItem = initializeLearningItemStack();
        this.abilityItem = initializeAbilityItemStack();
        LEARNING_ITEMS.put(learningItem, this);
        ABILITY_ITEMS.put(abilityItem, this);
    }

    public String getPermission() {
        return info.details().getPermission();
    }

    /*****************
     * LEARNING ITEM *
     *****************/
    private ItemStack initializeLearningItemStack() {
        final ItemStack item = new ItemStack(LEARNING_MATERIAL);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Learn " + getDetails().getName());
        List<String> lore = new ArrayList<String>(20);
        lore.add(getTag()); // This has to be the first lore!
        if (CustomEnchantment.okayToEnchant) {
            meta.addEnchant(getDetails().getType().getEnchantment(), getLevel(), true);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            switch (getDetails().getType()) {
                case SKILL:
                    lore.add(ChatColor.GRAY + "Skill");
                    break;
                case SPELL:
                    lore.add(ChatColor.GRAY + "Spell");
                    break;
                default:
                    lore.add(ChatColor.GRAY + "Special");
            }
        }
        lore.add(LEARN_STRING + getDetails().getName());
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = info.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        lore.add(REQUIRES_LEVELS + getDetails().getLearnCost());
        if (info.requirements().length > 0) {
            lore.add(REQUIRED_SKILLS);
            for (AbilityDetails abilityRequirement : info.requirements()) {
                lore.add(ChatColor.DARK_PURPLE + "  " + abilityRequirement.getName());
            }
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to learn.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public AbilityDetails getDetails() {
        return info.details();
    }

    public AbilityInfo getInfo() {
        return info;
    }

    /****************
     * ABILITY ITEM *
     ****************/
    private ItemStack initializeAbilityItemStack() {
        final ItemStack item = info.details().getItemStack();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getDetails().getColor() + getDetails().getName());
        List<String> lore = new ArrayList<String>(20);
        lore.add(getTag()); // This has to be the first lore!
        if (CustomEnchantment.okayToEnchant) {
            meta.addEnchant(getDetails().getType().getEnchantment(), getLevel(), true);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            switch (getDetails().getType()) {
                case SKILL:
                    lore.add(ChatColor.GRAY + "Skill");
                    break;
                case SPELL:
                    lore.add(ChatColor.GRAY + "Spell");
                    break;
                default:
                    lore.add(ChatColor.GRAY + "Special");
            }
        }
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = info.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        if (info.details().getComponents().length > 0) {
            ChatColor color = null;
            if (getDetails().getType() == AbilityType.SPELL) {
                lore.add(PREPARATION_COMPONENTS);
                color = ChatColor.YELLOW;
            } else if (getDetails().getType() == AbilityType.SKILL) {
                lore.add(USAGE_COMPONENTS);
                color = ChatColor.GOLD;
            }
            if (color != null) {
                for (ItemStack component : info.details().getComponents()) {
                    lore.add(color + "  " + component.getType().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getAmount());
                }
            }
        }
        if (info.castTime() > 0) {
            lore.add(CASTTIME + info.castTime() + " second" + (info.castTime() > 1 ? "s" : ""));
        }
        if (info.cooldown() > 0) {
            lore.add(COOLDOWN + info.cooldown() + " second" + (info.cooldown() > 1 ? "s" : ""));
        }
        if (info.range() > 0) {
            lore.add(RANGE + (info.range() - RANGE_OFFSET));
        }
        if (info.retainOnDeath()) {
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Remains on death");
        }
        if (info.destroyedOnDeath()) {
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Destroyed on death");
        }
        if (!info.allowDrop()) {
            lore.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Soulbound");
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to use.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public MenuItem createLearningMenuItem() {
        return new MenuItem("Click to obtain learning item").setItemStack(learningItem).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player != null) {
                    player.getInventory().addItem(new ItemStack(learningItem));
                }
            }
        });
    }

    @NotNull
    public String getTag() {
        return getDetails().getTag();
    }

    public MenuItem createAbilityMenuItem() {
        return new MenuItem("Click to obtain usage item").setItemStack(abilityItem).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player == null) {
                    return;
                }
                if  (info.inventoryLimit() > 0) {
                    ItemStack findItem = new ItemStack(info.details().getItemStack());
                    findItem.setAmount(info.inventoryLimit());
                    if (InventoryTools.contains(player.getInventory(), findItem)) {
                        player.sendMessage(ChatColor.RED + "You may not have more than " + info.inventoryLimit() + " of those.");
                        return;
                    }
                }
                if (getDetails().getType() == AbilityType.SPELL) {
                    for (ItemStack component : info.details().getComponents()) {
                        if (!InventoryTools.contains(player.getInventory(), component)) {
                            player.sendMessage(ChatColor.RED + "You do not have the required components to prepare this ability!");
                            return;
                        }
                    }
                }
                if (!player.getInventory().addItem(new ItemStack(abilityItem)).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You do not have room in your inventory to prepare this ability!");
                }
                if (getDetails().getType() == AbilityType.SPELL) {
                    for (ItemStack component : info.details().getComponents()) {
                        InventoryTools.remove(player.getInventory(), component);
                    }
                }
            }
        });
    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Ability && ((Ability) obj).info.equals(info);
    }

    public boolean isDestroyedOnDeath() {
        return info.destroyedOnDeath();
    }

    public boolean isRetainedOnDeath() {
        return info.retainOnDeath();
    }

    public boolean isAllowedToDrop() {
        return info.allowDrop();
    }

    public void learnAbility(final Player player) {
        if (player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + "You already possess this knowledge!");
            return;
        }
        if (player.getLevel() < getDetails().getLearnCost()) {
            player.sendMessage(ChatColor.RED + "You do not possess enough experience to learn this!");
            return;
        }
        for (AbilityDetails requiredAbility : info.requirements()) {
            if (!player.hasPermission(requiredAbility.getPermission())) {
                player.sendMessage(ChatColor.RED + "You lack knowledge of the required ability: " + requiredAbility.getColor() + requiredAbility.getColor());
                return;
            }
        }
        plugin.getPermission().playerAdd(player, getPermission());
        player.giveExpLevels(-getDetails().getLearnCost());
        ItemStack item = player.getItemInHand();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.updateInventory();
        } else {
            player.setItemInHand(null);
        }
        player.sendMessage(ChatColor.GREEN + "You now posses the knowledge of " + getDetails().getColor() + getDetails().getName() + ChatColor.GREEN + "!");
    }

    public void useAbility(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        if (!player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have the knowledge required to use this ability!");
            return;
        }
        if (info.cooldown() > 0 && coolDowns.containsKey(playerName)) {
            if (System.currentTimeMillis() - coolDowns.get(playerName) <= info.cooldown() * 1000) {
                player.sendMessage(ChatColor.RED + "That ability is on cooldown!");
                return;
            }
        }
        if (getDetails().getType() == AbilityType.SKILL) {
            for (ItemStack component : info.details().getComponents()) {
                if (!InventoryTools.contains(player.getInventory(), component)) {
                    player.sendMessage(ChatColor.RED + "You do not have the required components to use this ability!");
                    return;
                }
            }
        }
        plugin.getPlayerSession(player).cancelCast();
        if (canUseAbility(player)) {
            plugin.getPlayerSession(player).beginCast(this);
        }
    }

    void abilityUsed(final Player player) {
        if (info.consumesAbilityItem()) {
            InventoryTools.remove(player.getInventory(), new ItemStack(abilityItem));
            if (getDetails().getType() == AbilityType.SKILL && info.details().getComponents().length <= 0) {
                player.updateInventory();
            }
        }
        if (info.cooldown() > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(getDetails().getColor().toString() + ChatColor.ITALIC + getDetails().getName() + " is ready to use again.");
                }
            }, info.cooldown() * 20L);
            coolDowns.put(player.getName(), System.currentTimeMillis());
        }
        if (getDetails().getType() == AbilityType.SKILL) {
            for (ItemStack component : info.details().getComponents()) {
                InventoryTools.remove(player.getInventory(), component);
            }
            if (info.details().getComponents().length > 0) {
                player.updateInventory();
            }
        }
        player.sendMessage(ChatColor.GREEN + "Used " + getDetails().getColor() + getDetails().getName() + ChatColor.GREEN + "!");
    }

    protected abstract int getLevel();

    protected abstract boolean canUseAbility(Player player);

    protected abstract boolean onAbilityUse(Player player);
}
