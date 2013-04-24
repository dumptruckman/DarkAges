package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.darkages.util.InventoryTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Ability {

    private static final Material LEARNING_MATERIAL = Material.PAPER;
    public static final String SPELL_TAG = ChatColor.MAGIC.toString() + ChatColor.BOLD;
    public static final String LEARN_STRING = ChatColor.GOLD + "Teaches: ";
    public static final String ABILITY_DESCRIPTION = ChatColor.GREEN + "Ability description: ";
    public static final String PREPARATION_COMPONENTS = ChatColor.YELLOW + "Preparation components: ";
    public static final String USAGE_COMPONENTS = ChatColor.GOLD + "Usage components: ";
    public static final String CASTTIME = ChatColor.WHITE + "Cast time: ";
    public static final String COOLDOWN = ChatColor.WHITE + "Cooldown: ";
    public static final String REQUIRES_LEVELS = ChatColor.AQUA + "Level cost: ";
    public static final String REQUIRED_SKILLS = ChatColor.DARK_PURPLE + "Must know: ";

    public static final Map<ItemStack, Ability> LEARNING_ITEMS = new HashMap<ItemStack, Ability>(10);
    public static final Map<ItemStack, Ability> ABILITY_ITEMS = new HashMap<ItemStack, Ability>(10);

    @NotNull
    protected final DarkAgesPlugin plugin;
    @NotNull
    protected final AbilityInfo abilityInfo;
    @NotNull
    protected final ItemStack learningItem;
    @NotNull
    protected final ItemStack abilityItem;
    @NotNull
    protected final Set<ItemStack> actualPreparationComponents;
    @NotNull
    protected final Set<ItemStack> actualUsageComponents;
    @NotNull
    protected final Map<String, Long> coolDowns = new HashMap<String, Long>(Bukkit.getMaxPlayers() * 2);
    @NotNull
    private final String abilityTag;

    protected Ability(@NotNull final DarkAgesPlugin plugin) {
        this.plugin = plugin;
        final AbilityInfo abilityInfo = getClass().getAnnotation(AbilityInfo.class);
        if (abilityInfo == null) {
            throw new IllegalStateException("Ability must be annotated with AbilityInfo");
        }
        this.abilityInfo = abilityInfo;

        this.actualPreparationComponents = new HashSet<ItemStack>(abilityInfo.prepareComponents().length);
        for (Material component : abilityInfo.prepareComponents()) {
            ItemStack item = null;
            for (ItemStack i : actualPreparationComponents) {
                if (i.getType() == component) {
                    item = i;
                    break;
                }
            }
            if (item == null) {
                this.actualPreparationComponents.add(new ItemStack(component));
            } else {
                item.setAmount(item.getAmount());
            }
        }

        this.actualUsageComponents = new HashSet<ItemStack>(abilityInfo.usageComponents().length);
        for (Material component : abilityInfo.usageComponents()) {
            ItemStack item = null;
            for (ItemStack i : actualUsageComponents) {
                if (i.getType() == component) {
                    item = i;
                    break;
                }
            }
            if (item == null) {
                this.actualUsageComponents.add(new ItemStack(component));
            } else {
                item.setAmount(item.getAmount());
            }
        }

        abilityTag = abilityInfo.magicColor() + SPELL_TAG + abilityInfo.name();

        this.learningItem = initializeLearningItemStack();
        this.abilityItem = initializeAbilityItemStack();
        LEARNING_ITEMS.put(learningItem, this);
        ABILITY_ITEMS.put(abilityItem, this);
    }

    /*****************
     * LEARNING ITEM *
     *****************/
    private ItemStack initializeLearningItemStack() {
        final ItemStack item = new ItemStack(LEARNING_MATERIAL);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Learn " + abilityInfo.name());
        List<String> lore = new ArrayList<String>(20);
        lore.add(getAbilityTag()); // This has to be the first lore!
        if (CustomEnchantment.okayToEnchant) {
            meta.addEnchant(abilityInfo.type().getEnchantment(), getLevel(), true);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            switch (abilityInfo.type()) {
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
        lore.add(LEARN_STRING + abilityInfo.name());
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = abilityInfo.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        lore.add(REQUIRES_LEVELS + abilityInfo.levelCost());
        if (abilityInfo.requirements().length > 0) {
            lore.add(REQUIRED_SKILLS);
            for (Class<? extends Ability> abilityClass : abilityInfo.requirements()) {
                AbilityInfo requirementInfo = abilityClass.getAnnotation(AbilityInfo.class);
                if (requirementInfo != null) {
                    lore.add(ChatColor.DARK_PURPLE + "  " + requirementInfo.name());
                }
            }
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to learn.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /****************
     * ABILITY ITEM *
     ****************/
    private ItemStack initializeAbilityItemStack() {
        final ItemStack item = new ItemStack(abilityInfo.material());
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(abilityInfo.magicColor() + abilityInfo.name());
        List<String> lore = new ArrayList<String>(20);
        lore.add(getAbilityTag()); // This has to be the first lore!
        if (CustomEnchantment.okayToEnchant) {
            meta.addEnchant(abilityInfo.type().getEnchantment(), getLevel(), true);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            switch (abilityInfo.type()) {
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
        String[] desc = abilityInfo.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        if (abilityInfo.prepareComponents().length > 0) {
            lore.add(PREPARATION_COMPONENTS);
            for (ItemStack component : actualPreparationComponents) {
                lore.add(ChatColor.YELLOW + "  " + component.getType().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getAmount());
            }
        }
        if (abilityInfo.usageComponents().length > 0) {
            lore.add(USAGE_COMPONENTS);
            for (ItemStack component : actualUsageComponents) {
                lore.add(ChatColor.GOLD + "  " + component.getType().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getAmount());
            }
        }
        if (abilityInfo.castTime() > 0) {
            lore.add(CASTTIME + abilityInfo.castTime() + " second" + (abilityInfo.castTime() > 1 ? "s" : ""));
        }
        if (abilityInfo.cooldown() > 0) {
            lore.add(COOLDOWN + abilityInfo.cooldown() + " second" + (abilityInfo.cooldown() > 1 ? "s" : ""));
        }
        if (abilityInfo.retainOnDeath()) {
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Remains on death");
        }
        if (abilityInfo.destroyedOnDeath()) {
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Destroyed on death");
        }
        if (!abilityInfo.allowDrop()) {
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
    public String getAbilityTag() {
        return abilityTag;
    }

    public MenuItem createAbilityMenuItem() {
        return new MenuItem("Click to obtain usage item").setItemStack(abilityItem).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player == null) {
                    return;
                }
                if  (abilityInfo.inventoryLimit() > 0) {
                    Map<Integer, ? extends ItemStack> closeMatches = player.getInventory().all(abilityInfo.material());
                    int currentAbilityItemCount = 0;
                    for (ItemStack item : closeMatches.values()) {
                        if (item != null && item.isSimilar(abilityItem)) {
                            currentAbilityItemCount += item.getAmount();
                        }
                    }
                    if (currentAbilityItemCount >= abilityInfo.inventoryLimit()) {
                        player.sendMessage(ChatColor.RED + "You may not have more than " + abilityInfo.inventoryLimit() + " of those.");
                        return;
                    }
                }
                for (ItemStack component : actualPreparationComponents) {
                    if (!InventoryTools.contains(player.getInventory(), component)) {
                        player.sendMessage(ChatColor.RED + "You do not have the required components to prepare this ability!");
                        return;
                    }
                }
                if (!player.getInventory().addItem(new ItemStack(abilityItem)).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You do not have room in your inventory to prepare this ability!");
                }
                for (ItemStack component : actualPreparationComponents) {
                    InventoryTools.remove(player.getInventory(), component);
                }
            }
        });
    }

    @Override
    public int hashCode() {
        return abilityInfo.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Ability && ((Ability) obj).abilityInfo.equals(abilityInfo);
    }

    public boolean isDestroyedOnDeath() {
        return abilityInfo.destroyedOnDeath();
    }

    public boolean isRetainedOnDeath() {
        return abilityInfo.retainOnDeath();
    }

    public boolean isAllowedToDrop() {
        return abilityInfo.allowDrop();
    }

    public void learnAbility(final Player player) {
        if (player.hasPermission(abilityInfo.permission())) {
            player.sendMessage(ChatColor.RED + "You already possess this knowledge!");
            return;
        }
        if (player.getLevel() < abilityInfo.levelCost()) {
            player.sendMessage(ChatColor.RED + "You do not possess enough experience to learn this!");
            return;
        }
        for (Class<? extends Ability> abilityClass : abilityInfo.requirements()) {
            AbilityInfo requirementInfo = abilityClass.getAnnotation(AbilityInfo.class);
            if (requirementInfo != null) {
                if (!player.hasPermission(requirementInfo.permission())) {
                    player.sendMessage(ChatColor.RED + "You lack knowledge of the required ability: " + requirementInfo.magicColor() + requirementInfo.name());
                    return;
                }
            }
        }
        plugin.getPermission().playerAdd(player, abilityInfo.permission());
        player.giveExpLevels(-abilityInfo.levelCost());
        ItemStack item = player.getItemInHand();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.updateInventory();
        } else {
            player.setItemInHand(null);
        }
        player.sendMessage(ChatColor.GREEN + "You now posses the knowledge of " + abilityInfo.magicColor() + abilityInfo.name() + ChatColor.GREEN + "!");
    }

    public void useAbility(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        if (!player.hasPermission(abilityInfo.permission())) {
            player.sendMessage(ChatColor.RED + "You do not have the knowledge required to use this ability!");
            return;
        }
        if (abilityInfo.cooldown() > 0 && coolDowns.containsKey(playerName)) {
            if (System.currentTimeMillis() - coolDowns.get(playerName) <= abilityInfo.cooldown() * 1000) {
                player.sendMessage(ChatColor.RED + "That ability is on cooldown!");
                return;
            }
        }
        for (ItemStack component : actualUsageComponents) {
            if (!InventoryTools.contains(player.getInventory(), component)) {
                player.sendMessage(ChatColor.RED + "You do not have the required components to use this ability!");
                return;
            }
        }
        if (canUseAbility(player)) {
            if (abilityInfo.castTime() <= 0) {
                if (onAbilityUse(player)) {
                    abilityUsed(player);
                }
            } else {
                CastingTask task = new CastingTask(plugin, player, this);
                try {
                    task.runTaskLater(plugin, abilityInfo.castTime() * 20L);
                } catch (IllegalStateException e) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Something went wrong!!!");
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, abilityInfo.castTime() * 20, 1, true));
                plugin.playersCasting.put(player.getName(), task);
            }
        }
    }

    public static class CastingTask extends BukkitRunnable {
        private final DarkAgesPlugin plugin;
        public final Player player;
        public final Ability ability;
        public final boolean alreadyConfused;

        private CastingTask(final DarkAgesPlugin plugin, final Player player, final Ability ability) {
            this.plugin = plugin;
            this.player = player;
            this.ability = ability;
            this.alreadyConfused = player.hasPotionEffect(PotionEffectType.CONFUSION);
        }

        @Override
        public void run() {
            if (!alreadyConfused) {
                player.removePotionEffect(PotionEffectType.CONFUSION);
            }
            ability.onAbilityUse(player);
        }
    }

    private void abilityUsed(final Player player) {
        if (abilityInfo.consumesAbilityItem()) {
            InventoryTools.remove(player.getInventory(), new ItemStack(abilityItem));
            if (abilityInfo.usageComponents().length <= 0) {
                player.updateInventory();
            }
        }
        if (abilityInfo.cooldown() > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(abilityInfo.magicColor().toString() + ChatColor.ITALIC + abilityInfo.name() + " is ready to use again.");
                }
            }, abilityInfo.cooldown() * 20L);
            coolDowns.put(player.getName(), System.currentTimeMillis());
        }
        for (ItemStack component : actualUsageComponents) {
            InventoryTools.remove(player.getInventory(), component);
        }
        if (abilityInfo.usageComponents().length > 0) {
            player.updateInventory();
        }
        player.sendMessage(ChatColor.GREEN + "Used " + abilityInfo.magicColor() + abilityInfo.name() + ChatColor.GREEN + "!");
    }

    protected abstract int getLevel();

    protected abstract boolean canUseAbility(Player player);

    protected abstract boolean onAbilityUse(Player player);
}
