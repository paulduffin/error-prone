/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.junit.BugCheckerRule;
import com.google.errorprone.junit.CompilationTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** {@link AmbiguousMethodReference}Test */
@RunWith(JUnit4.class)
public class AmbiguousMethodReferenceTest {

  @Rule
  public BugCheckerRule rule = BugCheckerRule.forBugChecker(AmbiguousMethodReference.class);

  @CompilationTest(
      path = "A.java",
      lines = {
          "public class A {",
          "  interface B {}",
          "  interface C {}",
          "  interface D {}",
          "",
          "  // BUG: Diagnostic contains: c(A, D)",
          "  B c(D d) {",
          "    return null;",
          "  }",
          "  static B c(A a, D d) {",
          "    return null;",
          "  }",
          "}",
      })
  @Test
  public void positive() {
  }

  @CompilationTest(
      path = "A.java",
      lines = {
          "@SuppressWarnings(\"AmbiguousMethodReference\")",
          "public class A {",
          "  interface B {}",
          "  interface C {}",
          "  interface D {}",
          "",
          "  B c(D d) {",
          "    return null;",
          "  }",
          "  static B c(A a, D d) {",
          "    return null;",
          "  }",
          "}",
      })
  @Test
  public void suppressedAtClass() {
  }

  @CompilationTest(
      path = "A.java",
      lines = {
          "public class A {",
          "  interface B {}",
          "  interface C {}",
          "  interface D {}",
          "",
          "  @SuppressWarnings(\"AmbiguousMethodReference\")",
          "  B c(D d) {",
          "    return null;",
          "  }",
          "  // BUG: Diagnostic contains: c(D)",
          "  static B c(A a, D d) {",
          "    return null;",
          "  }",
          "}",
      })
  @Test
  public void suppressedAtMethod() {
  }

  @CompilationTest(
      path = "A.java",
      lines = {
          "public class A {",
          "  interface B {}",
          "  interface C {}",
          "  interface D {}",
          "",
          "  @SuppressWarnings(\"AmbiguousMethodReference\")",
          "  B c(D d) {",
          "    return null;",
          "  }",
          "  @SuppressWarnings(\"AmbiguousMethodReference\")",
          "  static B c(A a, D d) {",
          "    return null;",
          "  }",
          "}",
      })
  @Test
  public void suppressedAtBothMethods() {
  }

  @CompilationTest(
      path = "A.java",
      lines = {
          "public class A {",
          "  interface B {}",
          "  interface C {}",
          "  interface D {}",
          "",
          "  B c(D d) {",
          "    return null;",
          "  }",
          "  static B d(A a, D d) {",
          "    return null;",
          "  }",
          "}",
      })
  @Test
  public void negativeDifferentNames() {
  }

  @CompilationTest(
      path = "B.java",
      lines = {
          "public interface B<T> {",
          "  static <T> B<T> f() { return null; }",
          "}",
      })
  @CompilationTest(
      path = "A.java",
      lines = {
          "public abstract class A<T> implements B<T> {",
          "  public static <T> A<T> f() { return null; }",
          "}",
      })
  @Test
  public void negativeStatic() {
  }
}
