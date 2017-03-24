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

/**
 */
public class PatternExpander {

    private final String format;

    public PatternExpander(String pattern) {
        pattern = pattern.replace("{checker}", "%1$s");
        pattern = pattern.replace("{method}", "%2$s");
        format = pattern;
    }

    public String expand(String checkerName, String methodName) {
        if (methodName.startsWith("test")) {
            methodName = methodName.substring(4);
        }
        return String.format(format, checkerName, methodName);
    }
}
