package com.dumptruckman.minecraft.darkages.ability;

import org.bukkit.enchantments.Enchantment;

public enum AbilityType {

    SKILL(new CustomEnchantment(4664, "Skill")),
    SPELL(new CustomEnchantment(4665, "Spell")),
    SPECIAL(new CustomEnchantment(4666, "Special"));

    private final Enchantment enchantment;

    AbilityType(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }
}
