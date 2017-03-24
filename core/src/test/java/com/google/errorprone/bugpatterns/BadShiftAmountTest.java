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

package com.google.errorprone.bugpatterns;

import com.google.errorprone.junit.BugCheckerRule;
import com.google.errorprone.junit.CompilationTest;
import com.google.errorprone.junit.ErrorProneTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Bill Pugh (bill.pugh@gmail.com)
 */
@RunWith(JUnit4.class)
@ErrorProneTest(resourcePattern = "testdata/{checker}{method}.java")
public class BadShiftAmountTest {

  @Rule
  public BugCheckerRule rule = BugCheckerRule.forBugChecker(BadShiftAmount.class);

  @CompilationTest
  @Test
  public void testPositiveCases() throws Exception {
  }

  @CompilationTest
  @Test
  public void testNegativeCases() throws Exception {
  }
}
