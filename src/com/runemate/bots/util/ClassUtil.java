package com.runemate.bots.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by I on 1/23/2016.
 */
public final class ClassUtil {

    /**
     * Maps primitive {@code Class}es to their corresponding wrapper {@code Class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new ConcurrentHashMap<>(9);
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    /**
     * Maps wrapper {@code Class}es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new ConcurrentHashMap<>(primitiveWrapperMap.size());
    static {
        for (final Map.Entry<Class<?>, Class<?>> classClassEntry : primitiveWrapperMap.entrySet()) {
            final Class<?> wrapperClass = classClassEntry.getValue();
            if (!classClassEntry.getKey().equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, classClassEntry.getKey());
            }
        }
    }

    private ClassUtil() {
    }

    /**
     * Returns whether the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     * {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     *
     * @param type
     *            The class to query or null.
     * @return true if the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
     *         {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     * @since 3.1
     */
    public static boolean isPrimitiveOrWrapper(final Class<?> type) {
        return type != null && (type.isPrimitive() || isPrimitiveWrapper(type));
    }

    /**
     * Returns whether the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     *
     * @param type
     *            The class to query or null.
     * @return true if the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
     *         {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
     * @since 3.1
     */
    public static boolean isPrimitiveWrapper(final Class<?> type) {
        return wrapperPrimitiveMap.containsKey(type);
    }

}
