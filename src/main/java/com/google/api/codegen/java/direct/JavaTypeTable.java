/* Copyright 2016 Google Inc
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
package com.google.api.codegen.java.direct;

import com.google.api.codegen.LanguageUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class JavaTypeTable {
  /**
   * A bi-map from full names to short names indicating the import map.
   */
  private final BiMap<String, String> imports = HashBiMap.create();

  /**
   * A map from simple type name to a boolean, indicating whether its in java.lang or not. If a
   * simple type name is not in the map, this information is unknown.
   */
  private final Map<String, Boolean> implicitImports = Maps.newHashMap();

  private static final String JAVA_LANG_TYPE_PREFIX = "java.lang.";

  /**
   * A map from unboxed Java primitive type name to boxed counterpart.
   */
  private static final ImmutableMap<String, String> BOXED_TYPE_MAP =
      ImmutableMap.<String, String>builder()
          .put("boolean", "Boolean")
          .put("int", "Integer")
          .put("long", "Long")
          .put("float", "Float")
          .put("double", "Double")
          .build();

  public String importAndGetShortestName(String longName) {
    int lastDotIndex = longName.lastIndexOf('.');
    if (lastDotIndex < 0) {
      throw new IllegalArgumentException("expected fully qualified name");
    }
    String shortTypeName = longName.substring(lastDotIndex + 1);
    return importAndGetShortestName(longName, shortTypeName);
  }

  public String importAndGetShortestName(String fullName, String shortName) {
    // Derive a short name if possible
    if (imports.containsKey(fullName)) {
      // Short name already there.
      return imports.get(fullName);
    }
    if (imports.containsValue(shortName)
        || !fullName.startsWith(JAVA_LANG_TYPE_PREFIX) && isImplicitImport(shortName)) {
      // Short name clashes, use long name.
      return fullName;
    }
    imports.put(fullName, shortName);
    return shortName;
  }

  /**
   * Returns the Java representation of a basic type in boxed form.
   */
  public static String getBoxedTypeName(String typeName) {
    return LanguageUtil.getRename(typeName, BOXED_TYPE_MAP);
  }

  /**
   * Checks whether the simple type name is implicitly imported from java.lang.
   */
  private boolean isImplicitImport(String name) {
    Boolean yes = implicitImports.get(name);
    if (yes != null) {
      return yes;
    }
    // Use reflection to determine whether the name exists in java.lang.
    try {
      Class.forName("java.lang." + name);
      yes = true;
    } catch (Exception e) {
      yes = false;
    }
    implicitImports.put(name, yes);
    return yes;
  }
}