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
import com.google.api.codegen.java.JavaDocUtil;
import com.google.api.codegen.metacode.InitValueConfig;
import com.google.api.codegen.util.Name;
import com.google.api.tools.framework.aspects.documentation.model.DocumentationUtil;
import com.google.api.tools.framework.model.Field;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.ProtoElement;
import com.google.api.tools.framework.model.TypeRef;

import java.util.Arrays;
import java.util.List;

public class JavaIdentifierNamer implements IdentifierNamer {
  @Override
  public String getApiWrapperClassName(Interface interfaze) {
    return interfaze.getSimpleName() + "Api";
  }

  @Override
  public String getApiWrapperVariableName(Interface interfaze) {
    return Name.upperCamel(interfaze.getSimpleName(), "Api").toLowerCamel();
  }

  @Override
  public String getVariableName(String identifier, InitValueConfig initValueConfig) {
    if (initValueConfig.hasFormattingConfig()) {
      return Name.from("formatted", identifier).toLowerCamel();
    } else {
      return Name.from(identifier).toLowerCamel();
    }
  }

  @Override
  public String getSetFunctionCallName(TypeRef type, String identifier) {
    if (type.isMap()) {
      return Name.from("put", "all", identifier).toLowerCamel();
    } else if (type.isRepeated()) {
      return Name.from("add", "all", identifier).toLowerCamel();
    } else {
      return Name.from("set", identifier).toLowerCamel();
    }
  }

  @Override
  public String getPathTemplateName(CollectionConfig collectionConfig) {
    return Name.from(collectionConfig.getEntityName(), "path", "template").toUpperUnderscore();
  }

  @Override
  public String getPathTemplateNameGetter(CollectionConfig collectionConfig) {
    return IdentifierNamer.NOT_IMPLEMENTED;
  }

  @Override
  public String getFormatFunctionName(CollectionConfig collectionConfig) {
    return Name.from("format", collectionConfig.getEntityName(), "name").toLowerCamel();
  }

  @Override
  public String getParseFunctionName(String var, CollectionConfig collectionConfig) {
    return Name.from("parse", var, "from", collectionConfig.getEntityName(), "name").toLowerCamel();
  }

  @Override
  public String getEntityName(CollectionConfig collectionConfig) {
    return Name.from(collectionConfig.getEntityName()).toLowerCamel();
  }

  @Override
  public String getEntityNameParamName(CollectionConfig collectionConfig) {
    return Name.from(collectionConfig.getEntityName(), "name").toLowerCamel();
  }

  @Override
  public String getParamName(String var) {
    return Name.from(var).toLowerCamel();
  }

  @Override
  public String getPageStreamingDescriptorName(Method method) {
    return IdentifierNamer.NOT_IMPLEMENTED;
  }

  @Override
  public void addPageStreamingDescriptorImports(ModelTypeTable typeTable) {
    // do nothing
  }

  @Override
  public String getMethodKey(Method method) {
    return IdentifierNamer.NOT_IMPLEMENTED;
  }

  @Override
  public String getClientConfigPath(Interface service) {
    return IdentifierNamer.NOT_IMPLEMENTED;
  }

  @Override
  public String getGrpcClientTypeName(Interface service) {
    return IdentifierNamer.NOT_IMPLEMENTED;
  }

  @Override
  public String getApiMethodName(Method method) {
    return Name.upperCamel(method.getSimpleName()).toLowerCamel();
  }

  @Override
  public String getVariableName(Field field) {
    return Name.from(field.getSimpleName()).toLowerCamel();
  }

  @Override
  public boolean shouldImportRequestObjectParamType(Field field) {
    return true;
  }

  @Override
  public String getVariableName(Name name) {
    return name.toLowerCamel();
  }

  @Override
  public List<String> getDocLines(ProtoElement element) {
    return JavaDocUtil.getJavaDocLines(DocumentationUtil.getDescription(element));
  }

  @Override
  public String getParamDocPrefix(Field field) {
    return getParamDocPrefix(getVariableName(field), null);
  }

  @Override
  public String getParamDocPrefix(String variableName, TypeRef typeRef) {
    return "@param " + variableName + " ";
  }

  @Override
  public List<String> getThrowsDocLines() {
    return Arrays.asList("@throws com.google.api.gax.grpc.ApiException if the remote call fails");
  }

  @Override
  public String getPublicAccessModifier() {
    return "public";
  }

  @Override
  public String getPrivateAccessModifier() {
    return "private";
  }

  @Override
  public Object getGrpcMethodName(Method method) {
    return method.getSimpleName();
  }
}
