package darkages.menu;

import com.dumptruckman.minecraft.actionmenu.MenuItem;
import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import darkages.DarkAgesPlugin;
import darkages.ability.Ability;
import darkages.ability.AbilityType;

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
        final SingleViewMenu abilitiesMenu = initializeAbilitiesMenu();
        abilitiesMenu.addItem(goBackToMainMenuMenuItem); // Add back button
        mainMenu.addItem(abilitiesMenu);

        return mainMenu;
    }

    private SingleViewMenu initializeAbilitiesMenu() {
        final SingleViewMenu abilitiesMenu = SubMenuConfigurator.createNewSubMenu(plugin, "All Abilities", 9);

        // Create main menu "go back" button
        final MenuItem goBackToMainMenuMenuItem = SubMenuConfigurator.createGoBackMenuItem(abilitiesMenu);

        for (Ability ability : Ability.ABILITY_ITEMS.values()) {
            if (ability.getDetails().getType() == AbilityType.SPECIAL) {
                abilitiesMenu.addItem(ability.createAbilityMenuItem());
            }
        }

        // Create the Skill menu and add to main menu
        final SingleViewMenu skillsMenu = initializeSkillsMenu();
        skillsMenu.addItem(goBackToMainMenuMenuItem); // Add back button
        abilitiesMenu.addItem(skillsMenu);

        // Create the Spell menu and add to main menu
        final SingleViewMenu spellsMenu = initializeSpellsMenu();
        spellsMenu.addItem(goBackToMainMenuMenuItem); // Add back button
        abilitiesMenu.addItem(spellsMenu);

        return abilitiesMenu;
    }

    private SingleViewMenu initializeSkillsMenu() {
        final SingleViewMenu skillsMenu = SubMenuConfigurator.createNewSubMenu(plugin, "All Skills", 18);

        for (Ability ability : Ability.ABILITY_ITEMS.values()) {
            if (ability.getDetails().getType() == AbilityType.SKILL) {
                skillsMenu.addItem(ability.createLearningMenuItem());
            }
        }

        return skillsMenu;
    }

    private SingleViewMenu initializeSpellsMenu() {
        final SingleViewMenu spellsMenu = SubMenuConfigurator.createNewSubMenu(plugin, "All Spells", 18);

        for (Ability ability : Ability.ABILITY_ITEMS.values()) {
            if (ability.getDetails().getType() == AbilityType.SPELL) {
                spellsMenu.addItem(ability.createLearningMenuItem());
            }
        }

        return spellsMenu;
    }
}
