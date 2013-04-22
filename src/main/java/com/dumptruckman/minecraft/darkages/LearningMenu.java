package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import com.dumptruckman.minecraft.darkages.abilities.spells.SoulStone;

public class LearningMenu {

    private final DarkAgesPlugin plugin;

    public LearningMenu(final DarkAgesPlugin plugin) {
        this.plugin = plugin;
    }

    public SingleViewMenu buildMenu() {
        // Create the main menu
        final SingleViewMenu mainMenu = Menus.createSimpleInventoryMenu(plugin, "All Abilities", 9);

        // Create main menu "go back" button
        final MenuItem goBackToMainMenuMenuItem = SubMenuConfigurator.createGoBackMenuItem(mainMenu);

        // Create the Skill menu and add to main menu
        final SingleViewMenu skillsMenu = initializeSkillsMenu();
        skillsMenu.addItem(goBackToMainMenuMenuItem); // Add back button
        mainMenu.addItem(skillsMenu);

        // Create the Spell menu and add to main menu
        final SingleViewMenu spellsMenu = initializeSpellsMenu();
        spellsMenu.addItem(goBackToMainMenuMenuItem); // Add back button
        mainMenu.addItem(spellsMenu);

        return mainMenu;
    }

    private SingleViewMenu initializeSkillsMenu() {
        final SingleViewMenu skillsMenu = SubMenuConfigurator.createNewSubMenu(plugin, "All Skills", 9);

        // Add skills to menu

        return skillsMenu;
    }

    private SingleViewMenu initializeSpellsMenu() {
        final SingleViewMenu spellsMenu = SubMenuConfigurator.createNewSubMenu(plugin, "All Spells", 9);

        // Add skills to menu
        MenuItem soulStoneMenuItem = new SoulStone(plugin).createLearningMenuItem();
        plugin.soulStoneItem = soulStoneMenuItem.getItemStack(); // Store the soul stone itemstack for reference
        spellsMenu.addItem(soulStoneMenuItem);

        return spellsMenu;
    }
}
