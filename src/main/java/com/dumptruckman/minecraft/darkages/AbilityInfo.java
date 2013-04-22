package com.dumptruckman.minecraft.darkages;

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
    String description();
    String permission();
    Material material();
    Material[] components() default {};
    long castTime() default 0L;
    long coolDown() default 0L;
    int expCost() default 0;
    Class<? extends Ability>[] requirements() default {};
}
