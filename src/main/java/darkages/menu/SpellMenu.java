package darkages.menu;

import com.dumptruckman.minecraft.actionmenu.prefab.Menus;
import com.dumptruckman.minecraft.actionmenu.prefab.SingleViewMenu;
import darkages.DarkAgesPlugin;
import darkages.ability.Ability;
import darkages.ability.AbilityType;
import org.bukkit.entity.Player;

public class SpellMenu {

    private final DarkAgesPlugin plugin;
    private final Player player;

    public SpellMenu(final DarkAgesPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public SingleViewMenu buildMenu() {
        // Create the main menu
        final SingleViewMenu mainMenu = Menus.createSimpleInventoryMenu(plugin, player.getName() + "'s spells", 18);

        for (Ability ability : Ability.ABILITY_ITEMS.values()) {
            if (ability.getDetails().getType() == AbilityType.SPELL && player.hasPermission(ability.getDetails().getPermission())) {
                mainMenu.addItem(ability.createAbilityMenuItem());
            }
        }

        return mainMenu;
    }
}
