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
package com.google.api.codegen.transformer;

import com.google.api.codegen.MethodConfig;
import com.google.api.codegen.PageStreamingConfig;
import com.google.api.codegen.viewmodel.ApiCallSettingsView;
import com.google.api.codegen.viewmodel.ApiCallableType;
import com.google.api.codegen.viewmodel.ApiCallableView;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.TypeRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiCallableTransformer {

  public List<ApiCallableView> generateStaticApiCallables(SurfaceTransformerContext context) {
    List<ApiCallableView> callableMembers = new ArrayList<>();

    for (Method method : context.getInterface().getMethods()) {
      callableMembers.addAll(generateStaticApiCallables(context.asMethodContext(method)));
    }

    return callableMembers;
  }

  public List<ApiCallSettingsView> generateCallSettings(SurfaceTransformerContext context) {
    List<ApiCallSettingsView> settingsMembers = new ArrayList<>();

    for (Method method : context.getInterface().getMethods()) {
      settingsMembers.addAll(generateApiCallableSettings(context.asMethodContext(method)));
    }

    return settingsMembers;
  }

  private List<ApiCallableView> generateStaticApiCallables(MethodTransformerContext context) {
    ModelTypeTable typeTable = context.getTypeTable();
    Method method = context.getMethod();
    MethodConfig methodConfig = context.getMethodConfig();

    List<ApiCallableView> apiCallables = new ArrayList<>();

    ApiCallableView.Builder apiCallableBuilder = ApiCallableView.newBuilder();

    apiCallableBuilder.requestTypeName(typeTable.getAndSaveNicknameFor(method.getInputType()));
    apiCallableBuilder.responseTypeName(typeTable.getAndSaveNicknameFor(method.getOutputType()));
    apiCallableBuilder.name(context.getNamer().getCallableName(method));
    apiCallableBuilder.settingsFunctionName(context.getNamer().getSettingsFunctionName(method));

    if (methodConfig.isBundling()) {
      apiCallableBuilder.type(ApiCallableType.BundlingApiCallable);
    } else {
      apiCallableBuilder.type(ApiCallableType.SimpleApiCallable);
    }

    apiCallables.add(apiCallableBuilder.build());

    if (methodConfig.isPageStreaming()) {
      PageStreamingConfig pageStreaming = methodConfig.getPageStreaming();

      ApiCallableView.Builder pagedApiCallableBuilder = ApiCallableView.newBuilder();

      String pagedResponseTypeName =
          context
              .getNamer()
              .getAndSavePagedResponseTypeName(
                  typeTable, pageStreaming.getResourcesField().getType());

      pagedApiCallableBuilder.requestTypeName(
          typeTable.getAndSaveNicknameFor(method.getInputType()));
      pagedApiCallableBuilder.responseTypeName(pagedResponseTypeName);
      pagedApiCallableBuilder.name(context.getNamer().getPagedCallableName(method));
      pagedApiCallableBuilder.settingsFunctionName(
          context.getNamer().getSettingsFunctionName(method));

      apiCallables.add(pagedApiCallableBuilder.type(ApiCallableType.PagedApiCallable).build());
    }

    return apiCallables;
  }

  private List<ApiCallSettingsView> generateApiCallableSettings(MethodTransformerContext context) {
    ModelTypeTable typeTable = context.getTypeTable();
    Method method = context.getMethod();
    MethodConfig methodConfig = context.getMethodConfig();

    ApiCallSettingsView.Builder settings = ApiCallSettingsView.newBuilder();

    settings.methodName(context.getNamer().getApiMethodName(method));
    settings.requestTypeName(typeTable.getAndSaveNicknameFor(method.getInputType()));
    settings.responseTypeName(typeTable.getAndSaveNicknameFor(method.getOutputType()));

    if (methodConfig.isPageStreaming()) {
      settings.type(ApiCallableType.PagedApiCallable);
      TypeRef resourceType = methodConfig.getPageStreaming().getResourcesField().getType();
      settings.resourceTypeName(
          context.getTypeTable().getAndSaveNicknameForElementType(resourceType));
    } else {
      settings.resourceTypeName(
          context
              .getNamer()
              .getNotImplementedString(
                  "ApiCallableTransformer.generateApiCallableSettings - resourceTypeName"));
      if (methodConfig.isBundling()) {
        settings.type(ApiCallableType.BundlingApiCallable);
      } else {
        settings.type(ApiCallableType.SimpleApiCallable);
      }
    }

    settings.memberName(context.getNamer().getSettingsMemberName(method));
    settings.fnGetterName(context.getNamer().getSettingsMemberName(method));

    return Arrays.asList(settings.build());
  }
}
