/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2020
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.context.DocumentContext;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticInfo;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.MdoRefBuilder;
import com.github._1c_syntax.bsl.parser.BSLParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.MAJOR,
  minutesToFix = 1,
  tags = {
    DiagnosticTag.DEPRECATED,
    DiagnosticTag.DESIGN
  }

)
public class DeprecatedMethodCallDiagnostic extends AbstractVisitorDiagnostic {
  public DeprecatedMethodCallDiagnostic(DiagnosticInfo info) {
    super(info);
  }

  @Override
  public ParseTree visitCallStatement(BSLParser.CallStatementContext ctx) {

    String mdoRef = MdoRefBuilder.getMdoRef(documentContext, ctx);
    if (mdoRef.isEmpty()) {
      return super.visitCallStatement(ctx);
    }

    Token methodName = getMethodName(ctx);
    if (methodName == null) {
      return super.visitCallStatement(ctx);
    }

    checkDeprecatedCall(mdoRef, methodName);

    return super.visitCallStatement(ctx);
  }


  @Override
  public ParseTree visitComplexIdentifier(BSLParser.ComplexIdentifierContext ctx) {

    String mdoRef = MdoRefBuilder.getMdoRef(documentContext, ctx);
    if (mdoRef.isEmpty()) {
      return super.visitComplexIdentifier(ctx);
    }

    Token methodName = getMethodName(ctx);
    if (methodName == null) {
      return super.visitComplexIdentifier(ctx);
    }

    checkDeprecatedCall(mdoRef, methodName);

    return super.visitComplexIdentifier(ctx);
  }

  private void checkDeprecatedCall(String mdoRef, Token methodName) {
    var documentContexts = documentContext.getServerContext().getDocuments(mdoRef);
    String methodNameText = methodName.getText();

    documentContexts.values().stream()
      .map(DocumentContext::getSymbolTree)
      .flatMap(symbolTree -> symbolTree.getMethods().stream())
      .filter(methodSymbol -> methodSymbol.isDeprecated()
        && methodSymbol.getName().equalsIgnoreCase(methodNameText))
      .findAny()
      .ifPresent(methodSymbol -> diagnosticStorage.addDiagnostic(methodName));
  }

  private static Token getMethodName(BSLParser.CallStatementContext ctx) {
    var modifiers = ctx.modifier();
    var methodName = getMethodName(ctx.accessCall());

    if (modifiers.isEmpty()) {
      return methodName;
    } else {
      return Optional.ofNullable(getMethodName(modifiers)).orElse(methodName);
    }
  }

  private static Token getMethodName(BSLParser.AccessCallContext ctx) {
    return ctx.methodCall().methodName().getStart();
  }

  private static Token getMethodName(BSLParser.ComplexIdentifierContext ctx) {
    return getMethodName(ctx.modifier());
  }

  @CheckForNull
  private static Token getMethodName(List<? extends BSLParser.ModifierContext> modifiers) {
    return modifiers.stream()
      .map(BSLParser.ModifierContext::accessCall)
      .filter(Objects::nonNull)
      .map(DeprecatedMethodCallDiagnostic::getMethodName)
      .findFirst()
      .orElse(null);
  }
}
