package com.runemate.bots.dev.ui.element.query.transform;

import static com.runemate.bots.dev.ui.element.query.QueryBuilderExtension.SUPPORTED_PARAMETER_TYPES;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

import com.runemate.game.api.hybrid.queries.QueryBuilder;
import com.runemate.game.api.hybrid.util.collections.Pair;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class QueryParameterTransform {

    protected final String name;
    protected final Method method;

    private QueryParameterTransform(final String name, final Method method) {
        this.name = name;
        this.method = method;
    }

    private QueryParameterTransform(Pair<Method, String> pair) {
        this(pair.getRight(), pair.getLeft());
    }

    public static List<QueryParameterTransform> reflect(final Class<?> type) {
        if (!QueryBuilder.class.isAssignableFrom(type)) {
            return Collections.emptyList();
        }

        final List<QueryParameterTransform> declared = Arrays.stream(type.getMethods())
            .filter(it -> it.getReturnType().isAssignableFrom(type))
            .filter(it -> it.getParameterCount() > 0)
            .filter(it -> {
                final Class<?> pType = it.getParameterTypes()[0];
                return SUPPORTED_PARAMETER_TYPES.contains(
                    pType.isArray() ? pType.getComponentType() : pType);
            })
            .filter(it -> !isStatic(it.getModifiers()) && isPublic(it.getModifiers()))
            .map(it -> {
                final Class<?> t = it.getParameterTypes()[0];
                if (t.isArray()) {
                    return t.getComponentType().isEnum() ? enuerated(it, it.getName()) :
                        multi(it, it.getName());
                }
                return single(it, it.getName());
            })
            .collect(Collectors.toList());

        Class<?> superType = type.getSuperclass();
        if (superType != null) {
            declared.addAll(reflect(superType));
        }

        return declared;
    }

    public static QueryParameterTransform single(Method method, String name) {
        return new SingleTransform(name, method);
    }

    public static QueryParameterTransform multi(Method method, String name) {
        return new MultiTransform(name, method);
    }

    public static QueryParameterTransform enuerated(Method method, String name) {
        return new EnumTransform(name, method);
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public abstract boolean hasValue();

    public abstract Object value();

    public abstract void value(Object o);

    public abstract String getSignature();

    public abstract String getBuilderChainLink();

    public abstract Class<?> getParameterType();

    public boolean isArray() {
        return false;
    }

    public abstract void reset();

    public boolean isEnum() {
        return false;
    }

    public QueryBuilder apply(QueryBuilder qb) {
        try {
            return (QueryBuilder) method.invoke(qb, value());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return qb;
    }

    @Override
    public String toString() {
        return getSignature();
    }

    private static class SingleTransform extends QueryParameterTransform {
        private final String signature;
        private Object value;

        private SingleTransform(final String name, final Method method) {
            super(name, method);
            this.signature = name + "(" + getParameterType().getSimpleName() + ")";
        }

        @Override
        public boolean hasValue() {
            return value != null;
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public void value(final Object value) {
            this.value = value;
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public Class<?> getParameterType() {
            return method.getParameterTypes()[0];
        }

        @Override
        public boolean isArray() {
            return false;
        }

        public String getBuilderChainLink() {
            return hasValue() ? "." + name + "(" + value + ")" : "";
        }
    }

    private static class MultiTransform extends QueryParameterTransform {

        private final String signature;
        private final List<Object> values = new ArrayList<>();

        private MultiTransform(final String name, final Method method) {
            super(name, method);
            this.signature = name + "(" + getParameterType().getSimpleName() + "...)";
        }

        private MultiTransform(final Pair<Method, String> pair) {
            this(pair.getRight(), pair.getLeft());
        }

        @Override
        public boolean hasValue() {
            return !values.isEmpty();
        }

        @Override
        public Object value() {
            return values;
        }

        @Override
        public void value(final Object value) {
            values.add(value);
        }

        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public void reset() {
            if (values.size() > 1) {
                values.remove(values.size() - 1);
            } else {
                values.clear();
            }
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public Class<?> getParameterType() {
            return method.getParameterTypes()[0].getComponentType();
        }

        public Class<?> getRawParameterType() {
            return method.getParameterTypes()[0];
        }

        @Override
        public QueryBuilder apply(final QueryBuilder qb) {
            try {
                final List<?> value = (List<?>) value();
                final Object array = Array.newInstance(getParameterType(), value.size());
                for (int i = 0; i < value.size(); i++) {
                    Array.set(array, i, value.get(i));
                }
                final QueryBuilder res = (QueryBuilder) method.invoke(qb, array);
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return qb;
        }

        @Override
        public String getBuilderChainLink() {
            if (!hasValue()) {
                return "";
            }
            StringBuilder result = new StringBuilder("." + getName() + "(");
            final Iterator<Object> iterator = values.iterator();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                result.append(typeSpecificText(value));
                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
            return result + ")";
        }

        private String typeSpecificText(Object o) {
            if (o instanceof Pattern) {
                return "Pattern.compile(" + typeSpecificText(((Pattern) o).pattern()) + ")";
            } else if (o instanceof String) {
                return "\"" + o + "\"";
            }
            return o.toString();
        }
    }

    private static class EnumTransform extends MultiTransform {

        private EnumTransform(final String name, final Method method) {
            super(name, method);
        }

        @Override
        public String getBuilderChainLink() {
            if (!hasValue()) {
                return "";
            }
            StringBuilder result = new StringBuilder("." + getName() + "(");
            final Iterator<Enum<?>> iterator = ((List<Enum<?>>) value()).iterator();
            while (iterator.hasNext()) {
                Enum<?> value = iterator.next();
                Class<?> outer = value.getClass().getEnclosingClass();
                if (outer != null) {
                    result.append(outer.getSimpleName()).append(".");
                }
                result.append(value.getDeclaringClass().getSimpleName()).append(".").append(value.name());
                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
            return result + ")";
        }

//        @Override
//        public Class<?> getParameterType() {
//            return super.getParameterType();
//        }

        @Override
        public boolean isEnum() {
            return true;
        }
    }
}
