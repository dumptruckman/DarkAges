package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.abilities.AbilityType;
import org.bukkit.entity.Player;

public class SkillMenu {

    private final DarkAgesPlugin plugin;
    private final Player player;

    public SkillMenu(final DarkAgesPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public SingleViewMenu buildMenu() {
        // Create the main menu
        final SingleViewMenu mainMenu = Menus.createSimpleInventoryMenu(plugin, player.getName() + "'s skills", 18);

        for (Ability ability : Ability.ABILITY_ITEMS.values()) {
            if (ability.abilityInfo.type() == AbilityType.SKILL && player.hasPermission(ability.abilityInfo.permission())) {
                mainMenu.addItem(ability.createAbilityMenuItem());
            }
        }

        return mainMenu;
    }
}
