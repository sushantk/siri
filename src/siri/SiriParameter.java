package siri;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SiriParameter {

    String defaultClass() default Consts.defaultParamsClass;
    boolean required() default Consts.defaultRequired;
    boolean list() default Consts.defaultList; // if true, this method expects list of parameters as a pure "List" rather than IParams 
}
