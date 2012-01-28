package siri;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SiriObject {
    boolean hasSetMethods() default false;
}
