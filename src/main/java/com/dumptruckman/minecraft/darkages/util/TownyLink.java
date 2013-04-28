package com.dumptruckman.minecraft.darkages.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TownyLink {

    @Nullable
    public Location getTownSpawn(Player player) {
        try {
            com.palmergames.bukkit.towny.object.Resident r = com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().getResident(player.getName());
            if (r != null) {
                com.palmergames.bukkit.towny.object.Town t = r.getTown();
                if (t != null) {
                    return t.getSpawn();
                }
            }
        } catch (com.palmergames.bukkit.towny.exceptions.NotRegisteredException ignore) {
        } catch (com.palmergames.bukkit.towny.exceptions.TownyException ignore) { }
        return null;
    }

    public boolean isOkayToBuildPortal(Block block) {
        return block.getType() == Material.AIR || com.palmergames.bukkit.towny.object.TownyUniverse.getTownBlock(block.getLocation()) == null;
    }
}
