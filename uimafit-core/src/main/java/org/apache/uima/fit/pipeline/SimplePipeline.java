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
package org.apache.uima.fit.pipeline;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.util.LifeCycleUtil.close;
import static org.apache.uima.fit.util.LifeCycleUtil.collectionProcessComplete;
import static org.apache.uima.fit.util.LifeCycleUtil.destroy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.CasCreationUtils;

/**
 *
 */
public final class SimplePipeline {
  private SimplePipeline() {
    // This class is not meant to be instantiated
  }

  /**
   * Run the CollectionReader and AnalysisEngines as a pipeline. After processing all CASes provided
   * by the reader, the method calls {@link AnalysisEngine#collectionProcessComplete()
   * collectionProcessComplete()} on the engines and {@link Resource#destroy() destroy()} on all
   * engines.
   * 
   * @param reader
   *          The CollectionReader that loads the documents into the CAS.
   * @param descs
   *          Primitive AnalysisEngineDescriptions that process the CAS, in order. If you have a mix
   *          of primitive and aggregate engines, then please create the AnalysisEngines yourself
   *          and call the other runPipeline method.
   * @throws UIMAException
   *           if there is a problem initializing or running the CPE.
   * @throws IOException
   *           if there is an I/O problem in the reader
   */
  public static void runPipeline(final CollectionReader reader,
          final AnalysisEngineDescription... descs) throws UIMAException, IOException {
    // Create AAE
    final AnalysisEngineDescription aaeDesc = createEngineDescription(descs);

    // Instantiate AAE
    final AnalysisEngine aae = createEngine(aaeDesc);

    // Create CAS from merged metadata
    final CAS cas = CasCreationUtils.createCas(asList(reader.getMetaData(), aae.getMetaData()));
    reader.typeSystemInit(cas.getTypeSystem());

    try {
      // Process
      while (reader.hasNext()) {
        reader.getNext(cas);
        aae.process(cas);
        cas.reset();
      }

      // Signal end of processing
      aae.collectionProcessComplete();
    } finally {
      // Destroy
      aae.destroy();
    }
  }

  /**
   * Run the CollectionReader and AnalysisEngines as a pipeline. After processing all CASes provided
   * by the reader, the method calls {@link AnalysisEngine#collectionProcessComplete()
   * collectionProcessComplete()} on the engines, {@link CollectionReader#close() close()} on the
   * reader and {@link Resource#destroy() destroy()} on the reader and all engines.
   * 
   * @param readerDesc
   *          The CollectionReader that loads the documents into the CAS.
   * @param descs
   *          Primitive AnalysisEngineDescriptions that process the CAS, in order. If you have a mix
   *          of primitive and aggregate engines, then please create the AnalysisEngines yourself
   *          and call the other runPipeline method.
   * @throws UIMAException
   *           if there is a problem initializing or running the CPE.
   * @throws IOException
   *           if there is an I/O problem in the reader
   */
  public static void runPipeline(final CollectionReaderDescription readerDesc,
          final AnalysisEngineDescription... descs) throws UIMAException, IOException {
    // Create the components
    final CollectionReader reader = createReader(readerDesc);

    try {
      // Run the pipeline
      runPipeline(reader, descs);
    } finally {
      close(reader);
      destroy(reader);
    }
  }

  /**
   * Provides a simple way to run a pipeline for a given collection reader and sequence of analysis
   * engines. After processing all CASes provided by the reader, the method calls
   * {@link AnalysisEngine#collectionProcessComplete() collectionProcessComplete()} on the engines.
   * 
   * @param reader
   *          a collection reader
   * @param engines
   *          a sequence of analysis engines
   * @throws UIMAException
   *           if there is a problem initializing or running the CPE.
   * @throws IOException
   *           if there is an I/O problem in the reader
   */
  public static void runPipeline(final CollectionReader reader, final AnalysisEngine... engines)
          throws UIMAException, IOException {
    final List<ResourceMetaData> metaData = new ArrayList<ResourceMetaData>();
    metaData.add(reader.getMetaData());
    for (AnalysisEngine engine : engines) {
      metaData.add(engine.getMetaData());
    }

    final CAS cas = CasCreationUtils.createCas(metaData);
    reader.typeSystemInit(cas.getTypeSystem());

    while (reader.hasNext()) {
      reader.getNext(cas);
      runPipeline(cas, engines);
      cas.reset();
    }

    collectionProcessComplete(engines);
  }

  /**
   * Run a sequence of {@link AnalysisEngine analysis engines} over a {@link JCas}. The result of
   * the analysis can be read from the JCas.
   * 
   * @param aCas
   *          the CAS to process
   * @param aDescs
   *          a sequence of analysis engines to run on the jCas
   * @throws ResourceInitializationException
   *           if there is a problem initializing the components
   * @throws AnalysisEngineProcessException
   *           if there is a problem during the execution of the components
   */
  public static void runPipeline(final CAS aCas, final AnalysisEngineDescription... aDescs)
          throws ResourceInitializationException, AnalysisEngineProcessException {
    // Create aggregate AE
    final AnalysisEngineDescription aaeDesc = createEngineDescription(aDescs);

    // Instantiate
    final AnalysisEngine aae = createEngine(aaeDesc);
    try {
      // Process
      aae.process(aCas);

      // Signal end of processing
      aae.collectionProcessComplete();
    } finally {
      // Destroy
      aae.destroy();
    }
  }

  /**
   * Run a sequence of {@link AnalysisEngine analysis engines} over a {@link JCas}. The result of
   * the analysis can be read from the JCas.
   * 
   * @param jCas
   *          the jCas to process
   * @param descs
   *          a sequence of analysis engines to run on the jCas
   * @throws ResourceInitializationException
   *           if there is a problem initializing the components
   * @throws AnalysisEngineProcessException
   *           if there is a problem during the execution of the components
   */
  public static void runPipeline(final JCas jCas, final AnalysisEngineDescription... descs)
          throws AnalysisEngineProcessException, ResourceInitializationException {
    runPipeline(jCas.getCas(), descs);
  }

  /**
   * Run a sequence of {@link AnalysisEngine analysis engines} over a {@link JCas}. This method does
   * not {@link AnalysisEngine#destroy() destroy} the engines or send them other events like
   * {@link AnalysisEngine#collectionProcessComplete()}. This is left to the caller.
   * 
   * @param jCas
   *          the jCas to process
   * @param engines
   *          a sequence of analysis engines to run on the jCas
   * @throws AnalysisEngineProcessException
   *           if there is a problem during the execution of the components
   */
  public static void runPipeline(final JCas jCas, final AnalysisEngine... engines)
          throws AnalysisEngineProcessException {
    for (AnalysisEngine engine : engines) {
      engine.process(jCas);
    }
  }

  /**
   * Run a sequence of {@link AnalysisEngine analysis engines} over a {@link CAS}. This method does
   * not {@link AnalysisEngine#destroy() destroy} the engines or send them other events like
   * {@link AnalysisEngine#collectionProcessComplete()}. This is left to the caller.
   * 
   * @param cas
   *          the CAS to process
   * @param engines
   *          a sequence of analysis engines to run on the jCas
   * @throws AnalysisEngineProcessException
   *           if there is a problem during the execution of the components
   */
  public static void runPipeline(final CAS cas, final AnalysisEngine... engines)
          throws AnalysisEngineProcessException {
    for (AnalysisEngine engine : engines) {
      engine.process(cas);
    }
  }

  /**
   * Iterate through the {@link JCas JCases} processed by the pipeline, allowing to access each one
   * after it has been processed.
   * 
   * @param aReader
   *          the collection reader.
   * @param aEngines
   *          the analysis engines.
   * @return an {@link Iterable}&lt;{@link JCas}&gt; which can be used in an extended for-loop.
   */
  public static JCasIterable iteratePipeline(final CollectionReaderDescription aReader,
          AnalysisEngineDescription... aEngines) {
    return new JCasIterable(aReader, aEngines);
  }
}
