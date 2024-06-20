package net.lenni0451.reflect.proxy.define;

import lombok.SneakyThrows;
import net.lenni0451.reflect.ClassLoaders;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.dot;
import static net.lenni0451.reflect.bytecode.BytecodeUtils.slash;

/**
 * A class definer for proxy classes.
 */
public interface ProxyClassDefiner {

    /**
     * Create a new class loader based class definer.
     *
     * @param classLoader The class loader
     * @return The class definer
     */
    static ProxyClassDefiner loader(final ClassLoader classLoader) {
        return (builtClass, superClass, interfaces) -> ClassLoaders.defineClass(classLoader, dot(builtClass.getName()), builtClass.toBytes());
    }

    /**
     * Create a new class definer that dumps the class files to a directory.<br>
     * The defining of the class will be delegated to the given class definer.
     *
     * @param classDefiner The class definer
     * @param outputDir    The output directory
     * @return The class definer
     */
    static ProxyClassDefiner dumping(final ProxyClassDefiner classDefiner, final File outputDir) {
        return new ProxyClassDefiner() {
            @Override
            @SneakyThrows
            public Class<?> defineProxyClass(BuiltClass builtClass, Class<?> superClass, Class<?>[] interfaces) {
                File file = new File(outputDir, slash(builtClass.getName()) + ".class");
                file.getParentFile().mkdirs();
                Files.write(file.toPath(), builtClass.toBytes());
                return classDefiner.defineProxyClass(builtClass, superClass, interfaces);
            }
        };
    }


    /**
     * Define a built proxy class.
     *
     * @param builtClass The built class
     * @param superClass The super class
     * @param interfaces The interfaces
     * @return The defined class
     */
    Class<?> defineProxyClass(final BuiltClass builtClass, @Nullable final Class<?> superClass, @Nullable final Class<?>[] interfaces);

}
