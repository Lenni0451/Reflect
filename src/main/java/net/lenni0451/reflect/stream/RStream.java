package net.lenni0451.reflect.stream;

import net.lenni0451.reflect.JavaBypass;
import net.lenni0451.reflect.stream.constructor.ConstructorStream;
import net.lenni0451.reflect.stream.field.FieldStream;
import net.lenni0451.reflect.stream.method.MethodStream;

/**
 * Get a stream of all fields, methods or constructors of a class.
 */
public class RStream {

    /**
     * Get a stream of the given class.<br>
     * When getting a virtual field or invoking a virtual method you <b>have</b> to provide an instance of the class.
     *
     * @param clazz The class to get the stream of
     * @return The stream instance of the given class
     */
    public static RStream of(final Class<?> clazz) {
        return new RStream(clazz, null);
    }

    /**
     * Get a stream of the given class with the given instance.<br>
     * When getting a virtual field or invoking a virtual method you <b>don't</b> have to provide an instance of the class.
     *
     * @param clazz    The class to get the stream of
     * @param instance The instance of the given class
     * @return The stream instance of the given class and instance
     */
    public static RStream of(final Class<?> clazz, final Object instance) {
        return new RStream(clazz, instance);
    }

    /**
     * Get a stream of the given class by name.<br>
     * When getting a virtual field or invoking a virtual method you <b>have</b> to provide an instance of the class.
     *
     * @param className The name of the class to get the stream of
     * @return The stream instance of the given class by name
     * @throws ClassNotFoundException If the class with the given name doesn't exist
     */
    public static RStream of(final String className) {
        try {
            return of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            JavaBypass.UNSAFE.throwException(e);
        }
        return null;
    }

    /**
     * Get the stream of the given class by name with the given instance.<br>
     * When getting a virtual field or invoking a virtual method you <b>don't</b> have to provide an instance of the class.
     *
     * @param className The name of the class to get the stream of
     * @param instance  The instance of the given class
     * @return The stream instance of the given class by name and instance
     * @throws ClassNotFoundException If the class with the given name doesn't exist
     */
    public static RStream of(final String className, final Object instance) {
        try {
            return of(Class.forName(className), instance);
        } catch (ClassNotFoundException e) {
            JavaBypass.UNSAFE.throwException(e);
        }
        return null;
    }

    /**
     * Get the stream of the given instance.<br>
     * The class of the instance will be used to get the stream of.<br>
     * When getting a virtual field or invoking a virtual method you <b>don't</b> have to provide an instance of the class.
     *
     * @param instance The instance to get the stream of
     * @return The stream instance of the given instance
     */
    public static RStream of(final Object instance) {
        return new RStream(instance.getClass(), instance);
    }


    private final Class<?> clazz;
    private final Object instance;
    private boolean withSuper;

    private FieldStream fieldStream;
    private MethodStream methodStream;
    private ConstructorStream constructorStream;

    private RStream(final Class<?> clazz, final Object instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    /**
     * Get the class of this stream.
     */
    public Class<?> clazz() {
        return this.clazz;
    }

    /**
     * Get the instance of this stream.
     */
    public Object instance() {
        return this.instance;
    }

    /**
     * Include fields and methods from super classes.<br>
     * This will reset the field and method stream.
     *
     * @return This stream instance
     */
    public RStream withSuper() {
        if (this.withSuper) return this;
        this.withSuper = true;
        this.fieldStream = null;
        this.methodStream = null;
        return this;
    }


    /**
     * Get the field stream of this class.
     */
    public FieldStream fields() {
        if (this.fieldStream == null) this.fieldStream = new FieldStream(this, this.withSuper);
        return this.fieldStream;
    }

    /**
     * Get the constructor stream of this class.
     */
    public ConstructorStream constructors() {
        if (this.constructorStream == null) this.constructorStream = new ConstructorStream(this);
        return this.constructorStream;
    }

    /**
     * Get the method stream of this class.
     */
    public MethodStream methods() {
        if (this.methodStream == null) this.methodStream = new MethodStream(this, this.withSuper);
        return this.methodStream;
    }

}
