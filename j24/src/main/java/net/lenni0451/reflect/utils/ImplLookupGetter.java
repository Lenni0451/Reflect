package net.lenni0451.reflect.utils;

import net.lenni0451.reflect.JVMConstants;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

@ApiStatus.Internal
public class ImplLookupGetter {

    private static final long PTR_SIZE = ValueLayout.ADDRESS.byteSize();
    private static final String PROPERTY_NAME = "IMPL_LOOKUP_FROM_JNI";

    private static final int JNI_OK = 0;
    private static final int JNI_ERR = -1;
    private static final int JNI_EDETACHED = -2;
    private static final int JNI_EVERSION = -3;
    private static final int JNI_ENOMEM = -4;
    private static final int JNI_EEXIST = -5;
    private static final int JNI_EINVAL = -6;

    public static MethodHandles.Lookup getLookup() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            SymbolLookup lookup = SymbolLookup.libraryLookup(getLibJVMPath(), arena);
            JNI jni = new JNI(arena, lookup);
            JNIVM vm = new JNIVM(arena, jni.getVM());
            JNIEnv env = new JNIEnv(arena, vm.getJNIEnv());

            MemorySegment lookupClass = null;
            MemorySegment implLookupRef = null;
            MemorySegment systemClass = null;
            MemorySegment propertiesRef = null;
            MemorySegment propertiesClass = null;
            MemorySegment propertyName = null;
            MemorySegment previousValue = null;
            try {
                lookupClass = env.findClass(MethodHandles.Lookup.class);
                MemorySegment implLookupField = env.getStaticFieldId(lookupClass, JVMConstants.FIELD_MethodHandles_Lookup_IMPL_LOOKUP, MethodHandles.Lookup.class.descriptorString());
                implLookupRef = env.getStaticObjectField(lookupClass, implLookupField);
                systemClass = env.findClass(System.class);
                MemorySegment getPropertiesMethod = env.getStaticMethodId(systemClass, "getProperties", "()Ljava/util/Properties;");
                propertiesRef = env.callStaticObjectMethod(systemClass, getPropertiesMethod);
                propertiesClass = env.findClass(Properties.class);
                MemorySegment putMethod = env.getMethodId(propertiesClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                previousValue = env.callObjectMethod(propertiesRef, putMethod, propertyName = env.newStringUTF(PROPERTY_NAME), implLookupRef);
            } finally {
                env.deleteGlobalRef(lookupClass);
                env.deleteGlobalRef(implLookupRef);
                env.deleteGlobalRef(systemClass);
                env.deleteGlobalRef(propertiesRef);
                env.deleteGlobalRef(propertiesClass);
                env.deleteGlobalRef(propertyName);
                env.deleteGlobalRef(previousValue);
            }
        }

        Object implLookup = System.getProperties().remove(PROPERTY_NAME);
        if (implLookup == null) {
            throw new NullPointerException(PROPERTY_NAME + " is null after successful JNI call");
        } else if (!(implLookup instanceof MethodHandles.Lookup)) {
            throw new ClassCastException(PROPERTY_NAME + " is not a MethodHandles.Lookup but " + implLookup.getClass().getName());
        } else {
            return (MethodHandles.Lookup) implLookup;
        }
    }

    public static Path getLibJVMPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String javaHome = System.getProperty("java.home");
        Path libraryPath;
        if (os.contains("win")) {
            libraryPath = Paths.get(javaHome, "bin", "server", "jvm.dll");
        } else if (os.contains("mac")) {
            libraryPath = Paths.get(javaHome, "lib", "server", "libjvm.dylib");
        } else {
            libraryPath = Paths.get(javaHome, "lib", "server", "libjvm.so");
        }
        if (!Files.exists(libraryPath)) {
            throw new IllegalStateException(libraryPath + " does not exist");
        }
        return libraryPath;
    }


    private record JNI(Arena arena, SymbolLookup lookup) {

        public MemorySegment getVM() throws Throwable {
            MemorySegment vmRef = this.arena.allocate(ValueLayout.ADDRESS);
            MemorySegment vmCount = this.arena.allocate(ValueLayout.JAVA_INT);
            checkError((int) this.getFunction(
                    "JNI_GetCreatedJavaVMs",
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            ).invokeExact(
                    vmRef,
                    1,
                    vmCount
            ));
            if (vmCount.get(ValueLayout.JAVA_INT, 0) < 1) {
                throw new IllegalStateException("No JavaVM available");
            }
            return vmRef.get(ValueLayout.ADDRESS, 0);
        }

        private MethodHandle getFunction(final String function, final FunctionDescriptor descriptor) {
            return Linker.nativeLinker().downcallHandle(this.lookup.find(function).orElseThrow(), descriptor);
        }

        private static void checkError(final int err) {
            if (err != JNI_OK) {
                switch (err) {
                    case JNI_ERR -> throw new RuntimeException("Unknown JNI error");
                    case JNI_EDETACHED -> throw new IllegalStateException("Thread detached");
                    case JNI_EVERSION -> throw new IllegalArgumentException("Unknown JNI version");
                    case JNI_ENOMEM -> throw new OutOfMemoryError("JNI out of memory");
                    case JNI_EEXIST -> throw new IllegalStateException("VM already created");
                    case JNI_EINVAL -> throw new IllegalArgumentException("Invalid arguments");
                    default -> throw new RuntimeException("Unknown JNI error code: " + err);
                }
            }
        }

    }

    private record JNIVM(Arena arena, MemorySegment vm) {

        private static final int VERSION = (21 << 16) | 0;
        private static final int GET_ENV = 6;

        public MemorySegment getJNIEnv() throws Throwable {
            MemorySegment env = this.arena.allocate(ValueLayout.ADDRESS);
            JNI.checkError((int) this.getFunction(
                    GET_ENV,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            ).invokeExact(
                    env,
                    VERSION
            ));
            return env.get(ValueLayout.ADDRESS, 0);
        }

        private MethodHandle getFunction(final int function, final FunctionDescriptor descriptor) {
            MemorySegment functionPointer = this.vm.reinterpret(PTR_SIZE)
                    .get(ValueLayout.ADDRESS, 0)
                    .reinterpret((function + PTR_SIZE) * PTR_SIZE)
                    .getAtIndex(ValueLayout.ADDRESS, function);
            return Linker.nativeLinker().downcallHandle(functionPointer, descriptor).bindTo(this.vm);
        }

    }

    private record JNIEnv(Arena arena, MemorySegment env) {

        private static final int FIND_CLASS = 6;
        private static final int NEW_GLOBAL_REF = 21;
        private static final int DELETE_GLOBAL_REF = 22;
        private static final int GET_METHOD_ID = 33;
        private static final int CALL_OBJECT_METHOD = 34;
        private static final int GET_STATIC_METHOD_ID = 113;
        private static final int CALL_STATIC_OBJECT_METHOD = 114;
        private static final int GET_STATIC_FIELD_ID = 144;
        private static final int GET_STATIC_OBJECT_FIELD = 145;
        private static final int NEW_STRING_UTF = 167;

        public MemorySegment findClass(final Class<?> clazz) throws Throwable {
            return (MemorySegment) this.asGlobalRef(this.getFunction(
                    FIND_CLASS,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            )).invokeExact(
                    this.arena.allocateFrom(clazz.getName().replace('.', '/'))
            );
        }

        public void deleteGlobalRef(@Nullable final MemorySegment ref) throws Throwable {
            if (ref == null) return;
            this.getFunction(
                    DELETE_GLOBAL_REF,
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            ).invokeExact(
                    ref
            );
        }

        public MemorySegment getMethodId(final MemorySegment clazz, final String name, final String signature) throws Throwable {
            return (MemorySegment) this.getFunction(
                    GET_METHOD_ID,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            ).invokeExact(
                    clazz,
                    this.arena.allocateFrom(name),
                    this.arena.allocateFrom(signature)
            );
        }

        public MemorySegment callObjectMethod(final MemorySegment object, final MemorySegment method, final MemorySegment... args) throws Throwable {
            ValueLayout[] argsLayout = new ValueLayout[args.length + 3];
            Arrays.fill(argsLayout, ValueLayout.ADDRESS);
            return (MemorySegment) this.asGlobalRef(this.getFunction(
                    CALL_OBJECT_METHOD,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, argsLayout)
            )).asSpreader(MemorySegment[].class, args.length).invokeExact(
                    object,
                    method,
                    args
            );
        }

        public MemorySegment getStaticMethodId(final MemorySegment clazz, final String name, final String signature) throws Throwable {
            return (MemorySegment) this.getFunction(
                    GET_STATIC_METHOD_ID,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            ).invokeExact(
                    clazz,
                    this.arena.allocateFrom(name),
                    this.arena.allocateFrom(signature)
            );
        }

        public MemorySegment callStaticObjectMethod(final MemorySegment clazz, final MemorySegment method, final MemorySegment... args) throws Throwable {
            ValueLayout[] argsLayout = new ValueLayout[args.length + 3];
            Arrays.fill(argsLayout, ValueLayout.ADDRESS);
            return (MemorySegment) this.asGlobalRef(this.getFunction(
                    CALL_STATIC_OBJECT_METHOD,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, argsLayout)
            )).asSpreader(MemorySegment[].class, args.length).invokeExact(
                    clazz,
                    method,
                    args
            );
        }

        public MemorySegment getStaticFieldId(final MemorySegment clazz, final String name, final String signature) throws Throwable {
            return (MemorySegment) this.getFunction(
                    GET_STATIC_FIELD_ID,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            ).invokeExact(
                    clazz,
                    this.arena.allocateFrom(name),
                    this.arena.allocateFrom(signature)
            );
        }

        public MemorySegment getStaticObjectField(final MemorySegment clazz, final MemorySegment field) throws Throwable {
            return (MemorySegment) this.asGlobalRef(this.getFunction(
                    GET_STATIC_OBJECT_FIELD,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            )).invokeExact(
                    clazz,
                    field
            );
        }

        public MemorySegment newStringUTF(final String string) throws Throwable {
            return (MemorySegment) this.asGlobalRef(this.getFunction(
                    NEW_STRING_UTF,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            )).invokeExact(
                    this.arena.allocateFrom(string)
            );
        }


        private MethodHandle getFunction(final int function, final FunctionDescriptor descriptor) {
            MemorySegment functionPointer = this.env.reinterpret(PTR_SIZE)
                    .get(ValueLayout.ADDRESS, 0)
                    .reinterpret((function + PTR_SIZE) * PTR_SIZE)
                    .getAtIndex(ValueLayout.ADDRESS, function);
            return Linker.nativeLinker().downcallHandle(functionPointer, descriptor).bindTo(this.env);
        }

        private MethodHandle asGlobalRef(final MethodHandle function) {
            MethodHandle newGlobalRef = this.getFunction(NEW_GLOBAL_REF, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            return MethodHandles.filterReturnValue(function, newGlobalRef);
        }

    }

}
