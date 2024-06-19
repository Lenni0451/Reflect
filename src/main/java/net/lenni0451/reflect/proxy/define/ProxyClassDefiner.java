package net.lenni0451.reflect.proxy.define;

import lombok.SneakyThrows;
import net.lenni0451.reflect.ClassLoaders;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import java.io.File;
import java.nio.file.Files;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.dot;
import static net.lenni0451.reflect.bytecode.BytecodeUtils.slash;

public interface ProxyClassDefiner {

    static ProxyClassDefiner loader(final ClassLoader classLoader) {
        return (builtClass, superClass, interfaces) -> ClassLoaders.defineClass(classLoader, dot(builtClass.getName()), builtClass.toBytes());
    }

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


    Class<?> defineProxyClass(final BuiltClass builtClass, final Class<?> superClass, final Class<?>[] interfaces);

}
