package net.lenni0451.reflect.stream.utils;

public class Sneaky {

    public static <T extends Throwable> void sthrow(final Throwable t) throws T {
        throw (T) t;
    }

}
