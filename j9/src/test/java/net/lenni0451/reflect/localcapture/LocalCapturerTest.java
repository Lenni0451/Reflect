package net.lenni0451.reflect.localcapture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalCapturerTest {

    @Test
    void test() {
        String secret = "This is a secret text";
        Assertions.assertEquals(secret, getSecret());
    }

    private static String getSecret() {
        return LocalCapturer.walk(s -> s
                .filter(f -> f.getMethodName().equals("test"))
                .findFirst()
                .map(LocalStackFrame::getLocals)
                .orElseThrow(() -> new IllegalStateException("No locals found"))[1] //0 is the 'this' reference
                .toString());
    }

}
