/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.uima.fit.maven.javadoc;

import org.apache.uima.fit.maven.util.Util;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentDescriptionExtractorTest {

  @Test
  public void test() throws Exception {
    // Create the Java parser and parse the source code into an abstract syntax tree
    CompilationUnit result = Util.parseSource("src/test/resources/TestComponent.java", "UTF-8");

    ComponentDescriptionExtractor ex = new ComponentDescriptionExtractor(
            "some.test.mypackage.TestComponent");
    result.accept(ex);

    assertNotNull(ex.getJavadoc());

    JavadocTextExtractor textEx = new JavadocTextExtractor();
    ex.getJavadoc().accept(textEx);

    assertEquals("A test component used to test JavadocTextExtractor .", textEx.getText());
  }
}