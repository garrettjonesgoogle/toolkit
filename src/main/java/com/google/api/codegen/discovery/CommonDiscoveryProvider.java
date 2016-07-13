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
package com.google.api.codegen.discovery;

import com.google.api.codegen.DiscoveryContext;
import com.google.api.codegen.GeneratedResult;
import com.google.api.codegen.SnippetSetRunner;
import com.google.api.tools.framework.snippet.Doc;
import com.google.protobuf.Api;
import com.google.protobuf.Method;

import java.util.Collections;
import java.util.Map;

/**
 * Common DiscoveryProvider which runs code generation.
 */
public class CommonDiscoveryProvider implements DiscoveryProvider {
  private final DiscoveryContext context;
  private final SnippetSetRunner.Generator<Method> generator;
  private final String snippetFileName;

  public CommonDiscoveryProvider(
      DiscoveryContext context,
      SnippetSetRunner.Generator<Method> generator,
      String snippetFileName) {
    this.context = context;
    this.generator = generator;
    this.snippetFileName = snippetFileName;
  }

  @Override
  public Map<String, Doc> generate(Method method) {
    GeneratedResult result = generator.generate(method, snippetFileName, context);

    Api api = context.getApi();
    String outputRoot =
        "autogenerated/" + api.getName() + "/" + api.getVersion() + "/" + context.getApiRevision();

    String resultPath = outputRoot + "/" + result.getFilename();

    return Collections.singletonMap(resultPath, result.getDoc());
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private DiscoveryContext context;
    private SnippetSetRunner.Generator<Method> generator;
    private String snippetFileName;

    private Builder() {}

    public Builder setContext(DiscoveryContext context) {
      this.context = context;
      return this;
    }

    public Builder setSnippetSetRunner(SnippetSetRunner.Generator<Method> generator) {
      this.generator = generator;
      return this;
    }

    public Builder setSnippetFileName(String snippetFileName) {
      this.snippetFileName = snippetFileName;
      return this;
    }

    public CommonDiscoveryProvider build() {
      return new CommonDiscoveryProvider(context, generator, snippetFileName);
    }
  }
}
