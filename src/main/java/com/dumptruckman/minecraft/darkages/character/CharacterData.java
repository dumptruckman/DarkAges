package com.dumptruckman.minecraft.darkages.character;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CharacterData implements ConfigurationSerializable {

    private int strength = 3, dexterity = 3, constitution = 3, intelligence = 3, wisdom = 3;
    private int level = 1, exp = 0;
    private int health = 50, maxHealth = 50;
    private int mana = 50, maxMana = 50;

    public CharacterData() { }

    public CharacterData(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
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

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getConstitution() {
        return constitution;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
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

    public void setStrength(final int strength) {
        this.strength = strength;
    }

    public void setDexterity(final int dexterity) {
        this.dexterity = dexterity;
    }

    public void setConstitution(final int constitution) {
        this.constitution = constitution;
    }

    public void setIntelligence(final int intelligence) {
        this.intelligence = intelligence;
    }

    public void setWisdom(final int wisdom) {
        this.wisdom = wisdom;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public void setExp(final int exp) {
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
}
