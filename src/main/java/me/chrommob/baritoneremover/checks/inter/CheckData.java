package me.chrommob.baritoneremover.checks.inter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckData {
    boolean hidden() default false;
    String name() default "Unknown";
    String identifier() default "Unknown";

    String description() default "Unknown";

    CheckType checkType() default CheckType.NONE;
}
