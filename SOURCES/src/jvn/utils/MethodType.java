/***
 * MethodType annotation: used to indicate the method type (write or read)
 * 
 */

package jvn.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodType {
	String type();
}
