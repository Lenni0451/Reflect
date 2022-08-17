package net.lenni0451.reflect.stream.field;

import net.lenni0451.reflect.Fields;
import net.lenni0451.reflect.JavaBypass;
import net.lenni0451.reflect.stream.RStream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An easy-to-use stream to filter out the fields you want to access
 */
public class FieldStream {

    private final RStream parent;
    private final List<FieldWrapper> fields;

    public FieldStream(final RStream parent) {
        this.parent = parent;
        this.fields = new ArrayList<>();

        for (Field field : Fields.getDeclaredFields(parent.clazz())) this.fields.add(new FieldWrapper(this, field));
    }

    /**
     * Get the parent {@link RStream} instance
     */
    public RStream parent() {
        return this.parent;
    }

    /**
     * Get the amount of fields in this stream
     */
    public int size() {
        return this.fields.size();
    }


    /**
     * Get the {@link FieldWrapper} instance of the field with the given name
     *
     * @param name The name of the field
     * @return The {@link FieldWrapper} instance
     * @throws NoSuchFieldException If the field doesn't exist
     */
    public FieldWrapper by(final String name) {
        for (FieldWrapper field : this.fields) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        JavaBypass.UNSAFE.throwException(new NoSuchFieldException());
        return null;
    }

    /**
     * Get the {@link FieldWrapper} instance of the field with the given index
     *
     * @param index The index of the field
     * @return The {@link FieldWrapper} instance
     * @throws NoSuchFieldException If the index is out of bounds
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
     * Filter the fields by the given predicate
     *
     * @param filter The predicate
     * @return The filtered {@link FieldStream}
     */
    public FieldStream filter(final Predicate<FieldWrapper> filter) {
        this.fields.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter out all fields whose type is not the given {@link Class}
     *
     * @param clazz The {@link Class} of the field type
     * @return The filtered {@link FieldStream}
     */
    public FieldStream filter(final Class<?> clazz) {
        return this.filter(field -> field.type().equals(clazz));
    }

    /**
     * Filter out all static/non-static fields
     *
     * @param isStatic Whether the field should be static or not
     * @return The filtered {@link FieldStream}
     */
    public FieldStream filter(final boolean isStatic) {
        return this.filter(field -> field.modifier().isStatic() == isStatic);
    }


    /**
     * Get an iterator of the {@link FieldWrapper} instances
     */
    public Iterator<FieldWrapper> iterator() {
        return this.fields.iterator();
    }

    /**
     * Get the java stream of the {@link FieldWrapper} instances
     */
    public Stream<FieldWrapper> jstream() {
        return this.fields.stream();
    }

    /**
     * Loop through all {@link FieldWrapper} instances
     *
     * @param consumer The consumer
     * @return The {@link FieldStream}
     */
    public FieldStream forEach(final Consumer<FieldWrapper> consumer) {
        this.fields.forEach(consumer);
        return this;
    }

}
