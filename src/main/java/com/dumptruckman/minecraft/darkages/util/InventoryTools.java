/* Copyright (C) dumptruckman 2012
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.minecraft.darkages.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a handy utility class used for manipulating Bukkit's Inventory objects in a finer grained
 * manner than typically available in the Bukkit API.
 */
public class InventoryTools {

    private InventoryTools() {
        throw new AssertionError();
    }

    /**
     * Retrieves a Map of all ItemStacks with the key relating to the slot position
     * in the given inventory and the value being the ItemStack at that position.  This is similar
     * to the {@link Inventory#all(org.bukkit.Material)} with the addition of only populating the Map
     * with ItemStacks where {@link ItemStack#isSimilar(org.bukkit.inventory.ItemStack)} is true with the given item.
     *
     * @param inventory The Inventory to search for items in.
     * @param itemStack The item to look for similars of.
     * @return A Map of all ItemStacks meeting the above criteria, where the key is the slot index for the inventory
     * and the value is the ItemStacks found.
     */
    public static Map<Integer, ItemStack> all(final Inventory inventory, final ItemStack itemStack) {
        final Map<Integer, ? extends ItemStack> allItems = inventory.all(itemStack.getType());
        final Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>(allItems.size());
        for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
            if (item.getValue().isSimilar(itemStack)) {
                items.put(item.getKey(), item.getValue());
            }
        }
        return items;
    }

    public static boolean contains(final Inventory inventory, final ItemStack itemStack) {
        final HashMap<Integer, ? extends ItemStack> allItems = inventory.all(itemStack.getType());
        int foundAmount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
            if (item.getValue().isSimilar(itemStack)) {
                if (item.getValue().getAmount() >= itemStack.getAmount() - foundAmount) {
                    foundAmount = itemStack.getAmount();
                } else {
                    foundAmount += item.getValue().getAmount();
                }
                if (foundAmount >= itemStack.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean remove(final Inventory inventory, final ItemStack itemStack) {
        final HashMap<Integer, ? extends ItemStack> allItems = inventory.all(itemStack.getType());
        final HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>(allItems.size());
        int foundAmount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
            if (item.getValue().isSimilar(itemStack)) {
                if (item.getValue().getAmount() >= itemStack.getAmount() - foundAmount) {
                    removeFrom.put(item.getKey(), itemStack.getAmount() - foundAmount);
                    foundAmount = itemStack.getAmount();
                } else {
                    foundAmount += item.getValue().getAmount();
                    removeFrom.put(item.getKey(), item.getValue().getAmount());
                }
                if (foundAmount >= itemStack.getAmount()) {
                    break;
                }
            }
        }
        if (foundAmount == itemStack.getAmount()) {
            for (Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                ItemStack item = inventory.getItem(toRemove.getKey());
                if (item.getAmount() - toRemove.getValue() <= 0) {
                    inventory.clear(toRemove.getKey());
                } else {
                    item.setAmount(item.getAmount() - toRemove.getValue());
                    inventory.setItem(toRemove.getKey(), item);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Splits an arbitrarily large amount of items into a Collection of ItemStacks where the amount of each ItemStack
     * is no more than maximum stack size for the type of material of the item passed in.
     *
     * @param item The items to split.
     * @return A collection of ItemStacks where the amount of the item passed in has been separated so that each
     * ItemStack in the collection has an amount no greater than the maximum amount for the item's material type.
     */
    public static Collection<ItemStack> splitItemsByMaxStackSize(ItemStack item) {
        final int numStacks = item.getAmount() / item.getType().getMaxStackSize();
        final int remainingAmount = item.getAmount() % item.getType().getMaxStackSize();
        final Collection<ItemStack> items = new ArrayList<ItemStack>(numStacks + (remainingAmount != 0 ? 1 : 0));
        for (int i = 0; i < numStacks; i++) {
            items.add(new ItemStack(item.getType(), item.getType().getMaxStackSize(), item.getDurability()));
        }
        if (remainingAmount > 0) {
            items.add(new ItemStack(item.getType(), remainingAmount, item.getDurability()));
        }
        return items;
    }
}
