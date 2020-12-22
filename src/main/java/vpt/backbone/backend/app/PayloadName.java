package vpt.backbone.backend.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
//FIXME: This annotation could lead to SQL Injection
//Should use hibernate entity instead
//Only done for preview purposes
@Target(ElementType.TYPE)
public @interface PayloadName
{   
   String value();
}
