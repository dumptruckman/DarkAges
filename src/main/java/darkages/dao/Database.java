package darkages.dao;

import darkages.character.PlayerCharacter;
import org.bukkit.entity.Player;

public interface Database {

    long getPlayerId(Player player);

    PlayerCharacter getSelectedCharacter(long playerId);
}
