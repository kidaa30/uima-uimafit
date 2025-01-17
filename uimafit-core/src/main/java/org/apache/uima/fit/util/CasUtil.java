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

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.Subiterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Utility methods for convenient access to the {@link CAS}.
 * 
 */
public final class CasUtil {
  /**
   * Package name of JCas wrapper classes built into UIMA.
   */
  public static final String UIMA_BUILTIN_JCAS_PREFIX = "org.apache.uima.jcas.";

  private CasUtil() {
    // No instances
  }
  
  /**
   * Get an iterator over the given feature structures type.
   * 
   * @param <T>
   *          the JCas type.
   * @param cas
   *          a CAS.
   * @param type
   *          a type.
   * @return a return value.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  @SuppressWarnings("unchecked")
  public static <T extends FeatureStructure> Iterator<T> iteratorFS(CAS cas, Type type) {
    return (Iterator<T>) FSCollectionFactory.create(cas, type).iterator();
  }

  /**
   * Get an iterator over the given annotation type.
   * 
   * @param <T>
   *          the JCas type.
   * @param cas
   *          a CAS.
   * @param type
   *          a type.
   * @return a return value.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  @SuppressWarnings("unchecked")
  public static <T extends AnnotationFS> Iterator<T> iterator(CAS cas, Type type) {
    return ((AnnotationIndex<T>) cas.getAnnotationIndex(type)).iterator();
  }

  /**
   * Get the CAS type for the given JCas wrapper class.
   * 
   * @param cas
   *          the CAS hosting the type system.
   * @param type
   *          the JCas wrapper class.
   * @return the CAS type.
   */
  public static Type getType(CAS cas, Class<?> type) {
    return getType(cas, type.getName());
  }

  /**
   * Get the CAS type for the given name.
   * 
   * @param aCas
   *          the CAS hosting the type system.
   * @param aTypename
   *          the fully qualified type name.
   * @return the CAS type.
   */
  public static Type getType(CAS aCas, String aTypename) {
    String typeName = aTypename;
    if (typeName.startsWith(UIMA_BUILTIN_JCAS_PREFIX)) {
      typeName = "uima." + typeName.substring(UIMA_BUILTIN_JCAS_PREFIX.length());
    }
    else if (FeatureStructure.class.getName().equals(aTypename)) {
      typeName = CAS.TYPE_NAME_TOP;
    }
    else if (AnnotationFS.class.getName().equals(aTypename)) {
      typeName = CAS.TYPE_NAME_ANNOTATION;
    }
    final Type type = aCas.getTypeSystem().getType(typeName);
    if (type == null) {
      throw new IllegalArgumentException("Undeclared type [" + aTypename + "]");
    }
    return type;
  }

  /**
   * Get the CAS type for the given JCas wrapper class making sure it is or inherits from
   * {@link Annotation}.
   * 
   * @param aCas
   *          the CAS hosting the type system.
   * @param aJCasClass
   *          the JCas wrapper class.
   * @return the CAS type.
   */
  public static Type getAnnotationType(CAS aCas, Class<?> aJCasClass) {
    final Type type = getType(aCas, aJCasClass);
    if (!aCas.getTypeSystem().subsumes(aCas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + aJCasClass.getName()
              + "] is not an annotation type");
    }
    return type;
  }

  /**
   * Get the CAS type for the given name making sure it is or inherits from Annotation.
   * 
   * @param aCas
   *          the CAS hosting the type system.
   * @param aTypeName
   *          the fully qualified type name.
   * @return the CAS type.
   */
  public static Type getAnnotationType(CAS aCas, String aTypeName) {
    Type type = getType(aCas, aTypeName);
    if (!aCas.getTypeSystem().subsumes(aCas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + aTypeName + "] is not an annotation type");
    }
    return type;
  }

  /**
   * Convenience method to iterator over all feature structures of a given type.
   * 
   * @param array
   *          features structure array.
   * @param type
   *          the type.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Collection<FeatureStructure> selectFS(ArrayFS array, Type type) {
    return FSCollectionFactory.create(array, type);
  }

  /**
   * Convenience method to iterator over all annotations of a given type.
   * 
   * @param array
   *          features structure array.
   * @param type
   *          the type.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Collection<AnnotationFS> select(ArrayFS array, Type type) {
    final CAS cas = array.getCAS();
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }
    return (Collection) FSCollectionFactory.create(array, type);
  }

  /**
   * Convenience method to iterator over all features structures.
   * 
   * @param aCas
   *          the CAS hosting the type system.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Collection<FeatureStructure> selectAllFS(final CAS aCas) {
    return selectFS(aCas, getType(aCas, CAS.TYPE_NAME_TOP));
  }

  /**
   * Convenience method to iterator over all feature structures of a given type.
   * 
   * @param cas
   *          the CAS containing the type system.
   * @param type
   *          the type.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Collection<FeatureStructure> selectFS(final CAS cas, final Type type) {
    return FSCollectionFactory.create(cas, type);
  }

  /**
   * Convenience method to iterator over all annotations.
   * 
   * @param aCas
   *          the CAS hosting the type system.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Collection<AnnotationFS> selectAll(final CAS aCas) {
    return select(aCas, getType(aCas, CAS.TYPE_NAME_ANNOTATION));
  }

  /**
   * Convenience method to iterator over all annotations of a given type.
   * 
   * @param cas
   *          the CAS containing the type system.
   * @param type
   *          the type.
   * @return A collection of the selected type.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Collection<AnnotationFS> select(final CAS cas, final Type type) {
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }
    return (Collection) FSCollectionFactory.create(cas.getAnnotationIndex(type));
  }
  
  /**
   * Get all annotations of the given type at the specified offsets.
   * 
   * @param aCas
   *          the CAS containing the annotations.
   * @param aType
   *          the type of annotations to fetch.
   * @param aBegin
   *          the begin offset.
   * @param aEnd
   *          the end offset.
   * @return the annotations at the specified offsets.
   */
  public static List<AnnotationFS> selectAt(final CAS aCas, final Type aType, int aBegin, int aEnd) {
    List<AnnotationFS> list = new ArrayList<AnnotationFS>();
    FSIterator<AnnotationFS> it = aCas.getAnnotationIndex(aType).iterator();

    // Skip annotations whose start is before the start parameter.
    while (it.isValid() && (it.get()).getBegin() < aBegin) {
      it.moveToNext();
    }

    // Skip annotations whose end is after the end parameter.
    while (it.isValid() && (it.get()).getEnd() > aEnd) {
      it.moveToNext();
    }
    
    while (it.isValid()) {
      AnnotationFS a = it.get();
      // If the offsets do not match the specified offets, we're done
      if (a.getBegin() != aBegin && a.getEnd() != aEnd) {
        break;
      }
      it.moveToNext();
      list.add(a);
    }
    
    return list;
  }

  /**
   * Get the single instance of the specified type from the CAS at the given offsets.
   * 
   * @param aCas
   *          the CAS containing the annotations.
   * @param aType
   *          the type of annotations to fetch.
   * @param aBegin
   *          the begin offset.
   * @param aEnd
   *          the end offset.
   * @return the single annotation at the specified offsets.
   */
  public static AnnotationFS selectSingleAt(final CAS aCas, final Type aType, int aBegin, int aEnd) {
    List<AnnotationFS> list = selectAt(aCas, aType, aBegin, aEnd);

    if (list.isEmpty()) {
      throw new IllegalArgumentException("CAS does not contain any [" + aType.getName() + "] at ["
              + aBegin + "," + aEnd + "]");
    }
    
    if (list.size() > 1) {
      throw new IllegalArgumentException("CAS contains more than one [" + aType.getName()
              + "] at [" + aBegin + "," + aEnd + "]");
    }

    return list.get(0);
  }
  
  /**
   * Get a list of annotations of the given annotation type located between two annotations. Does
   * not use subiterators and does not respect type priorities. Zero-width annotations what lie on
   * the borders are included in the result, e.g. if the boundary annotations are [1..2] and [2..3]
   * then an annotation [2..2] is returned. If there is a non-zero overlap between the boundary
   * annotations, the result is empty. The method properly handles cases where the second boundary
   * annotations occurs before the first boundary annotation by switching their roles.
   * 
   * @param type
   *          a UIMA type.
   * @param ann1
   *          the first boundary annotation.
   * @param ann2
   *          the second boundary annotation.
   * @return a return value.
   * @see Subiterator
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectBetween(final Type type, final AnnotationFS ann1,
          final AnnotationFS ann2) {
    return selectBetween(ann1.getView(), type, ann1, ann2);
  }

  /**
   * Get a list of annotations of the given annotation type located between two annotations. Does
   * not use subiterators and does not respect type priorities. Zero-width annotations what lie on
   * the borders are included in the result, e.g. if the boundary annotations are [1..2] and [2..3]
   * then an annotation [2..2] is returned. If there is a non-zero overlap between the boundary
   * annotations, the result is empty. The method properly handles cases where the second boundary
   * annotations occurs before the first boundary annotation by switching their roles.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param ann1
   *          the first boundary annotation.
   * @param ann2
   *          the second boundary annotation.
   * @return a return value.
   * @see Subiterator
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectBetween(final CAS cas, final Type type,
          final AnnotationFS ann1, final AnnotationFS ann2) {
    AnnotationFS left;
    AnnotationFS right;
    if (ann1.getEnd() > ann2.getBegin()) {
      left = ann2;
      right = ann1;
    } else {
      left = ann1;
      right = ann2;
    }

    int begin = left.getEnd();
    int end = right.getBegin();

    List<AnnotationFS> list = new ArrayList<AnnotationFS>();
    FSIterator<AnnotationFS> it = cas.getAnnotationIndex(type).iterator();

    // Try to seek the insertion point.
    it.moveTo(left);

    // If the insertion point is beyond the index, move back to the last.
    if (!it.isValid()) {
      it.moveToLast();
      if (!it.isValid()) {
        return list;
      }
    }

    // Ignore type priorities by seeking to the first that has the same begin
    boolean moved = false;
    while (it.isValid() && (it.get()).getBegin() >= begin) {
      it.moveToPrevious();
      moved = true;
    }

    // If we moved, then we are now on one starting before the requested begin, so we have to
    // move one ahead.
    if (moved) {
      it.moveToNext();
    }

    // If we managed to move outside the index, start at first.
    if (!it.isValid()) {
      it.moveToFirst();
    }

    // Skip annotations whose start is before the start parameter.
    while (it.isValid() && (it.get()).getBegin() < begin) {
      it.moveToNext();
    }

    boolean strict = true;
    while (it.isValid()) {
      AnnotationFS a = it.get();
      // If the start of the current annotation is past the end parameter, we're done.
      if (a.getBegin() > end) {
        break;
      }
      it.moveToNext();
      if (strict && a.getEnd() > end) {
        continue;
      }

      assert (a.getBegin() >= left.getEnd()) : "Illegal begin " + a.getBegin() + " in [" + begin
              + ".." + end + "]";

      assert (a.getEnd() <= right.getBegin()) : "Illegal end " + a.getBegin() + " in [" + begin
              + ".." + end + "]";

      if (!a.equals(left) && !a.equals(right)) {
        list.add(a);
      }
    }

    return unmodifiableList(list);
  }

  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations of the given type to find the covered annotations. Does not use
   * subiterators and does not respect type prioritites. Was adapted from {@link Subiterator}. Uses
   * the same approach except that type priorities are ignored. 
   * <p>
   * The covering annotation is never returned itself, even if it is of the queried-for type or a
   * subtype of that type.
   * 
   * @param type
   *          a UIMA type.
   * @param coveringAnnotation
   *          the covering annotation.
   * @return a return value.
   * @see Subiterator
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovered(Type type, AnnotationFS coveringAnnotation) {
    return selectCovered(coveringAnnotation.getView(), type, coveringAnnotation);
  }

  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations of the given type to find the covered annotations. Does not use
   * subiterators and does not respect type prioritites. Was adapted from {@link Subiterator}. Uses
   * the same approach except that type priorities are ignored.
   * <p>
   * The covering annotation is never returned itself, even if it is of the queried-for type or a
   * subtype of that type.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param coveringAnnotation
   *          the covering annotation.
   * @return a return value.
   * @see Subiterator
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovered(CAS cas, Type type, AnnotationFS coveringAnnotation) {
    int begin = coveringAnnotation.getBegin();
    int end = coveringAnnotation.getEnd();

    List<AnnotationFS> list = new ArrayList<AnnotationFS>();
    FSIterator<AnnotationFS> it = cas.getAnnotationIndex(type).iterator();

    // Try to seek the insertion point.
    it.moveTo(coveringAnnotation);

    // If the insertion point is beyond the index, move back to the last.
    if (!it.isValid()) {
      it.moveToLast();
      if (!it.isValid()) {
        return list;
      }
    }

    // Ignore type priorities by seeking to the first that has the same begin
    boolean moved = false;
    while (it.isValid() && (it.get()).getBegin() >= begin) {
      it.moveToPrevious();
      moved = true;
    }

    // If we moved, then we are now on one starting before the requested begin, so we have to
    // move one ahead.
    if (moved) {
      it.moveToNext();
    }

    // If we managed to move outside the index, start at first.
    if (!it.isValid()) {
      it.moveToFirst();
    }

    // Skip annotations whose start is before the start parameter.
    while (it.isValid() && (it.get()).getBegin() < begin) {
      it.moveToNext();
    }

    boolean strict = true;
    while (it.isValid()) {
      AnnotationFS a = it.get();
      // If the start of the current annotation is past the end parameter, we're done.
      if (a.getBegin() > end) {
        break;
      }
      it.moveToNext();
      if (strict && a.getEnd() > end) {
        continue;
      }

      assert (a.getBegin() >= coveringAnnotation.getBegin()) : "Illegal begin " + a.getBegin()
              + " in [" + coveringAnnotation.getBegin() + ".." + coveringAnnotation.getEnd() + "]";

      assert (a.getEnd() <= coveringAnnotation.getEnd()) : "Illegal end " + a.getEnd() + " in ["
              + coveringAnnotation.getBegin() + ".." + coveringAnnotation.getEnd() + "]";

      if (!a.equals(coveringAnnotation)) {
        list.add(a);
      }
    }

    return unmodifiableList(list);
  }

  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations of the given type to find the covered annotations. Does not use
   * subiterators and does not respect type prioritites. Was adapted from {@link Subiterator}. Uses
   * the same approach except that type priorities are ignored.
   * <p>
   * <b>Note:</b> this is significantly slower than using
   * {@link #selectCovered(CAS, Type, AnnotationFS)}. It is possible to use
   * {@code  selectCovered(cas, type, new Annotation(jCas, int, int))}, but that will allocate memory
   * in the jCas for the new annotation. If you do that repeatedly many times, memory may fill up.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param begin
   *          begin offset.
   * @param end
   *          end offset.
   * @return a return value.
   * @see Subiterator
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovered(CAS cas, Type type, int begin, int end) {

    List<AnnotationFS> list = new ArrayList<AnnotationFS>();
    FSIterator<AnnotationFS> it = cas.getAnnotationIndex(type).iterator();

    // Skip annotations whose start is before the start parameter.
    while (it.isValid() && (it.get()).getBegin() < begin) {
      it.moveToNext();
    }

    boolean strict = true;
    while (it.isValid()) {
      AnnotationFS a = it.get();
      // If the start of the current annotation is past the end parameter, we're done.
      if (a.getBegin() > end) {
        break;
      }
      it.moveToNext();
      if (strict && a.getEnd() > end) {
        continue;
      }

      assert (a.getBegin() >= begin) : "Illegal begin " + a.getBegin() + " in [" + begin + ".."
              + end + "]";

      assert (a.getEnd() <= end) : "Illegal end " + a.getEnd() + " in [" + begin + ".." + end + "]";

      list.add(a);
    }

    return list;
  }

  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations to find the covering annotations.
   * 
   * <p>
   * <b>Note:</b> this is <b>REALLY SLOW!</b> You don't want to use this. Instead, consider using
   * {@link #indexCovering(CAS, Type, Type)} or a {@link ContainmentIndex}.
   * @param type
   *          a UIMA type.
   * @param coveredAnnotation
   *          the covered annotation.
   * 
   * @return a return value.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovering(Type type, AnnotationFS coveredAnnotation) {

    return selectCovering(coveredAnnotation.getView(), type, coveredAnnotation.getBegin(),
            coveredAnnotation.getEnd());
  }

  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations to find the covering annotations.
   * 
   * <p>
   * <b>Note:</b> this is <b>REALLY SLOW!</b> You don't want to use this. Instead, consider using
   * {@link #indexCovering(CAS, Type, Type)} or a {@link ContainmentIndex}.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param coveredAnnotation
   *          the covered annotation.
   * @return a return value.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovering(CAS cas, Type type, AnnotationFS coveredAnnotation) {

    return selectCovering(cas, type, coveredAnnotation.getBegin(), coveredAnnotation.getEnd());
  }
  
  /**
   * Get a list of annotations of the given annotation type constraint by a certain annotation.
   * Iterates over all annotations to find the covering annotations.
   * 
   * <p>
   * <b>Note:</b> this is <b>REALLY SLOW!</b> You don't want to use this. Instead, consider using
   * {@link #indexCovering(CAS, Type, Type)} or a {@link ContainmentIndex}.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param begin
   *          begin offset.
   * @param end
   *          end offset.
   * @return a return value.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectCovering(CAS cas, Type type, int begin, int end) {

    TypeSystem ts = cas.getTypeSystem();
    List<AnnotationFS> list = new ArrayList<AnnotationFS>();
    FSIterator<AnnotationFS> iter = cas.getAnnotationIndex().iterator();
    while (iter.hasNext()) {
      AnnotationFS a = iter.next();
      if ((a.getBegin() <= begin) && (a.getEnd() >= end)
              && ((type == null) || (ts.subsumes(type, a.getType())))) {
        list.add(a);
      }
    }
    return list;
  }

  /**
   * Create an index for quickly lookup up the annotations covering a particular annotation. This is
   * preferable to using {@link #selectCovering(CAS, Type, int, int)} because the overhead of
   * scanning the CAS occurs only when the index is build. Subsequent lookups to the index are fast.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          type to create the index for - this is used in lookups.
   * @param coveringType
   *          type of covering annotations.
   * @return the index.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Map<AnnotationFS, Collection<AnnotationFS>> indexCovering(CAS cas, Type type,
          Type coveringType) {
    Map<AnnotationFS, Collection<AnnotationFS>> index = new HashMap<AnnotationFS, Collection<AnnotationFS>>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Collection<AnnotationFS> get(Object paramObject) {
        Collection<AnnotationFS> res = super.get(paramObject);
        if (res == null) {
          return emptyList();
        } else {
          return res;
        }
      }
    };
    for (AnnotationFS s : select(cas, coveringType)) {
      for (AnnotationFS u : selectCovered(cas, type, s)) {
        Collection<AnnotationFS> c = index.get(u);
        if (c == EMPTY_LIST) {
          c = new LinkedList<AnnotationFS>();
          index.put(u, c);
        }
        c.add(s);
      }
    }
    return unmodifiableMap(index);
  }

  /**
   * Create an index for quickly lookup up the annotations covered by a particular annotation. This
   * is preferable to using {@link #selectCovered(CAS, Type, int, int)} because the overhead of
   * scanning the CAS occurs only when the index is build. Subsequent lookups to the index are fast.
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          type to create the index for - this is used in lookups.
   * @param coveredType
   *          type of covering annotations.
   * @return the index.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static Map<AnnotationFS, Collection<AnnotationFS>> indexCovered(CAS cas, Type type,
          Type coveredType) {
    Map<AnnotationFS, Collection<AnnotationFS>> index = new HashMap<AnnotationFS, Collection<AnnotationFS>>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Collection<AnnotationFS> get(Object paramObject) {
        Collection<AnnotationFS> res = super.get(paramObject);
        if (res == null) {
          return emptyList();
        } else {
          return res;
        }
      }
    };
    for (AnnotationFS s : select(cas, type)) {
      for (AnnotationFS u : selectCovered(cas, coveredType, s)) {
        Collection<AnnotationFS> c = index.get(s);
        if (c == EMPTY_LIST) {
          c = new LinkedList<AnnotationFS>();
          index.put(s, c);
        }
        c.add(u);
      }
    }
    return unmodifiableMap(index);
  }

  /**
   * This method exists simply as a convenience method for unit testing. It is not very efficient
   * and should not, in general be used outside the context of unit testing.
   * 
   * @param cas
   *          a CAS containing the annotation.
   * @param type
   *          a UIMA type.
   * @param index
   *          this can be either positive (0 corresponds to the first annotation of a type) or
   *          negative (-1 corresponds to the last annotation of a type.)
   * @return an annotation of the given type
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static AnnotationFS selectByIndex(CAS cas, Type type, int index) {
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }
    FSIterator<AnnotationFS> i = cas.getAnnotationIndex(type).iterator();
    int n = index;
    i.moveToFirst();
    if (n > 0) {
      while (n > 0 && i.isValid()) {
        i.moveToNext();
        n--;
      }
    }
    if (n < 0) {
      i.moveToLast();
      while (n < -1 && i.isValid()) {
        i.moveToPrevious();
        n++;
      }
    }

    return i.isValid() ? i.get() : null;
  }

  /**
   * Get the single instance of the specified type from the JCas.
   * 
   * @param cas
   *          a CAS containing the annotation.
   * @param type
   *          a UIMA type.
   * @return the single instance of the given type. throws IllegalArgumentException if not exactly
   *         one instance if the given type is present.
   */
  public static FeatureStructure selectSingle(CAS cas, Type type) {
    FSIterator<FeatureStructure> iterator = cas.getIndexRepository().getAllIndexedFS(type);

    if (!iterator.hasNext()) {
      throw new IllegalArgumentException("CAS does not contain any [" + type.getName() + "]");
    }

    FeatureStructure result = iterator.next();

    if (iterator.hasNext()) {
      throw new IllegalArgumentException("CAS contains more than one [" + type.getName() + "]");
    }

    return result;
  }

  /**
   * Return an annotation preceding or following of a given reference annotation.
   * 
   * @param type
   *          a type.
   * @param annotation
   *          anchor annotation
   * @param index
   *          relative position to access. A negative value selects a preceding annotation while a
   *          positive number selects a following annotation.
   * @return the addressed annotation.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static AnnotationFS selectSingleRelative(Type type, AnnotationFS annotation, int index) {
    return selectSingleRelative(annotation.getView(), type, annotation, index);
  }

  /**
   * Return an annotation preceding or following of a given reference annotation.
   * 
   * @param cas
   *          a CAS containing the annotation.
   * @param type
   *          a type.
   * @param annotation
   *          anchor annotation
   * @param index
   *          relative position to access. A negative value selects a preceding annotation while a
   *          positive number selects a following annotation.
   * @return the addressed annotation.
   * @throws IndexOutOfBoundsException
   *           if the relative index points beyond the type index bounds.
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static AnnotationFS selectSingleRelative(CAS cas, Type type, AnnotationFS annotation,
          int index) {
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }

    // move to first previous annotation
    FSIterator<AnnotationFS> itr = cas.getAnnotationIndex(type).iterator();
    itr.moveTo(annotation);

    if (index < 0) {
      // If the insertion point is beyond the index, move back to the last.
      if (!itr.isValid()) {
        itr.moveToLast();
        if (!itr.isValid()) {
          throw new IndexOutOfBoundsException("Reached end of index while seeking.");
        }
      }

      // No need to do additional seeks here (as done in selectCovered) because the current method
      // does not have to worry about type priorities - it never returns annotations that have
      // the same offset as the reference annotation.

      // make sure we're past the beginning of the reference annotation
      while (itr.isValid() && itr.get().getEnd() > annotation.getBegin()) {
        itr.moveToPrevious();
      }

      for (int i = 0; i < (-index - 1) && itr.isValid(); ++i, itr.moveToPrevious()) {
        // Seeking
      }

      if (!itr.isValid()) {
        throw new IndexOutOfBoundsException("Reached end of index while seeking.");
      } else {
        return itr.get();
      }
    } else if (index > 0) {
      // When seeking forward, there is no need to check if the insertion point is beyond the
      // index. If it was, there would be nothing beyond it that could be found and returned.
      // The moveTo operation also does not yield an iterator being invalid because it points
      // *before the first* index entry, at max it points *to the first* index entry, so this
      // case also does not need to be handled.
      
      // No need to do additional seeks here (as done in selectCovered) because the current method
      // does not have to worry about type priorities - it never returns annotations that have
      // the same offset as the reference annotation.
      
      // make sure we're past the end of the reference annotation
      while (itr.isValid() && itr.get().getBegin() < annotation.getEnd()) {
        itr.moveToNext();
      }

      for (int i = 0; i < (index - 1) && itr.isValid(); ++i, itr.moveToPrevious()) {
        // Seeking
      }

      if (!itr.isValid()) {
        throw new IndexOutOfBoundsException("Reached end of index while seeking.");
      } else {
        return itr.get();
      }
    } else {
      return annotation;
    }
  }

  /**
   * Returns the n annotations preceding the given annotation
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param annotation
   *          anchor annotation
   * @param count
   *          number of annotations to collect
   * @return List of aType annotations preceding anchor annotation
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectPreceding(CAS cas, Type type, AnnotationFS annotation,
          int count) {
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }

    List<AnnotationFS> precedingAnnotations = new LinkedList<AnnotationFS>();

    // Seek annotation in index
    FSIterator<AnnotationFS> itr = cas.getAnnotationIndex(type).iterator();
    itr.moveTo(annotation);
    
    // If the insertion point is beyond the index, move back to the last.
    if (!itr.isValid()) {
      itr.moveToLast();
      if (!itr.isValid()) {
        return precedingAnnotations;
      }
    }

    // No need to do additional seeks here (as done in selectCovered) because the current method
    // does not have to worry about type priorities - it never returns annotations that have
    // the same offset as the reference annotation.
    
    // make sure we're past the beginning of the reference annotation
    while (itr.isValid() && itr.get().getEnd() > annotation.getBegin()) {
      itr.moveToPrevious();
    }

    // add annotations from the iterator into the result list
    for (int i = 0; i < count && itr.isValid(); i++, itr.moveToPrevious()) {
      precedingAnnotations.add(itr.get());
    }

    // return in correct order
    Collections.reverse(precedingAnnotations);
    return precedingAnnotations;
  }

  /**
   * Returns the n annotations following the given annotation
   * 
   * @param cas
   *          a CAS.
   * @param type
   *          a UIMA type.
   * @param annotation
   *          anchor annotation
   * @param count
   *          number of annotations to collect
   * @return List of aType annotations following anchor annotation
   * @see <a href="package-summary.html#SortOrder">Order of selected feature structures</a>
   */
  public static List<AnnotationFS> selectFollowing(CAS cas, Type type, AnnotationFS annotation,
          int count) {
    if (!cas.getTypeSystem().subsumes(cas.getAnnotationType(), type)) {
      throw new IllegalArgumentException("Type [" + type.getName() + "] is not an annotation type");
    }

    // Seek annotation in index
    FSIterator<AnnotationFS> itr = cas.getAnnotationIndex(type).iterator();
    itr.moveTo(annotation);

    // When seeking forward, there is no need to check if the insertion point is beyond the
    // index. If it was, there would be nothing beyond it that could be found and returned.
    // The moveTo operation also does not yield an iterator being invalid because it points
    // *before the first* index entry, at max it points *to the first* index entry, so this
    // case also does not need to be handled.

    // No need to do additional seeks here (as done in selectCovered) because the current method
    // does not have to worry about type priorities - it never returns annotations that have
    // the same offset as the reference annotation.

    // make sure we're past the end of the reference annotation
    while (itr.isValid() && itr.get().getBegin() < annotation.getEnd()) {
      itr.moveToNext();
    }

    // add annotations from the iterator into the result list
    List<AnnotationFS> followingAnnotations = new LinkedList<AnnotationFS>();
    for (int i = 0; i < count && itr.isValid(); i++, itr.moveToNext()) {
      followingAnnotations.add(itr.get());
    }
    return followingAnnotations;
  }

  /**
   * Convenience method to get the specified view or a default view if the requested view does not
   * exist. The default can also be {@code null}.
   * 
   * @param cas
   *          a CAS
   * @param viewName
   *          the requested view.
   * @param fallback
   *          the default view if the requested view does not exist.
   * @return the requested view or the default if the requested view does not exist.
   */
  public static CAS getView(CAS cas, String viewName, CAS fallback) {
    CAS view;
    try {
      view = cas.getView(viewName);
    } catch (CASRuntimeException e) {
      // use fall-back view instead
      view = fallback;
    }
    return view;
  }

  /**
   * Convenience method to get the specified view or create a new view if the requested view does
   * not exist.
   * 
   * @param cas
   *          a CAS
   * @param viewName
   *          the requested view.
   * @param create
   *          the view is created if it does not exist.
   * @return the requested view
   * @throws IllegalArgumentException
   *           if the view does not exist and is not to be created.
   */
  public static CAS getView(CAS cas, String viewName, boolean create) {
    CAS view;
    try {
      view = cas.getView(viewName);
    } catch (CASRuntimeException e) {
      // View does not exist
      if (create) {
        view = cas.createView(viewName);
      } else {
        throw new IllegalArgumentException("No view with name [" + viewName + "]");
      }
    }

    return view;
  }

  /**
   * Fetch the text covered by the specified annotations and return it as a list of strings.
   * 
   * @param <T>
   *          UIMA JCas type.
   * @param iterable
   *          annotation container.
   * @return list of covered strings.
   */
  public static <T extends AnnotationFS> List<String> toText(Iterable<T> iterable) {
    return toText(iterable.iterator());
  }

  /**
   * Fetch the text covered by the specified annotations and return it as a list of strings.
   * 
   * @param <T>
   *          UIMA JCas type.
   * @param iterator
   *          annotation iterator.
   * @return list of covered strings.
   */
  public static <T extends AnnotationFS> List<String> toText(Iterator<T> iterator) {
    List<String> text = new ArrayList<String>();
    while (iterator.hasNext()) {
      AnnotationFS a = iterator.next();
      text.add(a.getCoveredText());
    }
    return text;
  }
}
