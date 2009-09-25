package de.pannous.xvantage.core;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.ObjectWithCollections;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.TesterMain;
import java.io.InputStream;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingTest extends TesterMain{

    public BindingTest() {
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testParse() throws Exception {
        Binding bind = new Binding(objectParser, "/test", SimpleObj.class);
        SimpleObj obj = (SimpleObj) ((Entry<Long, Object>) bind.parseObject("<test><name>name1</name></test>")).getValue();
        assertEquals("name1", obj.getName());
    }

    @Test
    public void testCreate() {
        Binding bind = new Binding(objectParser, "simpleObj", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());
        assertEquals(SimpleObj.class, bind.getClassObject());

        bind = new Binding(objectParser, "/simpleObj", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());

        bind = new Binding(objectParser, "", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/", bind.getPath());

        bind = new Binding(objectParser, "/test/", SimpleObj.class);
        assertEquals("simpleObj", bind.getElementName());
        assertEquals("/test/", bind.getPath());

        bind = new Binding(objectParser, "/test/pest/simpleobj", SimpleObj.class);
        assertEquals("simpleobj", bind.getElementName());
        assertEquals("/test/pest/", bind.getPath());
    }

    @Test
    public void testParseObjectWithCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithCollection.xml");
        Binding bind = new Binding(objectParser, "/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        Entry<Long, Object> res = bind.parseObject(str);
        ObjectWithCollections owc = (ObjectWithCollections) res.getValue();
        assertEquals(3, owc.getStringMap().size());
        assertEquals("test1", owc.getStringMap().get(1));
        assertNotNull(owc.getStringArray());
        assertEquals("str1", owc.getStringArray()[0]);
    }

    @Test
    public void testParseObjectWithEmptyCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithEmptyCollection.xml");
        Binding bind = new Binding(objectParser, "/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        Entry<Long, Object> res = bind.parseObject(str);
        ObjectWithCollections owc = (ObjectWithCollections) res.getValue();
        assertEquals(0, owc.getStringMap().size());
        assertEquals(0, owc.getStringMap().size());
    }
}
