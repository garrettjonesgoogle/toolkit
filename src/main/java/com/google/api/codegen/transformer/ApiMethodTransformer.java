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

import com.google.api.codegen.CollectionConfig;
import com.google.api.codegen.MethodConfig;
import com.google.api.codegen.PageStreamingConfig;
import com.google.api.codegen.ServiceMessages;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.viewmodel.ApiMethodDocView;
import com.google.api.codegen.viewmodel.ApiMethodType;
import com.google.api.codegen.viewmodel.CallableMethodDetailView;
import com.google.api.codegen.viewmodel.DynamicDefaultableParamView;
import com.google.api.codegen.viewmodel.ListMethodDetailView;
import com.google.api.codegen.viewmodel.MapParamDocView;
import com.google.api.codegen.viewmodel.OptionalArrayMethodView;
import com.google.api.codegen.viewmodel.ParamDocView;
import com.google.api.codegen.viewmodel.PathTemplateCheckView;
import com.google.api.codegen.viewmodel.RequestObjectMethodDetailView;
import com.google.api.codegen.viewmodel.RequestObjectParamView;
import com.google.api.codegen.viewmodel.SimpleParamDocView;
import com.google.api.codegen.viewmodel.StaticApiMethodView;
import com.google.api.codegen.viewmodel.StaticApiMethodView.Builder;
import com.google.api.codegen.viewmodel.UnpagedListCallableMethodDetailView;
import com.google.api.tools.framework.model.Field;
import com.google.api.tools.framework.model.TypeRef;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiMethodTransformer {
  private InitCodeTransformer initCodeTransformer;

  public ApiMethodTransformer() {
    this.initCodeTransformer = new InitCodeTransformer();
  }

  public StaticApiMethodView generatePagedFlattenedMethod(
      MethodTransformerContext context, ImmutableList<Field> fields) {
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(context.getNamer().getApiMethodName(context.getMethod()));
    setListMethodFields(context, methodViewBuilder);
    setFlattenedMethodFields(context, fields, methodViewBuilder);

    return methodViewBuilder.type(ApiMethodType.PagedFlattenedMethod).build();
  }

  public StaticApiMethodView generatePagedRequestObjectMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(namer.getApiMethodName(context.getMethod()));
    setListMethodFields(context, methodViewBuilder);
    setRequestObjectMethodFields(
        context, namer.getPagedCallableMethodName(context.getMethod()), methodViewBuilder);

    return methodViewBuilder.type(ApiMethodType.PagedRequestObjectMethod).build();
  }

  public StaticApiMethodView generatePagedCallableMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(namer.getPagedCallableMethodName(context.getMethod()));
    setListMethodFields(context, methodViewBuilder);
    setCallableMethodFields(
        context, namer.getPagedCallableName(context.getMethod()), methodViewBuilder);

    return methodViewBuilder.type(ApiMethodType.PagedCallableMethod).build();
  }

  public StaticApiMethodView generateUnpagedListCallableMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(namer.getCallableMethodName(context.getMethod()));
    setListMethodFields(context, methodViewBuilder);
    setCallableMethodFields(context, namer.getCallableName(context.getMethod()), methodViewBuilder);

    String getResourceListCallName =
        namer.getGetResourceListCallName(
            context.getMethodConfig().getPageStreaming().getResourcesField());
    UnpagedListCallableMethodDetailView unpagedListCallableDetails =
        UnpagedListCallableMethodDetailView.newBuilder()
            .fnGetResourceListCall(getResourceListCallName)
            .build();
    methodViewBuilder.unpagedListCallableMethod(unpagedListCallableDetails);

    methodViewBuilder.responseTypeName(
        context.getTypeTable().getAndSaveNicknameFor(context.getMethod().getOutputType()));

    return methodViewBuilder.type(ApiMethodType.UnpagedListCallableMethod).build();
  }

  public StaticApiMethodView generateFlattenedMethod(
      MethodTransformerContext context, ImmutableList<Field> fields) {
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(context.getNamer().getApiMethodName(context.getMethod()));
    setFlattenedMethodFields(context, fields, methodViewBuilder);
    setSyncStaticReturnFields(context, methodViewBuilder);

    return methodViewBuilder.type(ApiMethodType.FlattenedMethod).build();
  }

  public StaticApiMethodView generateRequestObjectMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(namer.getApiMethodName(context.getMethod()));
    setRequestObjectMethodFields(
        context, namer.getCallableMethodName(context.getMethod()), methodViewBuilder);
    setSyncStaticReturnFields(context, methodViewBuilder);

    return methodViewBuilder.type(ApiMethodType.RequestObjectMethod).build();
  }

  public StaticApiMethodView generateCallableMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    StaticApiMethodView.Builder methodViewBuilder = StaticApiMethodView.newBuilder();

    setCommonFields(context, methodViewBuilder);
    methodViewBuilder.name(namer.getCallableMethodName(context.getMethod()));
    setCallableMethodFields(context, namer.getCallableName(context.getMethod()), methodViewBuilder);
    methodViewBuilder.responseTypeName(
        context.getTypeTable().getAndSaveNicknameFor(context.getMethod().getOutputType()));
    methodViewBuilder.hasReturnValue(
        !ServiceMessages.s_isEmptyType(context.getMethod().getOutputType()));

    return methodViewBuilder.type(ApiMethodType.CallableMethod).build();
  }

  public void setCommonFields(
      MethodTransformerContext context, StaticApiMethodView.Builder methodViewBuilder) {
    SurfaceNamer namer = context.getNamer();
    methodViewBuilder.apiRequestTypeName(
        context.getTypeTable().getAndSaveNicknameFor(context.getMethod().getInputType()));
    methodViewBuilder.apiClassName(namer.getApiWrapperClassName(context.getInterface()));
    methodViewBuilder.apiVariableName(namer.getApiWrapperVariableName(context.getInterface()));
  }

  private void setListMethodFields(
      MethodTransformerContext context, StaticApiMethodView.Builder methodViewBuilder) {
    PageStreamingConfig pageStreaming = context.getMethodConfig().getPageStreaming();
    String resourceTypeName =
        context
            .getTypeTable()
            .getAndSaveNicknameForElementType(pageStreaming.getResourcesField().getType());
    methodViewBuilder.listMethod(
        ListMethodDetailView.newBuilder().resourceTypeName(resourceTypeName).build());
    methodViewBuilder.responseTypeName(
        context.getNamer().getPageAccessorTypeName(resourceTypeName));
    methodViewBuilder.hasReturnValue(true);
  }

  public void setFlattenedMethodFields(
      MethodTransformerContext context,
      ImmutableList<Field> fields,
      StaticApiMethodView.Builder methodViewBuilder) {
    SurfaceNamer namer = context.getNamer();
    methodViewBuilder.initCode(initCodeTransformer.generateInitCode(context, fields));
    methodViewBuilder.doc(
        ApiMethodDocView.newBuilder()
            .mainDocLines(namer.getDocLines(context.getMethod()))
            .paramDocs(getMethodParamDocs(context, fields))
            .throwsDocLines(namer.getThrowsDocLines())
            .build());

    List<RequestObjectParamView> params = new ArrayList<>();
    for (Field field : fields) {
      params.add(generateRequestObjectParam(context, field));
    }
    methodViewBuilder.methodParams(params);
    methodViewBuilder.requestObjectParams(params);

    methodViewBuilder.pathTemplateChecks(generatePathTemplateChecks(context, fields));
  }

  public void setRequestObjectMethodFields(
      MethodTransformerContext context,
      String callableMethodName,
      StaticApiMethodView.Builder methodViewBuilder) {
    SurfaceNamer namer = context.getNamer();
    methodViewBuilder.doc(
        ApiMethodDocView.newBuilder()
            .mainDocLines(namer.getDocLines(context.getMethod()))
            .paramDocs(
                Arrays.<ParamDocView>asList(
                    getRequestObjectParamDoc(context, context.getMethod().getInputType())))
            .throwsDocLines(namer.getThrowsDocLines())
            .build());
    methodViewBuilder.initCode(initCodeTransformer.generateRequestObjectInitCode(context));

    methodViewBuilder.methodParams(new ArrayList<RequestObjectParamView>());
    methodViewBuilder.requestObjectParams(new ArrayList<RequestObjectParamView>());
    methodViewBuilder.pathTemplateChecks(new ArrayList<PathTemplateCheckView>());

    RequestObjectMethodDetailView.Builder detailBuilder =
        RequestObjectMethodDetailView.newBuilder();
    if (context.getMethodConfig().hasRequestObjectMethod()) {
      detailBuilder.accessModifier(context.getNamer().getPublicAccessModifier());
    } else {
      detailBuilder.accessModifier(context.getNamer().getPrivateAccessModifier());
    }
    detailBuilder.callableMethodName(callableMethodName);
    methodViewBuilder.requestObjectMethod(detailBuilder.build());
  }

  private void setCallableMethodFields(
      MethodTransformerContext context, String callableName, Builder methodViewBuilder) {
    methodViewBuilder.doc(
        ApiMethodDocView.newBuilder()
            .mainDocLines(context.getNamer().getDocLines(context.getMethod()))
            .paramDocs(new ArrayList<ParamDocView>())
            .throwsDocLines(new ArrayList<String>())
            .build());
    methodViewBuilder.initCode(initCodeTransformer.generateRequestObjectInitCode(context));

    methodViewBuilder.methodParams(new ArrayList<RequestObjectParamView>());
    methodViewBuilder.requestObjectParams(new ArrayList<RequestObjectParamView>());
    methodViewBuilder.pathTemplateChecks(new ArrayList<PathTemplateCheckView>());

    String genericAwareResponseType =
        context
            .getNamer()
            .getGenericAwareResponseType(
                context.getTypeTable(), context.getMethod().getOutputType());
    methodViewBuilder.callableMethod(
        CallableMethodDetailView.newBuilder()
            .genericAwareResponseType(genericAwareResponseType)
            .callableName(callableName)
            .build());
  }

  public void setSyncStaticReturnFields(
      MethodTransformerContext context, StaticApiMethodView.Builder methodViewBuilder) {
    methodViewBuilder.responseTypeName(
        context
            .getNamer()
            .getStaticReturnTypeName(
                context.getTypeTable(), context.getMethod(), context.getMethodConfig()));
    methodViewBuilder.hasReturnValue(
        !ServiceMessages.s_isEmptyType(context.getMethod().getOutputType()));
  }

  public List<PathTemplateCheckView> generatePathTemplateChecks(
      MethodTransformerContext context, ImmutableList<Field> fields) {
    List<PathTemplateCheckView> pathTemplateChecks = new ArrayList<>();
    for (Field field : fields) {
      ImmutableMap<String, String> fieldNamePatterns =
          context.getMethodConfig().getFieldNamePatterns();
      String entityName = fieldNamePatterns.get(field.getSimpleName());
      if (entityName != null) {
        CollectionConfig collectionConfig = context.getCollectionConfig(entityName);
        PathTemplateCheckView check = new PathTemplateCheckView();
        check.pathTemplateName = context.getNamer().getPathTemplateName(collectionConfig);
        check.paramName = context.getNamer().getVariableName(field);

        pathTemplateChecks.add(check);
      }
    }
    return pathTemplateChecks;
  }

  public OptionalArrayMethodView generateOptionalArrayMethod(MethodTransformerContext context) {
    SurfaceNamer namer = context.getNamer();
    OptionalArrayMethodView apiMethod = new OptionalArrayMethodView();

    ApiMethodDocView.Builder docBuilder = ApiMethodDocView.newBuilder();

    docBuilder.mainDocLines(namer.getDocLines(context.getMethod()));
    List<ParamDocView> paramDocs =
        getMethodParamDocs(context, context.getMethodConfig().getRequiredFields());
    paramDocs.add(getOptionalArrayParamDoc(context, context.getMethodConfig().getOptionalFields()));
    paramDocs.add(getCallSettingsParamDoc(context));
    docBuilder.paramDocs(paramDocs);
    docBuilder.returnTypeName(
        namer.getDynamicReturnTypeName(
            context.getTypeTable(), context.getMethod(), context.getMethodConfig()));
    docBuilder.throwsDocLines(new ArrayList<String>());

    apiMethod.doc = docBuilder.build();

    apiMethod.name = namer.getApiMethodName(context.getMethod());
    apiMethod.requestTypeName =
        context.getTypeTable().getAndSaveNicknameFor(context.getMethod().getInputType());
    apiMethod.key = namer.getMethodKey(context.getMethod());
    apiMethod.grpcMethodName = namer.getGrpcMethodName(context.getMethod());

    List<DynamicDefaultableParamView> methodParams = new ArrayList<>();
    for (Field field : context.getMethodConfig().getRequiredFields()) {
      methodParams.add(generateDefaultableParam(context, field));
    }

    // TODO create a map TypeRef here instead of an array
    // (not done yet because array is sufficient for PHP, and maps are more complex to construct)
    TypeRef arrayType = TypeRef.fromPrimitiveName("string").makeRepeated();

    DynamicDefaultableParamView optionalArgs = new DynamicDefaultableParamView();
    optionalArgs.name = namer.varName(Name.from("optional", "args"));
    optionalArgs.defaultValue = context.getTypeTable().getZeroValueAndSaveNicknameFor(arrayType);
    methodParams.add(optionalArgs);

    DynamicDefaultableParamView callSettings = new DynamicDefaultableParamView();
    callSettings.name = namer.varName(Name.from("call", "settings"));
    callSettings.defaultValue = context.getTypeTable().getZeroValueAndSaveNicknameFor(arrayType);
    methodParams.add(callSettings);

    apiMethod.methodParams = methodParams;

    List<RequestObjectParamView> requiredRequestObjectParams = new ArrayList<>();
    for (Field field : context.getMethodConfig().getRequiredFields()) {
      requiredRequestObjectParams.add(generateRequestObjectParam(context, field));
    }
    apiMethod.requiredRequestObjectParams = requiredRequestObjectParams;

    List<RequestObjectParamView> optionalRequestObjectParams = new ArrayList<>();
    for (Field field : context.getMethodConfig().getOptionalFields()) {
      optionalRequestObjectParams.add(generateRequestObjectParam(context, field));
    }
    apiMethod.optionalRequestObjectParams = optionalRequestObjectParams;

    return apiMethod;
  }

  public DynamicDefaultableParamView generateDefaultableParam(
      MethodTransformerContext context, Field field) {
    DynamicDefaultableParamView param = new DynamicDefaultableParamView();
    param.name = context.getNamer().getVariableName(field);
    param.defaultValue = "";
    return param;
  }

  public RequestObjectParamView generateRequestObjectParam(
      MethodTransformerContext context, Field field) {
    SurfaceNamer namer = context.getNamer();
    RequestObjectParamView param = new RequestObjectParamView();
    param.name = namer.getVariableName(field);
    if (namer.shouldImportRequestObjectParamType(field)) {
      param.elementTypeName =
          context.getTypeTable().getAndSaveNicknameForElementType(field.getType());
      param.typeName = context.getTypeTable().getAndSaveNicknameFor(field.getType());
    } else {
      param.elementTypeName = SurfaceNamer.NOT_IMPLEMENTED;
      param.typeName = SurfaceNamer.NOT_IMPLEMENTED;
    }
    param.setCallName = namer.getSetFunctionCallName(field.getType(), field.getSimpleName());
    param.isMap = field.getType().isMap();
    param.isArray = !field.getType().isMap() && field.getType().isRepeated();
    return param;
  }

  public List<ParamDocView> getMethodParamDocs(
      MethodTransformerContext context, Iterable<Field> fields) {
    List<ParamDocView> allDocs = new ArrayList<>();
    for (Field field : fields) {
      SimpleParamDocView paramDoc = new SimpleParamDocView();
      paramDoc.paramName = context.getNamer().getVariableName(field);
      paramDoc.typeName = context.getTypeTable().getFullNameFor(field.getType());

      List<String> docLines = null;
      MethodConfig methodConfig = context.getMethodConfig();
      if (methodConfig.isPageStreaming()
          && methodConfig.getPageStreaming().hasPageSizeField()
          && field.equals(methodConfig.getPageStreaming().getPageSizeField())) {
        docLines =
            Arrays.asList(
                new String[] {
                  "The maximum number of resources contained in the underlying API",
                  "response. If page streaming is performed per-resource, this",
                  "parameter does not affect the return value. If page streaming is",
                  "performed per-page, this determines the maximum number of",
                  "resources in a page."
                });
      } else {
        docLines = context.getNamer().getDocLines(field);
      }

      paramDoc.firstLine = docLines.get(0);
      paramDoc.remainingLines = docLines.subList(1, docLines.size());

      allDocs.add(paramDoc);
    }
    return allDocs;
  }

  public SimpleParamDocView getRequestObjectParamDoc(
      MethodTransformerContext context, TypeRef typeRef) {
    SimpleParamDocView paramDoc = new SimpleParamDocView();
    paramDoc.paramName = "request";
    paramDoc.typeName = context.getTypeTable().getAndSaveNicknameFor(typeRef);
    paramDoc.firstLine = "The request object containing all of the parameters for the API call.";
    paramDoc.remainingLines = Arrays.asList();
    return paramDoc;
  }

  public ParamDocView getOptionalArrayParamDoc(
      MethodTransformerContext context, Iterable<Field> fields) {
    MapParamDocView paramDoc = new MapParamDocView();

    paramDoc.paramName = context.getNamer().varName(Name.from("optional", "args"));
    paramDoc.typeName = context.getNamer().getOptionalArrayTypeName();

    List<String> docLines = null;
    if (!fields.iterator().hasNext()) {
      docLines =
          Arrays.asList(
              new String[] {
                "Optional. There are no optional parameters for this method yet;",
                // FIXME remove PHP-specific variable naming/syntax
                "          this $optionalArgs parameter reserves a spot for future ones."
              });
    } else {
      docLines = Arrays.asList("Optional.");
    }
    paramDoc.firstLine = docLines.get(0);
    paramDoc.remainingLines = docLines.subList(1, docLines.size());

    paramDoc.arrayKeyDocs = getMethodParamDocs(context, fields);

    return paramDoc;
  }

  public ParamDocView getCallSettingsParamDoc(MethodTransformerContext context) {
    MapParamDocView paramDoc = new MapParamDocView();

    paramDoc.paramName = context.getNamer().varName(Name.from("call", "settings"));
    paramDoc.typeName = context.getNamer().getOptionalArrayTypeName();
    paramDoc.firstLine = "Optional.";
    paramDoc.remainingLines = new ArrayList<>();

    List<ParamDocView> arrayKeyDocs = new ArrayList<>();
    SimpleParamDocView retrySettingsDoc = new SimpleParamDocView();
    retrySettingsDoc.typeName = context.getNamer().getRetrySettingsClassName();
    retrySettingsDoc.paramName = context.getNamer().varName(Name.from("retry", "settings"));
    retrySettingsDoc.firstLine = "Retry settings to use for this call. If present, then";
    // FIXME remove PHP-specific variable naming/syntax
    retrySettingsDoc.remainingLines = Arrays.asList("$timeout is ignored.");
    arrayKeyDocs.add(retrySettingsDoc);

    SimpleParamDocView timeoutDoc = new SimpleParamDocView();
    timeoutDoc.typeName = context.getTypeTable().getAndSaveNicknameFor(TypeRef.of(Type.TYPE_INT32));
    timeoutDoc.paramName = context.getNamer().varName(Name.from("timeout", "millis"));
    // FIXME remove PHP-specific variable naming/syntax
    timeoutDoc.firstLine = "Timeout to use for this call. Only used if $retrySettings";
    timeoutDoc.remainingLines = Arrays.asList("is not set.");
    arrayKeyDocs.add(timeoutDoc);

    paramDoc.arrayKeyDocs = arrayKeyDocs;

    return paramDoc;
  }
}
