package darkages.character;

import org.jetbrains.annotations.NotNull;

public class PlayerCharacter {

    private long id;
    private CharacterStats stats;

    public PlayerCharacter(long id, @NotNull CharacterStats stats) {
        this.id = id;
        this.stats = stats;
    }

    public long getId() {
        return id;
    }

    public CharacterStats getStats() {
        return stats;
    }
}
