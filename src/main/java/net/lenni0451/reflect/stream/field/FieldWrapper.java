package net.lenni0451.reflect.stream.field;

import net.lenni0451.reflect.Fields;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A wrapper of the {@link Field} class for easy access to all required methods.
 */
public class FieldWrapper {

    private final FieldStream parent;
    private final Field field;
    private final ModifierWrapper modifier;

    public FieldWrapper(final FieldStream parent, final Field field) {
        this.parent = parent;
        this.field = field;
        this.modifier = new ModifierWrapper(this.field.getModifiers());
    }

    /**
     * @return The parent field stream
     */
    public FieldStream parent() {
        return this.parent;
    }

    /**
     * @return The underlying field
     */
    public Field raw() {
        return this.field;
    }

    /**
     * @return The name of the field
     */
    public String name() {
        return this.field.getName();
    }

    /**
     * @return The type of the field
     */
    public Class<?> type() {
        return this.field.getType();
    }

    /**
     * @return The owner (declaring) class of the field
     */
    public Class<?> owner() {
        return this.field.getDeclaringClass();
    }

    /**
     * @return The {@link ModifierWrapper} of the field
     */
    public ModifierWrapper modifier() {
        return this.modifier;
    }

    /**
     * @return The generic types of the field
     */
    public Type[] genericTypes() {
        if (this.field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) this.field.getGenericType();
            return parameterizedType.getActualTypeArguments();
        }
        return new Type[0];
    }

    /**
     * @return A stream of all annotations of the field
     */
    public Stream<Annotation> annotations() {
        return Arrays.stream(this.field.getDeclaredAnnotations());
    }


    /**
     * Get the value of the field.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param <T> The type of the field
     * @return The value of the field
     * @throws IllegalStateException If the field is not static and no instance is cached
     */
    public <T> T get() {
        if (!this.modifier.isStatic() && this.parent.parent().instance() == null) throw new IllegalStateException("Can not get non static field if no instance is provided");
        return Fields.get(this.parent.parent().instance(), this.field);
    }

    /**
     * Get the value of the field and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @return The value of the field
     * @throws IllegalStateException If the field is not static and no instance is cached
     */
    public RStream stream() {
        return RStream.of(this.<Object>get());
    }

    /**
     * Get the value of the field and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param clazz The class used for the stream
     * @return The value of the field
     * @throws IllegalStateException If the field is not static and no instance is cached
     */
    public RStream stream(final Class<?> clazz) {
        return RStream.of(clazz, this.get());
    }

    /**
     * Get the value of the field with the given instance.
     *
     * @param instance The instance of the owner
     * @param <T>      The type of the field
     * @return The value of the field
     */
    public <T> T get(final Object instance) {
        return Fields.get(instance, this.field);
    }

    /**
     * Get the value of the field with the given instance and wrap it in a new {@link RStream}.
     *
     * @param instance The instance of the owner
     * @return The value of the field
     */
    public RStream stream(final Object instance) {
        return RStream.of(this.<Object>get(instance));
    }

    /**
     * Get the value of the field with the given instance and wrap it in a new {@link RStream}.
     *
     * @param clazz    The class used for the stream
     * @param instance The instance of the owner
     * @return The value of the field
     */
    public RStream stream(final Class<?> clazz, final Object instance) {
        return RStream.of(clazz, this.get(instance));
    }

    /**
     * Set the value of the {@link Field}.
     *
     * @param value The value to set the {@link Field} to
     * @throws IllegalStateException If trying to set a non-static {@link Field} if no instance is provided
     */
    public void set(final Object value) {
        if (!this.modifier.isStatic() && this.parent.parent().instance() == null) throw new IllegalStateException("Can not set non-static field if no instance is provided");
        Fields.set(this.parent.parent().instance(), this.field, value);
    }

    /**
     * Set the value of the {@link Field} with the given owner.
     *
     * @param instance The instance of the owner
     * @param value    The value to set the {@link Field} to
     */
    public void set(final Object instance, final Object value) {
        Fields.set(instance, this.field, value);
    }

    /**
     * Copy the value of the {@link Field} to the given target.
     *
     * @param target The target to copy the value to
     */
    public void copy(final Object target) {
        if (this.modifier.isStatic()) throw new IllegalStateException("Can not copy static field");
        if (this.parent.parent().instance() == null) throw new IllegalStateException("Can not copy field if no instance is provided");
        Fields.copy(this.parent.parent().instance(), target, this.field);
    }

    /**
     * Copy the value of the {@link Field} to the given target with the given owner.
     *
     * @param instance The instance of the owner
     * @param target   The target to copy the value to
     */
    public void copy(final Object instance, final Object target) {
        if (this.modifier.isStatic()) throw new IllegalStateException("Can not copy static field");
        Fields.copy(instance, target, this.field);
    }


    @Override
    public String toString() {
        return this.field.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldWrapper) return this.field.equals(((FieldWrapper) obj).field);
        else if (obj instanceof Field) return this.field.equals(obj);
        else return false;
    }

    @Override
    public int hashCode() {
        return this.field.hashCode();
    }

}
