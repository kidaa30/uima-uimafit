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


 getCoveredAnnotations() contains code adapted from the UIMA Subiterator class.
 */
package org.apache.uima.fit.util;

import static java.util.Arrays.asList;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.ByteArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.DoubleArrayFS;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.LongArrayFS;
import org.apache.uima.cas.ShortArrayFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.BooleanArray;
import org.apache.uima.jcas.cas.ByteArray;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.FloatArray;
import org.apache.uima.jcas.cas.FloatList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.IntegerList;
import org.apache.uima.jcas.cas.LongArray;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.NonEmptyFloatList;
import org.apache.uima.jcas.cas.NonEmptyIntegerList;
import org.apache.uima.jcas.cas.NonEmptyStringList;
import org.apache.uima.jcas.cas.ShortArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.TOP;

/**
 * Bridge between Java {@link Collection Collections} from different representations of collections
 * of UIMA {@link FeatureStructure FeatureStructures}.
 * 
 * @param <T>
 *          data type.
 */
public abstract class FSCollectionFactory<T extends FeatureStructure> {

  private FSCollectionFactory() {
    // No instances.
  }

  /**
   * Create a {@link Collection} of the given type of feature structures. This collection is backed
   * by the CAS, either via an {@link CAS#getAnnotationIndex(Type)} or
   * {@link FSIndexRepository#getAllIndexedFS(Type)}.
   * 
   * @param cas
   *          the CAS to select from.
   * @param type
   *          the type of feature structures to select. All sub-types are returned as well.
   * @return a {@link Collection} of the given type of feature structures backed live by the CAS.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Collection<FeatureStructure> create(CAS cas, Type type) {
    // If the type is an annotation type, we can use the annotation index, which directly
    // provides us with its size. If not, we have to use getAllIndexedFS() which we have to
    // scan from beginning to end in order to determine its size.
    TypeSystem ts = cas.getTypeSystem();
    if (ts.subsumes(cas.getAnnotationType(), type)) {
      return (Collection) create(cas.getAnnotationIndex(type));
    } else {
      return create(cas.getIndexRepository().getAllIndexedFS(type));
    }
  }

  /**
   * Convert an {@link FSIterator} to a {@link Collection}.
   * 
   * @param <T>
   *          the feature structure type
   * @param aIterator
   *          the iterator to convert.
   * @return the wrapped iterator.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static <T extends FeatureStructure> Collection<T> create(FSIterator<T> aIterator) {
    return new FSIteratorAdapter<T>(aIterator);
  }

  /**
   * Convert an {@link AnnotationIndex} to a {@link Collection}.
   * 
   * @param <T>
   *          the feature structure type
      * @param aIndex
   *          the index to convert.
   * @return the wrapped index.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static <T extends AnnotationFS> Collection<T> create(AnnotationIndex<T> aIndex) {
    return new AnnotationIndexAdapter<T>(aIndex);
  }

  /**
   * Convert an {@link ArrayFS} to a {@link Collection}.
   * 
   * @param aArray
   *          the array to convert.
   * @return a new collection containing the same feature structures as the provided array.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Collection<FeatureStructure> create(ArrayFS aArray) {
    return create(aArray, (Type) null);
  }

  /**
   * Fetch all annotations of the given type or its sub-types from the given FS array.
   * 
   * @param <T>
   *          the JCas type.
   * @param aArray
   *          the FS array
   * @param aType
   *          the JCas wrapper class.
   * @return a new collection of all feature structures of the given type.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T extends TOP> Collection<T> create(ArrayFS aArray, Class<T> aType) {
    return (Collection) create(aArray, CasUtil.getType(aArray.getCAS(), aType));
  }

  /**
   * Fetch all annotations of the given type or its sub-types from the given FS array.
   * 
   * @param aArray
   *          the FS array
   * @param aType
   *          the CAS type.
   * @return a new collection of all feature structures of the given type.
   */
  public static Collection<FeatureStructure> create(ArrayFS aArray, Type aType) {
    TypeSystem ts = aArray.getCAS().getTypeSystem();
    List<FeatureStructure> data = new ArrayList<FeatureStructure>(aArray.size());
    for (int i = 0; i < aArray.size(); i++) {
      FeatureStructure value = aArray.get(i);
      if (value != null && (aType == null || ts.subsumes(aType, value.getType()))) {
        data.add(value);
      }
    }
    return asList(data.toArray(new FeatureStructure[data.size()]));
  }

  public static ArrayFS createArrayFS(CAS aCas, Collection<? extends FeatureStructure> aCollection) {
    return fillArrayFS(aCas.createArrayFS(aCollection.size()), aCollection);
  }

  public static ArrayFS createArrayFS(CAS aCas, FeatureStructure[] aArray) {
    return fillArrayFS(aCas.createArrayFS(aArray.length), asList(aArray));
  }

  public static FSArray createFSArray(JCas aJCas, Collection<? extends FeatureStructure> aCollection) {
    return fillArrayFS(new FSArray(aJCas, aCollection.size()), aCollection);
  }

  public static FSArray createFSArray(JCas aJCas, FeatureStructure[] aArray) {
    return fillArrayFS(new FSArray(aJCas, aArray.length), asList(aArray));
  }

  public static BooleanArrayFS createBooleanArray(CAS aCas, Collection<Boolean> aCollection) {
    return fillArrayFS(aCas.createBooleanArrayFS(aCollection.size()), aCollection);
  }

  public static BooleanArrayFS createBooleanArray(CAS aCas, boolean[] aArray) {
    return fillArrayFS(aCas.createBooleanArrayFS(aArray.length), aArray);
  }

  public static BooleanArrayFS createBooleanArray(JCas aJCas, Collection<Boolean> aCollection) {
    return fillArrayFS(new BooleanArray(aJCas, aCollection.size()), aCollection);
  }

  public static BooleanArrayFS createBooleanArray(JCas aJCas, boolean[] aArray) {
    return fillArrayFS(new BooleanArray(aJCas, aArray.length), aArray);
  }

  public static ByteArrayFS createByteArray(CAS aCas, Collection<Byte> aCollection) {
    return fillArrayFS(aCas.createByteArrayFS(aCollection.size()), aCollection);
  }

  public static ByteArrayFS createByteArray(CAS aCas, byte[] aArray) {
    return fillArrayFS(aCas.createByteArrayFS(aArray.length), aArray);
  }

  public static ByteArrayFS createByteArray(JCas aJCas, Collection<Byte> aCollection) {
    return fillArrayFS(new ByteArray(aJCas, aCollection.size()), aCollection);
  }

  public static ByteArrayFS createByteArray(JCas aJCas, byte[] aArray) {
    return fillArrayFS(new ByteArray(aJCas, aArray.length), aArray);
  }

  public static DoubleArrayFS createDoubleArray(CAS aCas, Collection<Double> aCollection) {
    return fillArrayFS(aCas.createDoubleArrayFS(aCollection.size()), aCollection);
  }

  public static DoubleArrayFS createDoubleArray(CAS aCas, double[] aArray) {
    return fillArrayFS(aCas.createDoubleArrayFS(aArray.length), aArray);
  }

  public static DoubleArrayFS createDoubleArray(JCas aJCas, Collection<Double> aCollection) {
    return fillArrayFS(new DoubleArray(aJCas, aCollection.size()), aCollection);
  }

  public static DoubleArrayFS createDoubleArray(JCas aJCas, double[] aArray) {
    return fillArrayFS(new DoubleArray(aJCas, aArray.length), aArray);
  }

  public static FloatArrayFS createFloatArray(CAS aCas, Collection<Float> aCollection) {
    return fillArrayFS(aCas.createFloatArrayFS(aCollection.size()), aCollection);
  }

  public static FloatArrayFS createFloatArray(CAS aCas, float[] aArray) {
    return fillArrayFS(aCas.createFloatArrayFS(aArray.length), aArray);
  }

  public static FloatArrayFS createFloatArray(JCas aJCas, Collection<Float> aCollection) {
    return fillArrayFS(new FloatArray(aJCas, aCollection.size()), aCollection);
  }

  public static FloatArrayFS createFloatArray(JCas aJCas, float[] aArray) {
    return fillArrayFS(new FloatArray(aJCas, aArray.length), aArray);
  }

  public static IntArrayFS createIntArray(CAS aCas, Collection<Integer> aCollection) {
    return fillArrayFS(aCas.createIntArrayFS(aCollection.size()), aCollection);
  }

  public static IntArrayFS createIntArray(CAS aCas, int[] aArray) {
    return fillArrayFS(aCas.createIntArrayFS(aArray.length), aArray);
  }

  public static IntArrayFS createIntArray(JCas aJCas, Collection<Integer> aCollection) {
    return fillArrayFS(new IntegerArray(aJCas, aCollection.size()), aCollection);
  }

  public static IntArrayFS createIntArray(JCas aJCas, int[] aArray) {
    return fillArrayFS(new IntegerArray(aJCas, aArray.length), aArray);
  }

  public static LongArrayFS createLongArray(CAS aCas, Collection<Long> aCollection) {
    return fillArrayFS(aCas.createLongArrayFS(aCollection.size()), aCollection);
  }

  public static LongArrayFS createLongArray(CAS aCas, long[] aArray) {
    return fillArrayFS(aCas.createLongArrayFS(aArray.length), aArray);
  }

  public static LongArrayFS createLongArray(JCas aJCas, Collection<Long> aCollection) {
    return fillArrayFS(new LongArray(aJCas, aCollection.size()), aCollection);
  }

  public static LongArrayFS createLongArray(JCas aJCas, long[] aArray) {
    return fillArrayFS(new LongArray(aJCas, aArray.length), aArray);
  }

  public static ShortArrayFS createShortArray(CAS aCas, Collection<Short> aCollection) {
    return fillArrayFS(aCas.createShortArrayFS(aCollection.size()), aCollection);
  }

  public static ShortArrayFS createShortArray(CAS aCas, short[] aArray) {
    return fillArrayFS(aCas.createShortArrayFS(aArray.length), aArray);
  }

  public static ShortArrayFS createShortArray(JCas aJCas, Collection<Short> aCollection) {
    return fillArrayFS(new ShortArray(aJCas, aCollection.size()), aCollection);
  }

  public static ShortArrayFS createShortArray(JCas aJCas, short[] aArray) {
    return fillArrayFS(new ShortArray(aJCas, aArray.length), aArray);
  }

  public static StringArrayFS createStringArray(CAS aCas, Collection<String> aCollection) {
    return fillArrayFS(aCas.createStringArrayFS(aCollection.size()), aCollection);
  }

  public static StringArrayFS createStringArray(CAS aCas, String[] aArray) {
    return fillArrayFS(aCas.createStringArrayFS(aArray.length), aArray);
  }

  public static StringArrayFS createStringArray(JCas aJCas, Collection<String> aCollection) {
    return fillArrayFS(new StringArray(aJCas, aCollection.size()), aCollection);
  }

  public static StringArrayFS createStringArray(JCas aJCas, String[] aArray) {
    return fillArrayFS(new StringArray(aJCas, aArray.length), aArray);
  }

  public static <T extends ArrayFS> T fillArrayFS(T aArrayFs,
          Iterable<? extends FeatureStructure> aCollection) {
    int i = 0;
    for (FeatureStructure fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static ArrayFS fillArrayFS(ArrayFS aArrayFs, FeatureStructure[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static BooleanArrayFS fillArrayFS(BooleanArrayFS aArrayFs, Iterable<Boolean> aCollection) {
    int i = 0;
    for (Boolean fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static BooleanArrayFS fillArrayFS(BooleanArrayFS aArrayFs, boolean[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static ByteArrayFS fillArrayFS(ByteArrayFS aArrayFs, Iterable<Byte> aCollection) {
    int i = 0;
    for (Byte fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static ByteArrayFS fillArrayFS(ByteArrayFS aArrayFs, byte[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static DoubleArrayFS fillArrayFS(DoubleArrayFS aArrayFs, Iterable<Double> aCollection) {
    int i = 0;
    for (Double fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static DoubleArrayFS fillArrayFS(DoubleArrayFS aArrayFs, double[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static FloatArrayFS fillArrayFS(FloatArrayFS aArrayFs, Iterable<Float> aCollection) {
    int i = 0;
    for (Float fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static FloatArrayFS fillArrayFS(FloatArrayFS aArrayFs, float[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static IntArrayFS fillArrayFS(IntArrayFS aArrayFs, Iterable<Integer> aCollection) {
    int i = 0;
    for (Integer fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static IntArrayFS fillArrayFS(IntArrayFS aArrayFs, int[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static LongArrayFS fillArrayFS(LongArrayFS aArrayFs, Iterable<Long> aCollection) {
    int i = 0;
    for (Long fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static LongArrayFS fillArrayFS(LongArrayFS aArrayFs, long[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static ShortArrayFS fillArrayFS(ShortArrayFS aArrayFs, Iterable<Short> aCollection) {
    int i = 0;
    for (Short fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static ShortArrayFS fillArrayFS(ShortArrayFS aArrayFs, short[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  public static StringArrayFS fillArrayFS(StringArrayFS aArrayFs, Iterable<String> aCollection) {
    int i = 0;
    for (String fs : aCollection) {
      aArrayFs.set(i, fs);
      i++;
    }
    return aArrayFs;
  }

  public static StringArrayFS fillArrayFS(StringArrayFS aArrayFs, String[] aArray) {
    aArrayFs.copyFromArray(aArray, 0, 0, aArrayFs.size());
    return aArrayFs;
  }

  // Using TOP here because FSList is only available in the JCas.
  public static Collection<TOP> create(FSList aList) {
    return create(aList, (Type) null);
  }

  /**
   * Fetch all annotations of the given type or its sub-types from the given FS list.
   * 
   * @param <T>
   *          the JCas type.
   * @param aList
   *          the FS list
   * @param aType
   *          the JCas wrapper class.
   * @return a new collection of all feature structures of the given type.
   */
  @SuppressWarnings("unchecked")
  public static <T extends TOP> Collection<T> create(FSList aList, Class<T> aType) {
    return (Collection<T>) create(aList, CasUtil.getType(aList.getCAS(), aType));
  }

  // Using TOP here because FSList is only available in the JCas.
  public static Collection<TOP> create(FSList aList, Type type) {
    TypeSystem ts = aList.getCAS().getTypeSystem();
    List<FeatureStructure> data = new ArrayList<FeatureStructure>();
    FSList i = aList;
    while (i instanceof NonEmptyFSList) {
      NonEmptyFSList l = (NonEmptyFSList) i;
      TOP value = l.getHead();
      if (value != null && (type == null || ts.subsumes(type, value.getType()))) {
        data.add(l.getHead());
      }
      i = l.getTail();
    }

    return asList(data.toArray(new TOP[data.size()]));
  }

  public static Collection<String> create(StringList aList) {
    List<String> data = new ArrayList<String>();
    StringList i = aList;
    while (i instanceof NonEmptyStringList) {
      NonEmptyStringList l = (NonEmptyStringList) i;
      data.add(l.getHead());
      i = l.getTail();
    }

    return asList(data.toArray(new String[data.size()]));
  }

  public static Collection<Integer> create(IntegerList aList) {
    List<Integer> data = new ArrayList<Integer>();
    IntegerList i = aList;
    while (i instanceof NonEmptyIntegerList) {
      NonEmptyIntegerList l = (NonEmptyIntegerList) i;
      data.add(l.getHead());
      i = l.getTail();
    }

    return asList(data.toArray(new Integer[data.size()]));
  }

  public static Collection<Float> create(FloatList aList) {
    List<Float> data = new ArrayList<Float>();
    FloatList i = aList;
    while (i instanceof NonEmptyFloatList) {
      NonEmptyFloatList l = (NonEmptyFloatList) i;
      data.add(l.getHead());
      i = l.getTail();
    }

    return asList(data.toArray(new Float[data.size()]));
  }

  public static FSList createFSList(JCas aJCas, Collection<? extends TOP> aCollection) {
    return createFSList(aJCas.getCas(), aCollection);
  }

  public static <T extends FeatureStructure> T createFSList(CAS aCas, FeatureStructure... aValues) {
    return createFSList(aCas, asList(aValues));
  }
  
  public static <T extends FeatureStructure> T createFSList(CAS aCas,
          Collection<? extends FeatureStructure> aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_FS_LIST);

    if (aValues.size() == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_FS_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    Iterator<? extends FeatureStructure> i = aValues.iterator();
    while (i.hasNext()) {
      head.setFeatureValue(headFeature, i.next());
      if (i.hasNext()) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }


  public static FloatList createFloatList(JCas aJCas, float... aValues) {
    return createFloatList(aJCas.getCas(), aValues);
  }

  public static <T extends FeatureStructure> T createFloatList(CAS aCas, float... aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_FLOAT_LIST);

    if (aValues.length == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_FLOAT_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    int i = 0;
    while (i < aValues.length) {
      head.setFloatValue(headFeature, aValues[i]);
      i++;
      if (i < aValues.length) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }

  public static <T extends FeatureStructure> T createFloatList(CAS aCas, Collection<Float> aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_FLOAT_LIST);

    if (aValues.size() == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_FLOAT_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    Iterator<Float> i = aValues.iterator();
    while (i.hasNext()) {
      head.setFloatValue(headFeature, i.next());
      if (i.hasNext()) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }

  public static FloatList createFloatList(JCas aJCas, Collection<Float> aCollection) {
    return createFloatList(aJCas.getCas(), aCollection);
  }

  public static IntegerList createIntegerList(JCas aJCas, int... aValues) {
    return createIntegerList(aJCas.getCas(), aValues);
  }

  public static <T extends FeatureStructure> T createIntegerList(CAS aCas, int... aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_INTEGER_LIST);

    if (aValues.length == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_INTEGER_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    int i = 0;
    while (i < aValues.length) {
      head.setIntValue(headFeature, aValues[i]);
      i++;
      if (i < aValues.length) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }

  public static <T extends FeatureStructure> T createIntegerList(CAS aCas, Collection<Integer> aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_INTEGER_LIST);

    if (aValues.size() == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_INTEGER_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    Iterator<Integer> i = aValues.iterator();
    while (i.hasNext()) {
      head.setIntValue(headFeature, i.next());
      if (i.hasNext()) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }

  public static IntegerList createIntegerList(JCas aJCas, Collection<Integer> aCollection) {
    return createIntegerList(aJCas.getCas(), aCollection);
  }

  public static StringList createStringList(JCas aJCas, String... aValues) {
    return createStringList(aJCas.getCas(), aValues);
  }

  public static <T extends FeatureStructure> T createStringList(CAS aCas, String... aValues) {
    return createStringList(aCas, asList(aValues));
  }
  
  public static <T extends FeatureStructure> T createStringList(CAS aCas, Collection<String> aValues) {
    if (aValues == null) {
      return null;
    }
    
    TypeSystem ts = aCas.getTypeSystem();

    Type emptyType = ts.getType(CAS.TYPE_NAME_EMPTY_STRING_LIST);

    if (aValues.size() == 0) {
      return aCas.createFS(emptyType);
    }
    
    Type nonEmptyType = ts.getType(CAS.TYPE_NAME_NON_EMPTY_STRING_LIST);
    Feature headFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD);
    Feature tailFeature = nonEmptyType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL);

    FeatureStructure head = aCas.createFS(nonEmptyType);
    FeatureStructure list = head;
    Iterator<String> i = aValues.iterator();
    while (i.hasNext()) {
      head.setStringValue(headFeature, i.next());
      if (i.hasNext()) {
        FeatureStructure tail = aCas.createFS(nonEmptyType);
        head.setFeatureValue(tailFeature, tail);
        head = tail;
      } else {
        head.setFeatureValue(tailFeature, aCas.createFS(emptyType));
      }
    }

    return (T) list;
  }

  public static StringList createStringList(JCas aJCas, Collection<String> aCollection) {
    return createStringList(aJCas.getCas(), aCollection);
  }

  private static class FSIteratorAdapter<T extends FeatureStructure> extends AbstractCollection<T> {
    private int sizeCache = -1;

    private final FSIterator<T> index;

    public FSIteratorAdapter(final FSIterator<T> aIterator) {
      index = aIterator.copy();
      index.moveToFirst();
    }

    @Override
    public Iterator<T> iterator() {
      return index.copy();
    }

    @Override
    public int size() {
      // Unfortunately FSIterator does not expose the sizes of its internal collection,
      // neither the current position although FSIteratorAggregate has a private field
      // with that information.
      if (sizeCache == -1) {
        synchronized (this) {
          if (sizeCache == -1) {
            FSIterator<T> clone = index.copy();
            clone.moveToFirst();
            sizeCache = 0;
            while (clone.isValid()) {
              sizeCache++;
              clone.moveToNext();
            }
          }
        }
      }

      return sizeCache;
    }
  }

  private static class AnnotationIndexAdapter<T extends AnnotationFS> extends AbstractCollection<T> {
    private final AnnotationIndex<T> index;

    public AnnotationIndexAdapter(AnnotationIndex<T> aIndex) {
      index = aIndex;
    }

    @Override
    public Iterator<T> iterator() {
      return index.iterator();
    }

    @Override
    public int size() {
      return index.size();
    }
  }
}
