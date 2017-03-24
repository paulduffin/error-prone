/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.apply;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 */
public interface ImportOrganizer {

  /**
   * @return the {@link Comparator} that will be used during initial collation of the imports;
   * determines the order of the {@link Iterable} passed to {@link #organizeImports(Iterable)}.
   */
  Comparator<String> comparator();

  /**
   * Organize the imports supplied, e.g. insert blank lines between various groups.
   *
   * @param importStrings the imports to organize, in order defined by {@link #comparator()}. Each
   * string is of the format {@code import( static)? <identifier>}.
   *
   * @return the list of organized imports, an empty string represents a blank line.
   */
  Iterable<String> organizeImports(Iterable<String> importStrings);

  static boolean isStatic(String importString) {
    return importString.startsWith("import static ");
  }

  /**
   * A {@link Comparator} that sorts import statements so that all static imports come before
   * all non-static imports and otherwise sorted alphabetically.
   */
  static Comparator<String> staticFirst() {
    return new Ordering<String>() {
      @Override
      public int compare(String s1, String s2) {
        return ComparisonChain.start()
            .compareTrueFirst(isStatic(s1), isStatic(s2))
            .compare(s1, s2)
            .result();
      }
    };
  }

  /**
   * An organizer that will insert a blank link between static and non-static imports as ordered by
   * the supplied {@link Comparator}.
   *
   * @param comparator the order of imports.
   * @return an {@link ImportOrganizer}.
   */
  static ImportOrganizer separateStaticAndNonStatic(Comparator<String> comparator) {
    return new ImportOrganizer() {
      @Override
      public Comparator<String> comparator() {
        return comparator;
      }

      @Override
      public Iterable<String> organizeImports(Iterable<String> importStrings) {
        List<String> organized = new ArrayList<>();

        // output sorted imports, with a line break between static and non-static imports
        boolean first = true;
        boolean prevIsStatic = true;
        for (String importString : importStrings) {
          boolean isStatic = ImportOrganizer.isStatic(importString);
          if (!first && prevIsStatic != isStatic) {
            // Add a blank line.
            organized.add("");
          }
          organized.add(importString);
          prevIsStatic = isStatic;
          first = false;
        }

        return organized;
      }
    };
  }

  /**
   * An {@link ImportOrganizer} that sorts import statements according to the Google Java Style
   * Guide, i.e. static first, static and non-static separated by blank line.
   */
  ImportOrganizer STATIC_FIRST_ORGANIZER = separateStaticAndNonStatic(staticFirst());
}
