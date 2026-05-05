package net.lenni0451.reflect.proxy.test;

public class Class5 {

    int pkgPrivateMethod() {
        return 0;
    }

    public int invokeMethod() {
        return this.pkgPrivateMethod();
    }

}
