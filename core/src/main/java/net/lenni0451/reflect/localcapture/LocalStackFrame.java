package net.lenni0451.reflect.localcapture;

import org.intellij.lang.annotations.MagicConstant;

public interface LocalStackFrame {

    int MODE_INTERPRETED = 0x01;
    int MODE_COMPILED = 0x02;

    /**
     * Get the binary name of the declaring class of the method represented by this stack frame.
     *
     * @return The binary name of the declaring class
     */
    String getClassName();

    /**
     * Get the name of the method represented by this stack frame.
     *
     * @return The name of the method
     */
    String getMethodName();

    /**
     * Get the declaring class of the method represented by this stack frame.
     *
     * @return The declaring class
     */
    Class<?> getDeclaringClass();

    /**
     * Get the index to the code array of the {@code Code} attribute containing the execution point represented by this stack frame.<br>
     * The code array gives the actual bytes of Java Virtual Machine code that implement the method.
     *
     * @return The index to the code array
     */
    int getByteCodeIndex();

    /**
     * Get the name of the source file containing the execution point represented by this stack frame.<br>
     * Generally, this corresponds to the {@code SourceFile} attribute of the relevant {@code class} file as defined by <cite>The Java Virtual Machine Specification</cite>.<br>
     * In some systems, the name may refer to some source code unit other than a file, such as an entry in a source repository.
     *
     * @return The name of the source file
     */
    String getFileName();

    /**
     * Get the line number of the source line containing the execution point represented by this stack frame.<br>
     * Generally, this is derived from the {@code LineNumberTable} attribute of the relevant {@code class} file as defined by <cite>The Java Virtual Machine Specification</cite>.
     *
     * @return The line number of the source line
     */
    int getLineNumber();

    /**
     * Get if the method containing the execution point represented by this stack frame is a native method.
     *
     * @return {@code true} if the method is native, {@code false} otherwise
     */
    boolean isNativeMethod();

    /**
     * Convert the stack frame to a {@link StackTraceElement} object.
     *
     * @return The stack trace element
     */
    StackTraceElement toStackTraceElement();

    /**
     * Get the mode of the stack frame.<br>
     * This is either {@link #MODE_INTERPRETED} or {@link #MODE_COMPILED} (at the time of writing this).
     *
     * @return The mode of the stack frame
     */
    @MagicConstant(intValues = {MODE_INTERPRETED, MODE_COMPILED})
    int getMode();

    /**
     * Get the monitors of the stack frame.<br>
     * This is a list of all objects that are currently locked by the thread represented by this stack frame.
     *
     * @return The monitors of the stack frame
     */
    Object[] getMonitors();

    /**
     * Get the locals of the stack frame.<br>
     * This is a list of all local variables in the method represented by this stack frame.<br>
     * The locals are represented as an array of objects, where each object is either a reference to an object or a primitive value wrapped in a {@link PrimitiveValue} object.
     *
     * @return The locals of the stack frame
     */
    Object[] getLocals();

    /**
     * Get the stack of the stack frame.<br>
     * This is a list of all values on the operand stack of the method represented by this stack frame.<br>
     * The stack is represented as an array of objects, where each object is either a reference to an object or a primitive value wrapped in a {@link PrimitiveValue} object.
     *
     * @return The stack of the stack frame
     */
    Object[] getStack();

}
