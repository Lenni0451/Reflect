package net.lenni0451.reflect.stream;

import net.lenni0451.reflect.stream.constructor.ConstructorStream;
import net.lenni0451.reflect.stream.field.FieldStream;
import net.lenni0451.reflect.stream.method.MethodStream;
import net.lenni0451.reflect.stream.utils.Lazy;
import net.lenni0451.reflect.stream.utils.Sneaky;

public class RStream {

    /**
     * Get a {@link RStream} instance of the given class
     *
     * @param clazz The {@link Class} to get the {@link RStream} instance of
     * @return The {@link RStream} instance of the given class
     */
    public static RStream of(final Class<?> clazz) {
        return new RStream(clazz, null);
    }

    /**
     * Get a {@link RStream} instance of the given class and instance
     *
     * @param clazz    The {@link Class} to get the {@link RStream} instance of
     * @param instance The instance of the given class
     * @return The {@link RStream} instance of the given class and instance
     */
    public static RStream of(final Class<?> clazz, final Object instance) {
        return new RStream(clazz, instance);
    }

    /**
     * Get the {@link RStream} instance of the given class by name
     *
     * @param className The name of the class to get the {@link RStream} instance of
     * @return The {@link RStream} instance of the given class by name
     * @throws ClassNotFoundException If the class with the given name doesn't exist
     */
    public static RStream of(final String className) {
        try {
            return of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            Sneaky.sthrow(e);
        }
        return null;
    }

    /**
     * Get the {@link RStream} instance of the given class by name and instance
     *
     * @param className The name of the class to get the {@link RStream} instance of
     * @param instance  The instance of the given class
     * @return The {@link RStream} instance of the given class by name and instance
     * @throws ClassNotFoundException If the class with the given name doesn't exist
     */
    public static RStream of(final String className, final Object instance) {
        try {
            return of(Class.forName(className), instance);
        } catch (ClassNotFoundException e) {
            Sneaky.sthrow(e);
        }
        return null;
    }

    /**
     * Get the {@link RStream} instance of the given instance
     *
     * @param instance The instance to get the {@link RStream} instance of
     * @return The {@link RStream} instance of the given instance
     */
    public static RStream of(final Object instance) {
        return new RStream(instance.getClass(), instance);
    }


    private final Class<?> clazz;
    private final Object instance;

    private final Lazy<FieldStream> fieldStream;
    private final Lazy<MethodStream> methodStream;
    private final Lazy<ConstructorStream> constructorStream;

    private RStream(final Class<?> clazz, final Object instance) {
        this.clazz = clazz;
        this.instance = instance;

        this.fieldStream = new Lazy<>(() -> new FieldStream(this));
        this.methodStream = new Lazy<>(() -> new MethodStream(this));
        this.constructorStream = new Lazy<>(() -> new ConstructorStream(this));
    }

    /**
     * Get the {@link Class} of the {@link RStream} instance
     */
    public Class<?> clazz() {
        return this.clazz;
    }

    /**
     * Get the instance of the {@link RStream} instance
     */
    public Object instance() {
        return this.instance;
    }


    /**
     * Get the {@link FieldStream} of the class
     */
    public FieldStream fields() {
        return this.fieldStream.get();
    }

    /**
     * Get the {@link MethodStream} of the class
     */
    public MethodStream methods() {
        return this.methodStream.get();
    }

    /**
     * Get the {@link ConstructorStream} of the class
     */
    public ConstructorStream constructors() {
        return this.constructorStream.get();
    }

}
