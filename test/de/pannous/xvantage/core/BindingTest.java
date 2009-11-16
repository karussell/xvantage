package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.test.SimpleObj;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingTest extends XvantageTester {

    public BindingTest() {
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreate() {
        Binding bind = newBinding("simpleObj", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());
        assertEquals(SimpleObj.class, bind.getClassObject());

        bind = newBinding("/simpleObj", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());

        bind = newBinding("", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());

        bind = newBinding("/test/", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/test/", bind.getPath());

        bind = newBinding("/test/pest/simpleobj", SimpleObj.class);
        assertEquals("simpleobj", bind.getElementName());
        assertEquals("/test/pest/", bind.getPath());
    }
}
