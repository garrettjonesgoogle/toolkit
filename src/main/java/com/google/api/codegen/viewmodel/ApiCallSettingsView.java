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
package com.google.api.codegen.viewmodel;

import com.google.api.codegen.viewmodel.ApiCallableView.Builder;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ApiCallSettingsView {
  public abstract ApiCallableType type();

  public abstract String methodName();

  public abstract String requestTypeName();

  public abstract String responseTypeName();

  public abstract String resourceTypeName();

  public abstract String memberName();

  public abstract String fnGetterName();

  public static Builder newBuilder() {
    return new AutoValue_ApiCallSettingsView.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder type(ApiCallableType type);

    public abstract Builder methodName(String apiMethodName);

    public abstract Builder requestTypeName(String val);

    public abstract Builder responseTypeName(String val);

    public abstract Builder resourceTypeName(String val);

    public abstract Builder memberName(String val);

    public abstract Builder fnGetterName(String val);

    public abstract ApiCallSettingsView build();
  }
}
