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
package org.apache.uima.fit.factory;

import org.apache.uima.fit.util.ReflectionUtil;
import org.apache.uima.resource.metadata.ResourceMetaData;

public final class ResourceMetaDataFactory {

  private ResourceMetaDataFactory() {
    // This class is not meant to be instantiated
  }

  /**
   * Adds meta data from a {@link org.apache.uima.fit.descriptor.ResourceMetaData} annotation to the
   * given meta data object if such an annotation is present on the component class. If no
   * annotation is present, default values are be added.
   * 
   * @param aMetaData
   *          the meta data object to configure.
   * @param aComponentClass
   *          the class that may carry the {@link org.apache.uima.fit.descriptor.ResourceMetaData}
   *          annotation
   */
  public static void configureResourceMetaData(ResourceMetaData aMetaData, Class<?> aComponentClass) {
    org.apache.uima.fit.descriptor.ResourceMetaData componentAnno = ReflectionUtil
            .getInheritableAnnotation(org.apache.uima.fit.descriptor.ResourceMetaData.class,
                    aComponentClass);

    if (componentAnno == null) {
      // Default handling if no annotation is present.
      aMetaData.setCopyright(getDefaultCopyright(aComponentClass));
      aMetaData.setDescription(getDefaultDescription(aComponentClass));
      aMetaData.setName(getDefaultName(aComponentClass));
      aMetaData.setVendor(getDefaultVendor(aComponentClass));
      aMetaData.setVersion(getDefaultVersion(aComponentClass));
    } else {
      // If annotation is present, use it
      // Annotation values cannot be null, but we want to avoid empty strings in the meta data,
      // thus we set to null when the value is empty.
      aMetaData.setCopyright(emptyAsNull(componentAnno.copyright()));
      aMetaData.setDescription(emptyAsNull(componentAnno.description()));
      aMetaData.setName(emptyAsNull(componentAnno.name()));
      aMetaData.setVendor(emptyAsNull(componentAnno.vendor()));
      aMetaData.setVersion(emptyAsNull(componentAnno.version()));
    }
  }
  
  /**
   * Used when the version of a component is unknown.
   */
  private static final String DEFAULT_VERSION = "unknown";
  
  /**
   * Used when the description of a component is unknown.
   */
  private static final String DEFAULT_DESCRIPTION = "Descriptor automatically generated by uimaFIT";

  /**
   * Get the default copyright of a component class.
   */
  public static String getDefaultCopyright(Class<?> aComponentClass)
  {
    // rec 2013-01-27: Basically just here for completeness - no idea where to one could obtain
    // a copyright information for the class. Possibly from some LICENSE file in the JAR which
    // contains the class.
    return null;
  }

  /**
   * Get the default version of a component class.
   */
  public static String getDefaultVersion(Class<?> aComponentClass)
  {
    // TODO This method could try to obtain a version from the package of the component
    // aComponentClass.getPackage().getImplementationVersion()
    return DEFAULT_VERSION;
  }

  /**
   * Get the default description of a component class.
   */
  public static String getDefaultDescription(Class<?> aComponentClass)
  {
    return DEFAULT_DESCRIPTION;
  }
  
  /**
   * Get the default vendor of a component class.
   * 
   * @return the package name of the component, if the component is in a package, otherwise
   *         {@code null}.
   */
  public static String getDefaultVendor(Class<?> aComponentClass)
  {
    // TODO This method could try to obtain a vendor from the package of the component
    // aComponentClass.getPackage().getImplementationVendor()
    if (aComponentClass.getPackage() != null) {
      return aComponentClass.getPackage().getName();
    }
    else {
      return null;
    }
  }
  
  /**
   * Get the default name of a component class.
   * 
   * @return the fully qualified name of the class.
   */
  public static String getDefaultName(Class<?> aComponentClass)
  {
    return aComponentClass.getName();
  }
  
  private static String emptyAsNull(String aString) {
    if (aString == null || aString.length() == 0) {
      return null;
    }
    else {
      return aString;
    }
  }
}
