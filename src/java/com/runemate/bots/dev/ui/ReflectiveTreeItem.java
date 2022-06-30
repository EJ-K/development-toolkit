package com.runemate.bots.dev.ui;

import static com.runemate.bots.dev.ui.DevelopmentToolkitPage.*;

import com.runemate.bots.util.*;
import com.runemate.game.api.hybrid.*;
import com.runemate.game.api.hybrid.local.hud.*;
import com.runemate.game.api.script.annotations.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.rmi.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import javafx.scene.control.*;
import javafx.util.*;

public class ReflectiveTreeItem extends QueriableTreeItem<Pair<Method, Object>> {

    public static final List<Pair<Class<?>, String>> BLACKLISTED_METHODS = new ArrayList<>(50);
    public static final List<Pair<Class<?>, Pair<Method, Function<Object, ?>>>> ALIASED_METHODS = new ArrayList<>(50);
    private static final Pattern METHOD_REGEX = Pattern.compile("^(get|is|are|has)\\w+$");

    public ReflectiveTreeItem(final Method method, final Object value) {
        super(new ThreadSafePair<>(method, value));
    }

    public ReflectiveTreeItem(final Pair<Method, Object> value) {
        super(new ThreadSafePair<>(value.getKey(), value.getValue()));
    }

    @Override
    public List<TreeItem<Pair<Method, Object>>> query() {
        final Object queryableObject = getQueryableObject();
        if (queryableObject == null) {
            return null;
        }
        return Stream.concat(getMethodsToReflect(queryableObject instanceof Class
                ? (Class<?>) queryableObject
                : queryableObject.getClass()).stream()
                .map(m -> {
                    Object value;
                    try {
                        m.setAccessible(true);
                        value = m.invoke(queryableObject);
                    } catch (Exception e) {
                        System.err.println("Point H Thread: " + Thread.currentThread().getName() + ' ' + m);
                        if (!(e.getCause() instanceof ServerException)) {
                            e.printStackTrace();
                        }
                        value = e.getCause() != null ? e.getCause() : e;
                    }
                    final TreeItem<Pair<Method, Object>> treeItem;
                    if (value == null) {
                        treeItem = new TreeItem<>(new Pair<>(m, null));
                    } else if (!isTypeExpandable(m.getReturnType()) && !isTypeExpandable(value.getClass())) {
                        treeItem = new TreeItem<>(new Pair<>(m, value));
                    } else if (Iterable.class.isAssignableFrom(value.getClass())) {
                        treeItem = new TreeItem<>(new Pair<>(m, value));
                        ((Iterable<?>) value).forEach(o -> treeItem.getChildren()
                            .add(o == null || isTypeExpandable(o.getClass())
                                ? new ReflectiveTreeItem(null, o)
                                : new TreeItem<>(new Pair<>(null, o))));
                    } else if (value.getClass().isArray()) {
                        treeItem = new TreeItem<>(new Pair<>(m, value));
                        for (int index = 0, length = Array.getLength(value); index < length; ++index) {
                            Object o = Array.get(value, index);
                            if (o != null) {
                                treeItem.getChildren().add(isTypeExpandable(o.getClass())
                                    ? new ReflectiveTreeItem(null, o)
                                    : new TreeItem<>(new Pair<>(null, o)));
                            }
                        }
                    } else if (Map.class.isAssignableFrom(value.getClass())) {
                        treeItem = new TreeItem<>(new Pair<>(m, value));
                        ((Map<?, ?>) value).entrySet().forEach(o -> treeItem.getChildren().add(new ReflectiveTreeItem(null, o)));
                    } else {
                        treeItem = new ReflectiveTreeItem(m, value);
                    }
                    return treeItem;
                }),
            ALIASED_METHODS.stream()
                .filter(alias -> alias.getKey().isAssignableFrom(queryableObject.getClass()))
                .map(alias -> new TreeItem<>(new Pair<>(alias.getValue().getKey(),
                    (Object) alias.getValue().getValue().apply(queryableObject)
                )))
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected Object getQueryableObject() {
        return getValue().getValue();
    }

    // When you change this change the override in StaticReflectiveTreeItem
    protected List<Method> getMethodsToReflect(Class<?> c) {
        return Arrays.stream(c.getMethods())
            .filter(m -> m.getParameterCount() == 0
                && !Modifier.isStatic(m.getModifiers())
                && !Objects.equals(m.getReturnType(), Void.TYPE)
                && !m.isAnnotationPresent(Deprecated.class)
                && (Environment.isRS3() ? !m.isAnnotationPresent(OSRSOnly.class) : !m.isAnnotationPresent(RS3Only.class))
                && METHOD_REGEX.matcher(m.getName()).matches()
                && BLACKLISTED_METHODS.stream().noneMatch(pair -> pair.getKey().isAssignableFrom(c) && pair.getValue().equals(m.getName())))
            .collect(Collectors.toList());
    }

    protected boolean isTypeExpandable(Class<?> c) {
        if (c.isArray() || Iterable.class.isAssignableFrom(c)) {
            return true;
        }
        if (ClassUtil.isPrimitiveOrWrapper(c)) {
            return false;
        }
        if (String.class.equals(c)
            || Rectangle.class.equals(c)
            || InteractableRectangle.class.equals(c)
            || Color.class.equals(c)
            || File.class.equals(c)
            || Point.class.equals(c)
            || InteractablePoint.class.equals(c)) {
            return false;
        }
        if (getMethodsToReflect(c).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getValue() == null || getValue().getValue() == null || super.isLeaf();
    }

    public static class StaticReflectiveTreeItem extends ReflectiveTreeItem {

        public StaticReflectiveTreeItem(Class<?> clazz) {
            super(null, clazz);
        }

        @Override
        protected List<Method> getMethodsToReflect(Class<?> c) {
            return Arrays.stream(c.getMethods())
                .filter(m -> m.getParameterCount() == 0
                    && Modifier.isStatic(m.getModifiers())
                    && !Objects.equals(m.getReturnType(), Void.TYPE)
                    && !m.isAnnotationPresent(Deprecated.class)
                    && (Environment.isRS3() ? !m.isAnnotationPresent(OSRSOnly.class) : !m.isAnnotationPresent(RS3Only.class))
                    && ("values".equals(m.getName()) || METHOD_REGEX.matcher(m.getName()).matches())
                    && BLACKLISTED_METHODS.stream()
                    .noneMatch(pair -> pair.getKey().isAssignableFrom(c) && pair.getValue().equals(m.getName())))
                .collect(Collectors.toList());
        }

        @Override
        protected boolean isTypeExpandable(Class<?> c) {
            if (c.isArray() || Iterable.class.isAssignableFrom(c)) {
                return true;
            }
            if (ClassUtil.isPrimitiveOrWrapper(c)) {
                return false;
            }
            if (String.class.equals(c)
                || Rectangle.class.equals(c)
                || InteractableRectangle.class.equals(c)
                || Color.class.equals(c)
                || File.class.equals(c)
                || Point.class.equals(c)
                || InteractablePoint.class.equals(c)) {
                return false;
            }
            if (super.getMethodsToReflect(c).isEmpty()) {
                return false;
            }
            return true;
        }
    }

    private static class ThreadSafePair<K, V> extends Pair<K, V> {

        public ThreadSafePair(final K key, final V value) {
            super(key, value);
        }

        @Override
        public K getKey() {
            return optionallyThreadedCall(super::getKey);
        }

        @Override
        public V getValue() {
            return optionallyThreadedCall(super::getValue);
        }

        @Override
        public boolean equals(final Object o) {
            return Boolean.TRUE.equals(optionallyThreadedCall(() -> super.equals(o)));
        }

        @Override
        public String toString() {
            return optionallyThreadedCall(super::toString);
        }
    }
}
