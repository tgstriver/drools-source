/*
 * Copyright (c) 2020. Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.mvel.builder;

import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.compiler.BoundIdentifiers;
import org.drools.compiler.compiler.DescrBuildError;
import org.drools.compiler.lang.descr.ReturnValueRestrictionDescr;
import org.drools.compiler.rule.builder.ReturnValueBuilder;
import org.drools.compiler.rule.builder.RuleBuildContext;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.ReturnValueRestriction;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.mvel.MVELDialectRuntimeData;
import org.drools.mvel.asm.AsmUtil;
import org.drools.mvel.expr.MVELCompilationUnit;
import org.drools.mvel.expr.MVELReturnValueExpression;

public class MVELReturnValueBuilder
    implements
        ReturnValueBuilder {

    public void build(final RuleBuildContext context,
                      final BoundIdentifiers usedIdentifiers,
                      final Declaration[] previousDeclarations,
                      final Declaration[] localDeclarations,
                      final ReturnValueRestriction returnValueRestriction,
                      final ReturnValueRestrictionDescr returnValueRestrictionDescr,
                      final AnalysisResult analysis) {
        boolean typesafe = context.isTypesafe();
        try {
            MVELDialect dialect = (MVELDialect) context.getDialect( "mvel" );
            
            context.setTypesafe( ((MVELAnalysisResult)analysis).isTypesafe() );
            MVELCompilationUnit unit = dialect.getMVELCompilationUnit((String) returnValueRestrictionDescr.getContent(),
                                                                      analysis,  
                                                                      previousDeclarations, 
                                                                      localDeclarations, 
                                                                      null, 
                                                                      context,
                                                                      "drools",
                                                                      KnowledgeHelper.class,
                                                                      false,
                                                                      MVELCompilationUnit.Scope.EXPRESSION );
    
            MVELReturnValueExpression expr = new MVELReturnValueExpression( unit,
                                                                            context.getDialect().getId() );
            returnValueRestriction.setReturnValueExpression( expr );
            
            MVELDialectRuntimeData data = ( MVELDialectRuntimeData ) context.getPkg().getDialectRuntimeRegistry().getDialectData( "mvel" );
            data.addCompileable( returnValueRestriction,
                                  expr );
            
            expr.compile( data, context.getRule() );
        } catch ( final Exception e ) {
            AsmUtil.copyErrorLocation(e, context.getRuleDescr());
            context.addError( new DescrBuildError( context.getParentDescr(),
                                                          context.getRuleDescr(),
                                                          null,
                                                          "Unable to build expression for 'returnValue' : " + e.getMessage() + "'" + context.getRuleDescr().getSalience() + "'" ) );
        } finally {
            context.setTypesafe( typesafe );
        }

    }

}
