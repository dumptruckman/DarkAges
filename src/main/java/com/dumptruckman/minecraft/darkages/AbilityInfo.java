package com.dumptruckman.minecraft.darkages;

import com.dumptruckman.minecraft.darkages.abilities.AbilityType;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityInfo {

    String name();
    ChatColor magicColor();
    AbilityType type();
    String description();
    String permission();
    Material material();
    int levelCost();
    Material[] prepareComponents() default {};
    Material[] usageComponents() default {};
    int castTime() default 0;
    int cooldown() default 0;
    Class<? extends Ability>[] requirements() default {};
    int inventoryLimit() default 0;
    boolean consumesAbilityItem() default true;
    boolean destroyedOnDeath() default false;
    boolean retainOnDeath() default false;
    boolean allowDrop() default false;
}
