package siri;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SiriParameter {

    String defaultClass() default Consts.defaultClass;
    boolean required() default false;
}
