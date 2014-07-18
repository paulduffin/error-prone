/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.errorprone;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.io.CharSource;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Utility class for tests which need to assert on the diagnostics produced during compilation.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class DiagnosticTestHelper {

  // When testing a single error-prone check, the name of the check. Used to validate diagnostics.
  // Null if not testing a single error-prone check.
  private final String checkName;

  /**
   * Construct a {@link DiagnosticTestHelper} not associated with a specific check.
   */
  public DiagnosticTestHelper() {
    this(null);
  }

  /**
   * Construct a {@link DiagnosticTestHelper} for a check with the given name.
   */
  public DiagnosticTestHelper(String checkName) {
    this.checkName = checkName;
  }

  public DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();

  public static Matcher<Diagnostic<JavaFileObject>> suggestsRemovalOfLine(URI fileURI, int line) {
    return allOf(
        diagnosticOnLine(fileURI, line),
        diagnosticMessage(containsString("remove this line")));
  }

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return collector.getDiagnostics();
  }

  public String describe() {
    StringBuilder stringBuilder = new StringBuilder().append("Diagnostics:\n");
    for (Diagnostic<? extends JavaFileObject> diagnostic : getDiagnostics()) {
      stringBuilder.append("  [")
          .append(diagnostic.getLineNumber()).append(":")
          .append(diagnostic.getColumnNumber())
          .append("]\t");
      stringBuilder.append(diagnostic.getMessage(Locale.getDefault()).replaceAll("\n", "\\\\n"));
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  public static BaseMatcher<Diagnostic<JavaFileObject>> diagnosticLineAndColumn(
      final long line, final long column) {
    return new BaseMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      public boolean matches(Object object) {
        Diagnostic<JavaFileObject> item = (Diagnostic<JavaFileObject>) object;
        return item.getLineNumber() == line && item.getColumnNumber() == column;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("a diagnostic on line:column ")
            .appendValue(line)
            .appendText(":")
            .appendValue(column);
      }
    };
  }

  public static BaseMatcher<Diagnostic<JavaFileObject>> diagnosticOnLine(
      final URI fileURI, final long line) {
    return new BaseMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      public boolean matches(Object object) {
        Diagnostic<JavaFileObject> item = (Diagnostic<JavaFileObject>) object;
        return item.getSource().toUri().equals(fileURI)
            && item.getLineNumber() == line;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("a diagnostic on line ")
            .appendValue(line);
      }
    };
  }

  public static BaseMatcher<Diagnostic<JavaFileObject>> diagnosticOnLine(
      final URI fileURI, final long line, final String message) {
    return new BaseMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      public boolean matches(Object object) {
        Diagnostic<JavaFileObject> item = (Diagnostic<JavaFileObject>) object;
        return item.getSource().toUri().equals(fileURI)
            && item.getLineNumber() == line
            && item.getMessage(Locale.getDefault()).contains(message);
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("a diagnostic on line ")
            .appendValue(line)
            .appendText(" that contains \n")
            .appendValue(message)
            .appendText("\n");
      }
    };
  }


  public static BaseMatcher<Diagnostic<JavaFileObject>> diagnosticMessage(
      final Matcher<String> matcher) {
    return new BaseMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      public boolean matches(Object object) {
        Diagnostic<JavaFileObject> item = (Diagnostic<JavaFileObject>) object;
        return matcher.matches(item.getMessage(Locale.getDefault()));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a diagnostic with message ").appendDescriptionOf(matcher);
      }
    };
  }

  /**
   * Comment that marks a bug on the next line in a test file.  For example,
   * "// BUG: Diagnostic contains: foo.bar()", where "foo.bar()" is a string that should be in the
   * diagnostic for the line.  Multiple expected strings may be separated by newlines, e.g.
   * // BUG: Diagnostic contains: foo.bar()
   * // bar.baz()
   * // baz.foo()
   */
  private static final String BUG_MARKER_COMMENT = "// BUG: Diagnostic contains:";

  /**
   * Asserts that the diagnostics contain a diagnostic on each line of the source file that
   * matches our bug marker pattern.  Parses the bug marker pattern for the specific string to
   * look for in the diagnostic.
   * @param source File in which to find matching lines
   *
   * TODO(user): Switch to use assertThat instead of assertTrue.
   */
  @SuppressWarnings("unchecked")
  public void assertHasDiagnosticOnAllMatchingLines(JavaFileObject source)
      throws IOException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = getDiagnostics();
    final LineNumberReader reader = new LineNumberReader(
        CharSource.wrap(source.getCharContent(false)).openStream());
    do {
      String line = reader.readLine();
      if (line == null) {
        break;
      }

      if (line.contains(BUG_MARKER_COMMENT)) {
        // Diagnostic must contain all patterns from the bug marker comment.
        List<String> patterns = extractPatterns(line, reader);
        int lineNumber = reader.getLineNumber();
        for (String pattern : patterns) {
          Matcher<Iterable<Diagnostic<JavaFileObject>>> patternMatcher =
              hasItem(diagnosticOnLine(source.toUri(), lineNumber, pattern));
          assertTrue(
              "Did not see an error on line " + lineNumber + " containing " + pattern
                  + ". All errors:\n" + diagnostics,
              patternMatcher.matches(diagnostics));
        }

        if (checkName != null) {
          // Diagnostic must contain check name.
          Matcher<Iterable<Diagnostic<JavaFileObject>>> checkNameMatcher = hasItem(
              diagnosticOnLine(source.toUri(), lineNumber, "[" + checkName + "]"));
          assertTrue(
              "Did not see an error on line " + lineNumber + " containing [" + checkName
                  + "]. All errors:\n" + diagnostics,
              checkNameMatcher.matches(diagnostics));
        }

      } else {
        int lineNumber = reader.getLineNumber() + 1;
        // Cast is unnecessary, but javac throws an error because of poor type inference.
        Matcher<Iterable<Diagnostic<JavaFileObject>>> matcher =
            (Matcher) not(hasItem(diagnosticOnLine(source.toUri(), lineNumber)));
        assertTrue("Saw unexpected error on line " + lineNumber, matcher.matches(diagnostics));
      }
    } while (true);
    reader.close();
  }

  /**
   * Extracts the patterns from a bug marker comment.
   *
   * @param line The first line of the bug marker comment
   * @param reader A reader for the test file
   * @return A list of patterns that the diagnostic is expected to contain
   * @throws IOException
   */
  private static List<String> extractPatterns(String line, BufferedReader reader)
      throws IOException {
    int bugMarkerIndex = line.indexOf(BUG_MARKER_COMMENT);
    if (bugMarkerIndex < 0) {
      throw new IllegalArgumentException("Line must contain bug marker prefix");
    }
    List<String> result = new ArrayList<String>();
    String restOfLine = line.substring(bugMarkerIndex + BUG_MARKER_COMMENT.length()).trim();
    result.add(restOfLine);
    line = reader.readLine().trim();
    while (line.startsWith("//")) {
      restOfLine = line.substring(2).trim();
      result.add(restOfLine);
      line = reader.readLine().trim();
    }

    return result;
  }
}
