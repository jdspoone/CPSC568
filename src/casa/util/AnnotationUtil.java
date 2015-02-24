package casa.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * A class used holding useful (static) annotation related methods.
 * 
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * 
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 */
public class AnnotationUtil {

	/**
	 * Searches the [searchClass] for a field with the name [name] that is
	 * marked by the annotation [annotationClass]. The field is search all the
	 * way up the type hierarchy. Fields in the object marked
	 * &#64;[annotation](recurse=true) are recursively searched for the field.
	 * You can specify fields in a sub-object using "dot syntax": For a class
	 * specified:
	 * 
	 * <pre>
	 * public class X 
	 *   (@)CasaOptions
	 *   int y;
	 * }
	 * public class Z {
	 *   (@)CasaOptions(recurse=true)
	 *   X x;
	 * }
	 * </pre>
	 * 
	 * you can search for the y field in Z by calling getAnnotatedField(new Z(),
	 * CasaOptions.class, "x.y").
	 * 
	 * @param object
	 *            The object who's class to search for the field name
	 * @param annotationClass
	 *            Only fields marked with annoation are searched
	 * @param name
	 *            The name of the field
	 * @return A Pair containing the object the field is found in and the Field
	 *         object itself
	 * @throws IllegalArgumentException
	 *             This actually shouldn't be thrown
	 * @throws IllegalAccessException
	 *             If the field is found, but it's not accessible
	 */
	public static Pair<Object, Field> getAnnotatedField(Object object,
			Class<? extends Annotation> annotationClass, String name)
			throws IllegalArgumentException, IllegalAccessException {
		// split the name around the first dot into LOCALNAME and REST, where
		// REST will be null if there's no dot
		int dotIndex = name.indexOf('.');
		String localName;
		String rest;
		if (dotIndex < 0) {
			localName = name;
			rest = null;
		} else {
			localName = name.substring(0, dotIndex);
			rest = (name.length() > (dotIndex + 1)) ? name
					.substring(dotIndex + 1) : null;
		}

		List<Field> fields = AnnotationUtil.getAnnotatedFields(object
				.getClass(), annotationClass);
		for (Field field : fields) {
			if (field.getName().equalsIgnoreCase(localName)) {
				return rest == null ? new Pair<Object, Field>(object, field)
						: getAnnotatedField(field.get(object), annotationClass,
								rest);
			}
		}
		return null;
	}

	/**
	 * Returns a list of all fields in [searchClass] marked with an annotation
	 * [annotationClass]. This method traverses the type hierarchy, not just the
	 * class itself. if [annotationClass] is null, all fields are returned.
	 * 
	 * @param searchClass
	 *            The object who's type is to be searched for the annotation.
	 * @param annotationClass
	 *            The annotation to search for
	 * @return A list of all fields in [object]'s class marked with an
	 *         annotation [annotationClass]
	 */
	public static List<Field> getAnnotatedFields(Class<?> searchClass,
			Class<? extends Annotation> annotationClass) {
		LinkedList<Field> list = new LinkedList<Field>();
		for (; searchClass != null; searchClass = searchClass.getSuperclass()) {
			try {
				for (Field f : searchClass.getDeclaredFields()) {
					if (annotationClass == null
							|| f.isAnnotationPresent(annotationClass)) {
						list.add(f);
					}
				}
			} catch (Throwable e) {
				// We might have arrived here as a result of a security exception of a no class def found exception
				// In either case, we just ignore it. and carry on.
			}
		}
		return list;
	}
	
	/**
	 * Retrieves an annotation that is an instance of a specified annotation class.
	 * If there are more than annotations matching then only one will be arbitrarily chosen. 
	 * 
	 * @param <Anno> The annotation type to retrieve
	 * @param field the field to read for annotations
	 * @param annotationClass The class object of the annotation type to retrieve
	 * @return the matching annotation or null (if none exist)
	 */
	@SuppressWarnings("unchecked")
	public static <Anno extends Annotation> Anno getAnnotation(Field field, Class<Anno> annotationClass){
		for(Annotation ann : field.getAnnotations())
			if(annotationClass.isInstance(ann))
				return (Anno)ann;
		return null;
	}
	
	/**
	 * Retrieves an annotation that is an instance of a specified annotation class.
	 * If there are more than annotations matching then only one will be arbitrarily chosen. 
	 * 
	 * @param <Anno> The annotation type to retrieve
	 * @param method the method to read for annotations
	 * @param annotationClass The class object of the annotation type to retrieve
	 * @return the matching annotation or null (if none exist)
	 */
	@SuppressWarnings("unchecked")
	public static <Anno extends Annotation> Anno getAnnotation(Method method, Class<Anno> annotationClass){
		for(Annotation ann : method.getAnnotations())
			if(annotationClass.isInstance(ann))
				return (Anno)ann;
		return null;
	}
}
