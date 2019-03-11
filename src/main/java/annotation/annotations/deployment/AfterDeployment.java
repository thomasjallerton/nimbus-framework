package annotation.annotations.deployment;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AfterDeployments.class)
public @interface AfterDeployment {
    boolean isTest() default false;
    String[] stages() default {NimbusConstants.stage};
}
