package darkages.character;

import org.jetbrains.annotations.NotNull;

public class PlayerCharacter {

    private long playerId;
    private long characterId;
    private CharacterStats stats;

    public PlayerCharacter(long playerId, @NotNull CharacterStats stats) {
        this.playerId = playerId;
        this.characterId = stats.getCharacterId();
        this.stats = stats;
    }

    public long getPlayerId() {
        return playerId;
    }

    public long getCharacterId() {
        return characterId;
    }

    public CharacterStats getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return "PlayerCharacter{" +
                "playerId=" + playerId +
                ", characterId=" + characterId +
                ", stats=" + stats +
                '}';
    }
}
