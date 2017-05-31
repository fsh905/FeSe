package bid.fese.prepare;

import jdk.internal.org.objectweb.asm.Type;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.Integer.TYPE;

/**
 * Created by feng_sh on 17-5-31.
 */
public class CastTest {

    @Test
    public void test() throws ClassNotFoundException {
        Class<?> cs = Class.forName("bid.fese.prepare.CastTest");
        Method[] ms = cs.getDeclaredMethods();
        for (Method m : ms) {
            System.out.println(m.getName());
            Parameter[] ps = m.getParameters();
            for (Parameter p : ps) {
                System.out.println(p.getType() == Integer.TYPE);
                System.out.println(p.getType().getTypeName());
            }
            Class<?>[] mcs = m.getParameterTypes();
            for (Class<?> mc : mcs) {
                System.out.println(mc == Integer.TYPE);
                System.out.println(mc.getTypeName());
            }
        }
        System.out.println(Class.forName("java.lang.String").cast("hehe"));
    }

    public void castMethod(int a, long b) {
        System.out.println(a + b);
    }

}


