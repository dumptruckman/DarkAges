package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.ability.Ability;
import com.dumptruckman.minecraft.darkages.ability.AbilityType;
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
            if (ability.getDetails().getType() == AbilityType.SKILL && player.hasPermission(ability.getDetails().getPermission())) {
                mainMenu.addItem(ability.createAbilityMenuItem());
            }
        }

        return mainMenu;
    }
}
