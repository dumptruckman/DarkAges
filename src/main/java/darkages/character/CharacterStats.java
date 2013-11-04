package darkages.character;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CharacterStats implements ConfigurationSerializable {

    private long characterId;

    private byte level = 1;
    private long exp = 0;
    private int health = 50, maxHealth = 50;
    private int mana = 50, maxMana = 50;
    private short strength = 3, dexterity = 3, constitution = 3, intelligence = 3, wisdom = 3;

    public CharacterStats(long characterId) {
        this.characterId = characterId;
    }

    public CharacterStats(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                if (entry.getKey().equals(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    continue;
                }
                Field field = getClass().getDeclaredField(entry.getKey());
                boolean access = field.isAccessible();
                field.setAccessible(true);
                field.set(this, entry.getValue());
                field.setAccessible(access);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public long getCharacterId() {
        return characterId;
    }

    @Override
    public Map<String, Object> serialize() {
        Field[] fields = getClass().getDeclaredFields();
        Map<String, Object> result = new HashMap<String, Object>(fields.length);
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) == null) {
                boolean access = field.isAccessible();
                field.setAccessible(true);
                try {
                    result.put(field.getName(), field.get(this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                field.setAccessible(access);
            }
        }
        return result;
    }

    public short getStrength() {
        return strength;
    }

    public short getDexterity() {
        return dexterity;
    }

    public short getConstitution() {
        return constitution;
    }

    public short getIntelligence() {
        return intelligence;
    }

    public short getWisdom() {
        return wisdom;
    }

    public byte getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setStrength(short strength) {
        this.strength = strength;
    }

    public void setDexterity(short dexterity) {
        this.dexterity = dexterity;
    }

    public void setConstitution(short constitution) {
        this.constitution = constitution;
    }

    public void setIntelligence(short intelligence) {
        this.intelligence = intelligence;
    }

    public void setWisdom(short wisdom) {
        this.wisdom = wisdom;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public void setExp(final long exp) {
        this.exp = exp;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

    public void setMaxHealth(final int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public void setMaxMana(final int maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public String toString() {
        return "CharacterStats{" +
                "characterId=" + characterId +
                ", level=" + level +
                ", exp=" + exp +
                ", health=" + health +
                ", maxHealth=" + maxHealth +
                ", mana=" + mana +
                ", maxMana=" + maxMana +
                ", strength=" + strength +
                ", dexterity=" + dexterity +
                ", constitution=" + constitution +
                ", intelligence=" + intelligence +
                ", wisdom=" + wisdom +
                '}';
    }
}
