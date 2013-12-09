package com.activeandroid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andrewgrosner
 * Date: 12/9/13
 * Contributors:
 * Description: tells ActiveAndroid that we want to ignore this class as a table
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
