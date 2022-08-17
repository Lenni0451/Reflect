package net.lenni0451.reflect.stream.general;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A wrapper for {@link Field}/{@link Method}/{@link Constructor} modifier for easier usage.
 */
public class ModifierWrapper {

    private final int modifier;

    public ModifierWrapper(final int modifier) {
        this.modifier = modifier;
    }

    /**
     * Get the raw modifier int
     */
    public int raw() {
        return this.modifier;
    }

    /**
     * Get if the modifier contains the public modifier<br>
     * Targets: {@link Field}, {@link Method}, {@link Constructor}
     */
    public boolean isPublic() {
        return Modifier.isPublic(this.modifier);
    }

    /**
     * Get if the modifier contains the private modifier<br>
     * Targets: {@link Field}, {@link Method}, {@link Constructor}
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(this.modifier);
    }

    /**
     * Get if the modifier contains the protected modifier<br>
     * Targets: {@link Field}, {@link Method}, {@link Constructor}
     */
    public boolean isProtected() {
        return Modifier.isProtected(this.modifier);
    }

    /**
     * Get if the modifier contains the static modifier<br>
     * Targets: {@link Field}, {@link Method}
     */
    public boolean isStatic() {
        return Modifier.isStatic(this.modifier);
    }

    /**
     * Get if the modifier contains the final modifier<br>
     * Targets: {@link Field}, {@link Method}
     */
    public boolean isFinal() {
        return Modifier.isFinal(this.modifier);
    }

    /**
     * Get if the modifier contains the synchronized modifier<br>
     * Targets: {@link Method}
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(this.modifier);
    }

    /**
     * Get if the modifier contains the volatile modifier<br>
     * Targets: {@link Field}
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(this.modifier);
    }

    /**
     * Get if the modifier contains the transient modifier<br>
     * Targets: {@link Field}
     */
    public boolean isTransient() {
        return Modifier.isTransient(this.modifier);
    }

    /**
     * Get if the modifier contains the native modifier<br>
     * Targets: {@link Method}
     */
    public boolean isNative() {
        return Modifier.isNative(this.modifier);
    }

    /**
     * Get if the modifier contains the abstract modifier<br>
     * Targets: {@link Method}
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(this.modifier);
    }

    /**
     * Get if the modifier contains the strict modifier<br>
     * Targets: {@link Method}
     */
    public boolean isStrict() {
        return Modifier.isStrict(this.modifier);
    }

}
