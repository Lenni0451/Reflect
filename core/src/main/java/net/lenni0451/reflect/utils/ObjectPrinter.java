package net.lenni0451.reflect.utils;

import net.lenni0451.reflect.Fields;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Convert an Object to a String using reflection to access all fields.
 */
public class ObjectPrinter {

    private static final List<Class<?>> PRIMITIVE_CLASSES = FieldInitializer.init(new ArrayList<>(), list -> {
        list.add(Boolean.class);
        list.add(Byte.class);
        list.add(Short.class);
        list.add(Character.class);
        list.add(Integer.class);
        list.add(Long.class);
        list.add(Float.class);
        list.add(Double.class);
        list.add(String.class);
    });
    private static final List<Converter> CONVERTERS = FieldInitializer.init(new ArrayList<>(), list -> {
        list.add(new Converter(Class::isArray, (array, out, valueToString, includeSuper) -> {
            out.append(array.getClass().getComponentType().getSimpleName()).append("[]{");
            int size = Array.getLength(array);
            for (int i = 0; i < size; i++) {
                Object value = Array.get(array, i);
                out.append(valueToString.apply(value)).append(", ");
            }
            if (size > 0) out.setLength(out.length() - 2);
            out.append("}");
        }));
        list.add(new Converter(Iterable.class::isAssignableFrom, (it, out, valueToString, includeSuper) -> {
            Iterable<?> iterable = (Iterable<?>) it;
            out.append(it.getClass().getSimpleName()).append("{");
            boolean hasElements = false;
            for (Object value : iterable) {
                hasElements = true;
                out.append(valueToString.apply(value)).append(", ");
            }
            if (hasElements) out.setLength(out.length() - 2);
            out.append("}");
        }));
        list.add(new Converter(Map.class::isAssignableFrom, (m, out, valueToString, includeSuper) -> {
            Map<?, ?> map = (Map<?, ?>) m;
            out.append(m.getClass().getSimpleName()).append("{");
            boolean hasEntries = false;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                hasEntries = true;
                out.append(valueToString.apply(entry.getKey())).append("=").append(valueToString.apply(entry.getValue())).append(", ");
            }
            if (hasEntries) out.setLength(out.length() - 2);
            out.append("}");
        }));

        //Has to be last
        list.add(new Converter(c -> true, (o, out, valueToString, includeSuper) -> {
            out.append(o.getClass().getSimpleName()).append("{");
            Field[] fields = getFields(o.getClass(), includeSuper);
            for (Field field : fields) {
                Object value = Fields.get(o, field);
                out.append(field.getName()).append("=").append(valueToString.apply(value)).append(", ");
            }
            if (fields.length > 0) out.setLength(out.length() - 2);
            out.append("}");
        }));
    });

    /**
     * Convert an object to a string using reflection to access all fields.<br>
     * The depth is set to {@code 0} so only the object itself will be printed.<br>
     * Super fields will not be included.
     *
     * @param o The object to convert to a string
     * @return The string representation of the object
     * @see #toString(Object, int, boolean)
     */
    public static String toString(final Object o) {
        return toString(o, 0, false);
    }

    /**
     * Convert an object to a string using reflection to access all fields.<br>
     * Special handling exists for: {@code null}, primitive types, arrays, {@link Iterable}s and {@link Map}s.<br>
     * A depth of {@code 0} is the default and only the object itself will be printed.<br>
     * For fields outside the depth, only the {@code toString()} method of the object will be called.
     *
     * @param o            The object to convert to a string
     * @param depth        The maximum depth of fields to access
     * @param includeSuper If the fields of super classes should be included
     * @return The string representation of the object
     */
    public static String toString(final Object o, final int depth, final boolean includeSuper) {
        Optional<String> plainString = plainToString(o);
        if (plainString.isPresent()) return plainString.get();

        StringBuilder out = new StringBuilder();
        Converter converter = CONVERTERS.stream()
                .filter(conv -> conv.filter.test(o.getClass()))
                .findFirst()
                .orElse(null);
        if (converter == null) throw new IllegalStateException("No converter found! This should never happen!");
        converter.converter.convert(o, out, value -> {
            if (depth > 0) return toString(value, depth - 1, includeSuper);
            else return plainToString(value).orElseGet(() -> String.valueOf(value));
        }, includeSuper);
        return out.toString();
    }

    private static Field[] getFields(final Class<?> clazz, final boolean includeSuper) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (true) {
            for (Field field : Fields.getDeclaredFields(current)) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                fields.add(field);
            }
            if (!includeSuper || current == null || Object.class.equals(current)) break;
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    private static Optional<String> plainToString(final Object o) {
        if (o == null) {
            return Optional.of("null");
        } else if (PRIMITIVE_CLASSES.contains(o.getClass())) {
            String quote;
            if (o instanceof String) quote = "\"";
            else if (o instanceof Character) quote = "'";
            else quote = "";
            return Optional.of(quote + o + quote);
        }
        return Optional.empty();
    }


    private static class Converter {
        private final Predicate<Class<?>> filter;
        private final ConverterFunction converter;

        private Converter(final Predicate<Class<?>> filter, final ConverterFunction converter) {
            this.filter = filter;
            this.converter = converter;
        }
    }

    @FunctionalInterface
    private interface ConverterFunction {
        void convert(final Object o, final StringBuilder out, final Function<Object, String> valueToString, final boolean includeSuper);
    }

}
