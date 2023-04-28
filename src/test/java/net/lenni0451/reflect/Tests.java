package net.lenni0451.reflect;

public class Tests {

    public static final int JAVA_MAJOR_VERSION;

    static {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.")) javaVersion = javaVersion.substring(2);
        JAVA_MAJOR_VERSION = Integer.parseInt(javaVersion.split("\\.")[0]);
    }

}
