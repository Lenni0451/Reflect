package net.lenni0451.reflect.stream.field;

import net.lenni0451.reflect.Fields;
import net.lenni0451.reflect.JavaBypass;
import net.lenni0451.reflect.stream.RStream;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A stream of all fields of a class and super classes (if wanted).<br>
 * The fields of super classes are after the fields of the class itself (descending order).
 */
public class FieldStream {

    private final RStream parent;
    private final List<FieldWrapper> fields;

    public FieldStream(final RStream parent, final boolean withSuper) {
        this.parent = parent;
        this.fields = new ArrayList<>();

        Class<?> clazz = parent.clazz();
        do {
            for (Field field : Fields.getDeclaredFields(clazz)) this.fields.add(new FieldWrapper(this, field));

            if (!withSuper) break;
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }

    /**
     * @return The parent stream
     */
    public RStream parent() {
        return this.parent;
    }

    /**
     * @return The amount of fields in this stream
     */
    public int size() {
        return this.fields.size();
    }


    /**
     * Get a field by the given name.<br>
     * If there are multiple fields with the same name the first one will be returned.
     *
     * @param name The name of the field
     * @return The field wrapper
     * @throws NoSuchFieldException If the field doesn't exist
     */
    public FieldWrapper by(final String name) {
        for (FieldWrapper field : this.fields) {
            if (field.name().equals(name)) return field;
        }
        JavaBypass.UNSAFE.throwException(new NoSuchFieldException());
        return null;
    }

    /**
     * Get a field by the given index.<br>
     * The index is the position of the field in the stream.
     *
     * @param index The index of the field
     * @return The field wrapper
     * @throws NoSuchFieldException If the field doesn't exist
     */
    public FieldWrapper by(final int index) {
        try {
            return this.fields.get(index);
        } catch (IndexOutOfBoundsException e) {
            JavaBypass.UNSAFE.throwException(new NoSuchFieldException());
        }
        return null;
    }


    /**
     * Filter the fields with the given filter.<br>
     * The current stream will be modified.
     *
     * @param filter The filter
     * @return This stream
     */
    public FieldStream filter(final Predicate<FieldWrapper> filter) {
        this.fields.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter the fields by the given names.<br>
     * The current stream will be modified.
     *
     * @param names The names of the fields
     * @return This stream
     */
    public FieldStream filter(final Collection<String> names) {
        return this.filter(f -> names.contains(f.name()));
    }

    /**
     * Filter the fields by the given names.<br>
     * The current stream will be modified.
     *
     * @param names The names of the fields
     * @return This stream
     */
    public FieldStream filter(final String... names) {
        return this.filter(Arrays.asList(names));
    }

    /**
     * Filter the fields by the given type.<br>
     * The current stream will be modified.
     *
     * @param clazz The type
     * @return This stream
     */
    public FieldStream filter(final Class<?> clazz) {
        return this.filter(field -> field.type().equals(clazz));
    }

    /**
     * Filter the fields by whether tey are static.<br>
     * The current stream will be modified.
     *
     * @param isStatic Whether the fields should be static
     * @return This stream
     */
    public FieldStream filter(final boolean isStatic) {
        return this.filter(field -> field.modifier().isStatic() == isStatic);
    }


    /**
     * @return An iterator of field wrappers
     */
    public Iterator<FieldWrapper> iterator() {
        return this.fields.iterator();
    }

    /**
     * @return A stream of field wrappers
     */
    public Stream<FieldWrapper> jstream() {
        return this.fields.stream();
    }

    /**
     * Loop through all fields in this stream.
     *
     * @param consumer The consumer
     * @return This stream
     */
    public FieldStream forEach(final Consumer<FieldWrapper> consumer) {
        this.fields.forEach(consumer);
        return this;
    }

}
