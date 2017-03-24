/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.errorprone.junit;

import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Rule to make it easier to test an Error Prone {@link BugChecker} implementation.
 *
 * <p>It is possible with this rule to create a test without having to add any boiler plate code
 * other than the rule. The test itself can be specified completely with annotations on an empty
 * method.
 *
 * <p>Add the following to a JUnit 4 test class in order to test a {@link BugChecker}
 * implementation. The implementation class is provided to the {@link #forBugChecker(Class)} method
 * so each test can only test one {@link BugChecker} but it can provide multiple test methods for
 * it.
 *
 * <pre>
 * {@literal @Rule}
 * {@code public BugCheckerRule rule = BugCheckerRule.forBugChecker(MyBugChecker.class);}
 * </pre>
 *
 * <p>In order to TODO.
 */
@ErrorProneTest // Default annotation used when not specified on the test class.
public class BugCheckerRule implements TestRule {

  private final Class<? extends BugChecker> checkerClass;

  public static BugCheckerRule forBugChecker(Class<? extends BugChecker> checkerClass) {
    return new BugCheckerRule(checkerClass);
  }

  private BugCheckerRule(Class<? extends BugChecker> checkerClass) {
    this.checkerClass = checkerClass;
  }

  /**
   * Get the {@link ErrorProneTest} annotation from the test class, or a default one if the test
   * class is not annotated with it.
   *
   * @param testClass the test class.
   * @return the {@link ErrorProneTest} annotation
   */
  private ErrorProneTest getErrorProneTestAnnotation(Class<?> testClass) {
    ErrorProneTest errorProneTest = testClass.getAnnotation(ErrorProneTest.class);
    if (errorProneTest == null) {
      errorProneTest = getClass().getAnnotation(ErrorProneTest.class);
    }
    return errorProneTest;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // TODO Move out of this anonymous function.
        Class<?> testClass = description.getTestClass();

        ErrorProneTest errorProneTest = getErrorProneTestAnnotation(testClass);

        PatternExpander patternExpander = new PatternExpander(errorProneTest.resourcePattern());
        String methodName = description.getMethodName();

        String checkerName = checkerClass.getSimpleName();
        String inputPath = patternExpander.expand(checkerName, methodName);

        CompilationTest[] compilationTests = getCompilationTests(description);
        if (compilationTests != null) {
          CompilationTestHelper compilationHelper =
              CompilationTestHelper.newInstance(checkerClass, testClass);

          for (CompilationTest compilationTest : compilationTests) {
            Source source;
            String[] lines = compilationTest.lines();
            String path = compilationTest.path();

            // If one but not both of path and lines has been specified then it is an error.
            if ((lines.length == 0) != path.isEmpty()) {
              throw new IllegalStateException("Must specify both path() and lines()");
            } else if (lines.length == 0) {
              source = new Resource(inputPath);
            } else {
              source = new Lines(path, lines);
            }

            source.prepareCompilationHelper(compilationHelper);
          }
          base.evaluate();
          compilationHelper.doTest();
        } else {
          base.evaluate();
        }
      }
    };
  }

  private CompilationTest[] getCompilationTests(Description description) {
    CompilationTest compilationTest = description.getAnnotation(CompilationTest.class);
    if (compilationTest != null) {
      return new CompilationTest[]{compilationTest};
    }

    CompilationTests compilationTests = description.getAnnotation(CompilationTests.class);
    if (compilationTests != null) {
      return compilationTests.value();
    }

    return null;
  }

  private static abstract class Source {
    abstract void prepareCompilationHelper(CompilationTestHelper compilationHelper);
  }

  private static class Resource extends Source {
    private final String path;

    private Resource(String path) {
      this.path = path;
    }

    @Override
    void prepareCompilationHelper(CompilationTestHelper compilationHelper) {
      compilationHelper.addSourceFile(path);
    }
  }

  private static class Lines extends Source {
    private final String path;
    private final String[] array;

    private Lines(String path, String[] array) {
      this.path = path;
      this.array = array;
    }

    @Override
    void prepareCompilationHelper(CompilationTestHelper compilationHelper) {
      compilationHelper.addSourceLines(path, array);
    }
  }
}
