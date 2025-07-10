package net.lenni0451.reflect.proxy.test;

public class Class2 extends Class1 implements Interface1 {

    @Override
    public int test() {
        return 2;
    }

    @Override
    public int interfaceTest() {
        return 12;
    }

}
