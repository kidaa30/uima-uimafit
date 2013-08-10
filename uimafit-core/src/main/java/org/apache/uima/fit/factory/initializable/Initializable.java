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
package org.apache.uima.fit.factory.initializable;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.xwriter.IntegerFileNamer;
import org.apache.uima.fit.component.xwriter.XWriter;
import org.apache.uima.fit.component.xwriter.XWriterFileNamer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * <p>
 * This interface provides a way of initializing a class with a {@link UimaContext}. The standard
 * use case of this interface involves situations in which a member variable is declared with an
 * interface type and the actual class that is used is decided at runtime. When the member variable
 * is instantiated, it is useful to provide it a {@code UimaContext} so that it can initialize
 * itself based on configuration parameters.
 * </p>
 * 
 * <p>
 * As an example, consider the component {@link XWriter} which has a member variable of type
 * {@link XWriterFileNamer} which is an interface that provides a way for {@code XWriter} to come up
 * with a name each file that is generated given a {@link JCas}. The default
 * {@code XWriterFileNamer} that is provided, {@link IntegerFileNamer}, provides a single
 * configuration parameter for specifying a prefix to each file name that is generated. This
 * parameter is initialized in the {@link IntegerFileNamer#initialize} method. This initialize
 * method is called because {@code XWriter} instantiates its XWriterFileNamer member variable using
 * a {@link InitializableFactory#create} method. Therefore, when XWriter is instantiated it should
 * be given a value for the configuration parameter {@link IntegerFileNamer#PARAM_PREFIX}. See the
 * unit tests for {@code XWriter} for a complete code example. Note that the implementation of
 * {@code XWriterFileNamer} does not have to implement {@code Initializable} if it has no need for
 * the initialize method.
 * </p>
 * 
 */
public interface Initializable {

  /**
   * This method will be called automatically if the implementing class is instantiated with
   * InitializableFactory.
   */
  public void initialize(UimaContext context) throws ResourceInitializationException;
}