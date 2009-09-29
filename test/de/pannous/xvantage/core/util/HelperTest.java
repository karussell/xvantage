package de.pannous.xvantage.core.util;

import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.SimpleObj;
import java.lang.reflect.Constructor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class HelperTest {

    public HelperTest() {
    }

    @Test
    public void testToJavaModifier() {
        assertEquals("test", Helper.getJavaModifier("Test"));
        assertEquals("testPest", Helper.getJavaModifier("TestPest"));
    }

    @Test
    public void testToProperty() {
        assertEquals("a", Helper.getPropertyFromJavaMethod("isA", true));
        assertEquals("b", Helper.getPropertyFromJavaMethod("setB", false));
        assertEquals("mestable", Helper.getPropertyFromJavaMethod("isMestable", true));
        assertEquals("testable", Helper.getPropertyFromJavaMethod("setTestable", true));

        assertEquals("a", Helper.getPropertyFromJavaMethod("getA", false));
        assertEquals("c", Helper.getPropertyFromJavaMethod("setC", false));
        assertEquals("testable", Helper.getPropertyFromJavaMethod("getTestable", false));
        assertEquals("testable", Helper.getPropertyFromJavaMethod("setTestable", false));
    }
 
    @Test
    public void testPrivateConstructor() {
        try {
            Constructor<SimpleObj> c = Helper.getConstructor(SimpleObj.class);
            assertNotNull(c);
            c.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}
