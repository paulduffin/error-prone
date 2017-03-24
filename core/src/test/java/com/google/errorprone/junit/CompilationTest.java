package com.google.errorprone.junit;

import com.google.errorprone.bugpatterns.BugChecker;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated test method must check the behavior of the {@link BugChecker}
 * specified on the associated {@link BugCheckerRule}.
 *
 * @see BugCheckerRule
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompilationTests.class)
public @interface CompilationTest {

  /**
   * The virtual path to the code specified in the {@link #lines()}, this must be specified iff
   * the {@link #lines()} is specified.
   */
  String path() default "";

  /**
   * The input against which the compiler will run the associated {@link BugChecker}, if this is
   * not specified then it will default to loading the code from a resource.
   */
  String[] lines() default {};
}
