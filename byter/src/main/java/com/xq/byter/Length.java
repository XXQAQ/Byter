package com.xq.byter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)@Retention(RetentionPolicy.RUNTIME)
public @interface Length {

    String lengthByField() default "";

    int length() default 0;

}
