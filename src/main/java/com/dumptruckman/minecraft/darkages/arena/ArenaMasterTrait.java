package com.dumptruckman.minecraft.darkages.arena;

import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import com.dumptruckman.minecraft.darkages.menu.ArenaMenu;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class ArenaMasterTrait extends Trait implements CommandConfigurable {

    private final DarkAgesPlugin plugin;

    private String representsArena = "";

    public ArenaMasterTrait() {
        super("arenamaster");
        plugin = (DarkAgesPlugin) Bukkit.getServer().getPluginManager().getPlugin("DarkAges");
    }

    @Override
    public void load(final DataKey key) throws NPCLoadException {
        representsArena = key.getString("representsArena", "");
    }

    @Override
    public void save(final DataKey key) {
        key.setString("representsArena", representsArena);
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!representsArena.isEmpty()) {
            try {
                SingleViewMenu menu = new ArenaMenu(plugin, representsArena).buildMenu();
                menu.updateView(menu, event.getClicker());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void configure(final CommandContext commandContext) {
        if (commandContext.length() == 1) {
            representsArena = commandContext.getString(0);
        }
    }
}
