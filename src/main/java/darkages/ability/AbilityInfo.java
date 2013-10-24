package darkages.ability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityInfo {

    AbilityDetails details();
    String description();
    int castTime() default 0;
    int cooldown() default 0;
    int range() default 0;
    AbilityDetails[] requirements() default {};
    int inventoryLimit() default 0;
    boolean consumesAbilityItem() default true;
    boolean destroyedOnDeath() default false;
    boolean retainOnDeath() default false;
    boolean allowDrop() default false;
    boolean requiresTarget() default false;
}
