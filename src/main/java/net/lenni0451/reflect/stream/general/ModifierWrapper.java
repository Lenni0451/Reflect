package net.lenni0451.reflect.stream.general;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A wrapper for field/constructor/method modifier for easier usage.
 */
public class ModifierWrapper {

    private final int modifier;

    public ModifierWrapper(final int modifier) {
        this.modifier = modifier;
    }

    /**
     * @return The raw modifier
     */
    public int raw() {
        return this.modifier;
    }

    /**
     * Targets: {@link Field}, {@link Constructor}, {@link Method}.
     *
     * @return If the modifier is public
     */
    public boolean isPublic() {
        return Modifier.isPublic(this.modifier);
    }

    /**
     * Targets: {@link Field}, {@link Constructor}, {@link Method}.
     *
     * @return If the modifier is private
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(this.modifier);
    }

    /**
     * Targets: {@link Field}, {@link Constructor}, {@link Method}.
     *
     * @return If the modifier is protected
     */
    public boolean isProtected() {
        return Modifier.isProtected(this.modifier);
    }

    /**
     * Targets: {@link Field}, {@link Method}.
     *
     * @return If the modifier is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(this.modifier);
    }

    /**
     * Targets: {@link Field}, {@link Method}.
     *
     * @return If the modifier is final
     */
    public boolean isFinal() {
        return Modifier.isFinal(this.modifier);
    }

    /**
     * Targets: {@link Field}, {@link Method}.
     *
     * @return If the modifier is synchronized
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(this.modifier);
    }

    /**
     * Targets: {@link Field}.
     *
     * @return If the modifier is volatile
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(this.modifier);
    }

    /**
     * Targets: {@link Field}.
     *
     * @return If the modifier is transient
     */
    public boolean isTransient() {
        return Modifier.isTransient(this.modifier);
    }

    /**
     * Targets: {@link Method}.
     *
     * @return If the modifier is native
     */
    public boolean isNative() {
        return Modifier.isNative(this.modifier);
    }

    /**
     * Targets: {@link Method}.
     *
     * @return If the modifier is abstract
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(this.modifier);
    }

    /**
     * Targets: {@link Method}.
     *
     * @return If the modifier is strict
     */
    public boolean isStrict() {
        return Modifier.isStrict(this.modifier);
    }

}
