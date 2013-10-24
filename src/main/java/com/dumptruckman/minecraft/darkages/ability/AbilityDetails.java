package com.dumptruckman.minecraft.darkages.ability;

import com.dumptruckman.minecraft.darkages.character.CharacterClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AbilityDetails {

    AMBUSH(CharacterClass.MONK, false, "Ambush", AbilityType.SKILL, ChatColor.WHITE, 5, new ItemStack(Material.ARROW), new ItemStack(Material.ARROW)),
    SHADOW_FIGURE(CharacterClass.ROGUE, true, "Shadow Figure", AbilityType.SKILL, ChatColor.WHITE, 35, new ItemStack(Material.ARROW), new ItemStack(Material.ARROW)),
    SOUL_STONE("Soul Stone", AbilityType.SPECIAL, ChatColor.DARK_GRAY, 0, new ItemStack(Material.GHAST_TEAR)),
    BUTTERFLY_WING("Butterfly Wing", AbilityType.SPECIAL, ChatColor.AQUA, 0, new ItemStack(Material.FEATHER)),
    SPAWN_TELEPORT("Spawn Teleport", AbilityType.SPECIAL, ChatColor.WHITE, 0, new ItemStack(Material.COMPASS)),
    TOWN_TELEPORT("Town Teleport", AbilityType.SPECIAL, ChatColor.GRAY, 0, new ItemStack(Material.COMPASS)),
    BEAG_IOC(CharacterClass.PRIEST, false, "beag ioc", AbilityType.SPELL, ChatColor.BLUE, 5, new ItemStack(Material.SPECKLED_MELON), new ItemStack(Material.INK_SACK, 1, (short) 15)),
    DACHAIDH("dachaidh", AbilityType.SPELL, ChatColor.GRAY, 7, new ItemStack(Material.COMPASS), new ItemStack(Material.FEATHER, 5)),
    ;

    private final String name;
    private final AbilityType type;
    private final ItemStack item;
    private final ItemStack[] components;
    private final String permission;
    private final ChatColor color;
    private final String tag;
    private final int learnCost;
    private final CharacterClass requiredClass;
    private final boolean pureOnly;

    AbilityDetails(String name, AbilityType type, ChatColor color, int learnCost, ItemStack item, ItemStack... components) {
        this(CharacterClass.NO_CLASS, false, name, type, color, learnCost, item, components);
    }

    AbilityDetails(CharacterClass requiredClass, boolean pureOnly, String name, AbilityType type, ChatColor color, int learnCost, ItemStack item, ItemStack... components) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.item = item;
        this.learnCost = learnCost;
        this.components = components;
        this.permission = "darkages.ability." + type.name().toLowerCase() + "." + name.toLowerCase().replaceAll("\\s", "");
        this.tag = color + ChatColor.MAGIC.toString() + ChatColor.BOLD + name;
        this.requiredClass = requiredClass;
        this.pureOnly = pureOnly;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getTag() {
        return tag;
    }

    public AbilityType getType() {
        return type;
    }

    public int getLearnCost() {
        return learnCost;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack[] getComponents() {
        return components;
    }

    public boolean isAbleToUse(CharacterClass characterClass) {
        if (requiredClass == CharacterClass.NO_CLASS) {
            return true;
        } else if (characterClass == CharacterClass.GAME_MASTER) {
            return true;
        } else if (requiredClass == characterClass) {
            return true;
        }
        return false;
    }

    public boolean isPureOnly() {
        return pureOnly;
    }
}
