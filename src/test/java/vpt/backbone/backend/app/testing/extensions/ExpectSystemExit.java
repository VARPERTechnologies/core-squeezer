package vpt.backbone.backend.app.testing.extensions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@ExtendWith(SystemExitExtension.class)
public @interface ExpectSystemExit {
   int value() default Integer.MAX_VALUE;
}