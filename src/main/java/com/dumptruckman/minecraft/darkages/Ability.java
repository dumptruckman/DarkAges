package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.darkages.util.InventoryTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    protected final Map<Material, Integer> actualPreparationComponents;
    @NotNull
    protected final Map<Material, Integer> actualUsageComponents;
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

        this.actualPreparationComponents = new HashMap<Material, Integer>(abilityInfo.prepareComponents().length);
        for (Material component : abilityInfo.prepareComponents()) {
            if (actualPreparationComponents.containsKey(component)) {
                int count = actualPreparationComponents.get(component);
                count++;
                actualPreparationComponents.put(component, count);
            } else {
                actualPreparationComponents.put(component, 1);
            }
        }

        this.actualUsageComponents = new HashMap<Material, Integer>(abilityInfo.usageComponents().length);
        for (Material component : abilityInfo.usageComponents()) {
            if (actualUsageComponents.containsKey(component)) {
                int count = actualUsageComponents.get(component);
                count++;
                actualUsageComponents.put(component, count);
            } else {
                actualUsageComponents.put(component, 1);
            }
        }

        this.learningItem = initializeLearningItemStack();
        this.abilityItem = initializeAbilityItemStack();
        LEARNING_ITEMS.put(learningItem, this);
        ABILITY_ITEMS.put(abilityItem, this);
        abilityTag = abilityInfo.magicColor() + SPELL_TAG + abilityInfo.name();
    }

    private ItemStack initializeLearningItemStack() {
        final ItemStack item = new ItemStack(LEARNING_MATERIAL);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Learn " + abilityInfo.name());
        List<String> lore = new ArrayList<String>(10);
        lore.add(getAbilityTag());
        lore.add(LEARN_STRING + abilityInfo.name());
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = abilityInfo.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to learn.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack initializeAbilityItemStack() {
        final ItemStack item = new ItemStack(abilityInfo.material());
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(abilityInfo.magicColor() + abilityInfo.name());
        List<String> lore = new ArrayList<String>(10);
        lore.add(getAbilityTag());
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = abilityInfo.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        if (abilityInfo.prepareComponents().length > 0) {
            lore.add(PREPARATION_COMPONENTS);
            for (Map.Entry<Material, Integer> component : actualPreparationComponents.entrySet()) {
                lore.add(ChatColor.YELLOW + "  " + component.getKey().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getValue());
            }
        }
        if (abilityInfo.usageComponents().length > 0) {
            lore.add(USAGE_COMPONENTS);
            for (Map.Entry<Material, Integer> component : actualUsageComponents.entrySet()) {
                lore.add(ChatColor.GOLD + "  " + component.getKey().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getValue());
            }
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to use.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public MenuItem createLearningMenuItem() {
        if (!actualPreparationComponents.isEmpty()) {
            return new MenuItem("Right click to obtain learning item").setItemStack(learningItem).setAction(new Action() {
                @Override
                public void performAction(@Nullable final Player player) {
                    if (player != null) {
                        player.getInventory().addItem(new ItemStack(learningItem));
                    }
                }
            });
        } else {
            return createAbilityMenuItem();
        }
    }

    public String getAbilityTag() {
        return abilityTag;
    }

    public MenuItem createAbilityMenuItem() {
        return new MenuItem("Right click to obtain usage item").setItemStack(abilityItem).setAction(new Action() {
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
                for (Map.Entry<Material, Integer> component : actualPreparationComponents.entrySet()) {
                    if (!player.getInventory().contains(component.getKey(), component.getValue())) {
                        player.sendMessage(ChatColor.RED + "You do not have the required components to prepare this spell!");
                        return;
                    }
                }
                if (!player.getInventory().addItem(new ItemStack(abilityItem)).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You do not have room in your inventory to prepare this spell!");
                }
                for (Map.Entry<Material, Integer> component : actualPreparationComponents.entrySet()) {
                    player.getInventory().remove(new ItemStack(component.getKey(), component.getValue()));
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

    public void useAbility(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        final String playerName = event.getPlayer().getName();
        if (!player.hasPermission(abilityInfo.permission())) {
            player.sendMessage(ChatColor.RED + "You do not have the knowledge required to use this ability!");
            return;
        }
        if (abilityInfo.coolDown() > 0 && coolDowns.containsKey(playerName)) {
            if (System.currentTimeMillis() - coolDowns.get(playerName) <= abilityInfo.coolDown() * 1000) {
                System.out.println(ChatColor.RED + "That ability is on cooldown!");
                return;
            }
        }
        if (canUseAbility(event)) {
            if (abilityInfo.castTime() <= 0) {
                if (onAbilityUse(event)) {
                    if (abilityInfo.consumesAbilityItem()) {
                        InventoryTools.remove(player.getInventory(), new ItemStack(abilityItem));
                    }
                    abilityUsed(playerName);
                }
            } else {
                if (abilityInfo.consumesAbilityItem()) {
                    InventoryTools.remove(player.getInventory(), new ItemStack(abilityItem));
                }
            }
        }
    }

    private void abilityUsed(final String playerName) {
        if (abilityInfo.coolDown() > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) {
                        player.sendMessage(abilityInfo.magicColor().toString() + ChatColor.ITALIC + abilityInfo.name() + " is ready to use again.");
                    }
                }
            }, abilityInfo.coolDown() * 20L);
        }
    }

    protected abstract boolean canUseAbility(PlayerInteractEvent event);

    protected abstract boolean onAbilityUse(PlayerInteractEvent event);
}
