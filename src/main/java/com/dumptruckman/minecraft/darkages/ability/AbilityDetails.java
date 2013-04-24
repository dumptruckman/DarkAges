package com.dumptruckman.minecraft.darkages.ability;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AbilityDetails {

    AMBUSH("Ambush", AbilityType.SKILL, ChatColor.WHITE, 5, new ItemStack(Material.ARROW), new ItemStack(Material.ARROW)),
    SOUL_STONE("Soul Stone", AbilityType.SPECIAL, ChatColor.DARK_GRAY, 0, new ItemStack(Material.GHAST_TEAR)),
    BEAG_IOC("beag ioc", AbilityType.SPELL, ChatColor.BLUE, 5, new ItemStack(Material.SPECKLED_MELON), new ItemStack(Material.INK_SACK, 1, (short) 15)),
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

    AbilityDetails(String name, AbilityType type, ChatColor color, int learnCost, ItemStack item, ItemStack... components) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.item = item;
        this.learnCost = learnCost;
        this.components = components;
        this.permission = "darkages.ability." + type.name().toLowerCase() + "." + name.toLowerCase().replaceAll("\\s", "");
        this.tag = color + ChatColor.MAGIC.toString() + ChatColor.BOLD + name;
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
}