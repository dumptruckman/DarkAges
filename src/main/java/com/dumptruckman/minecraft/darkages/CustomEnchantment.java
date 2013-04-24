package com.dumptruckman.minecraft.darkages;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class CustomEnchantment extends Enchantment {

    public static boolean okayToEnchant = false;

    private final String name;

    public CustomEnchantment(final int id, final String name) {
        super(id);
        this.name = name;
        if (okayToEnchant) {
            Enchantment.registerEnchantment(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean conflictsWith(final Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(final ItemStack itemStack) {
        return true;
    }

}
