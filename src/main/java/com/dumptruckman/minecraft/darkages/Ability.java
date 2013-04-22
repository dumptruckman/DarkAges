package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.Action;
import com.dumptruckman.minecraft.actionmenu.MenuItem;
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
    public static final String COMPONENTS = ChatColor.YELLOW + "Required components: ";

    public static final Map<ItemStack, Ability> LEARNING_ITEMS = new HashMap<ItemStack, Ability>(10);
    public static final Map<ItemStack, Ability> ABILITY_ITEMS = new HashMap<ItemStack, Ability>(10);

    @NotNull
    protected final AbilityInfo abilityInfo;
    @NotNull
    protected final ItemStack learningItem;
    @NotNull
    protected final ItemStack abilityItem;
    @NotNull
    protected final Map<Material, Integer> actualComponents;

    protected Ability() {
        final AbilityInfo abilityInfo = getClass().getAnnotation(AbilityInfo.class);
        if (abilityInfo == null) {
            throw new IllegalStateException("Ability must be annotated with AbilityInfo");
        }
        this.abilityInfo = abilityInfo;

        this.actualComponents = new HashMap<Material, Integer>(abilityInfo.components().length);
        for (Material component : abilityInfo.components()) {
            if (actualComponents.containsKey(component)) {
                int count = actualComponents.get(component);
                count++;
                actualComponents.put(component, count);
            } else {
                actualComponents.put(component, 1);
            }
        }

        this.learningItem = initializeLearningItemStack();
        this.abilityItem = initializeAbilityItemStack();
        LEARNING_ITEMS.put(learningItem, this);
        ABILITY_ITEMS.put(abilityItem, this);
    }

    private ItemStack initializeLearningItemStack() {
        final ItemStack item = new ItemStack(LEARNING_MATERIAL);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Learn " + abilityInfo.name());
        List<String> lore = new ArrayList<String>(10);
        lore.add(getSpellTag());
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
        lore.add(getSpellTag());
        lore.add(ABILITY_DESCRIPTION);
        String[] desc = abilityInfo.description().split("\n");
        for (String descString : desc) {
            lore.add(ChatColor.GREEN + "  " + descString);
        }
        if (abilityInfo.components().length > 0) {
            lore.add(COMPONENTS);
            for (Map.Entry<Material, Integer> component : actualComponents.entrySet()) {
                lore.add(ChatColor.YELLOW + "  " + component.getKey().name().toLowerCase().replaceAll("_", " ") + "  x" + component.getValue());
            }
        }
        lore.add("");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to use.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public MenuItem createLearningMenuItem() {
        if (!actualComponents.isEmpty()) {
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

    private String getSpellTag() {
        return abilityInfo.magicColor() + SPELL_TAG + abilityInfo.name();
    }

    public MenuItem createAbilityMenuItem() {
        return new MenuItem("Right click to obtain usage item").setItemStack(abilityItem).setAction(new Action() {
            @Override
            public void performAction(@Nullable final Player player) {
                if (player == null) {
                    return;
                }
                for (Map.Entry<Material, Integer> component : actualComponents.entrySet()) {
                    if (!player.getInventory().contains(component.getKey(), component.getValue())) {
                        player.sendMessage(ChatColor.RED + "You do not have the required components to prepare this spell!");
                        return;
                    }
                }
                if (!player.getInventory().addItem(new ItemStack(abilityItem)).isEmpty()) {
                    player.sendMessage(ChatColor.RED + "You do not have room in your inventory to prepare this spell!");
                }
                for (Map.Entry<Material, Integer> component : actualComponents.entrySet()) {
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

    public abstract void useAbility(PlayerInteractEvent event);
}
