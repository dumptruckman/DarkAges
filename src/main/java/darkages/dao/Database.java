package darkages.dao;

import darkages.character.PlayerCharacter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Database {

    @Nullable
    PlayerCharacter getSelectedCharacter(String playerName);

    @NotNull
    PlayerCharacter createCharacter(String playerName);
}
