package de.pannous.xvantage.core.parsing;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.XvantageTester;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.ObjectWithCollectionOfCollection;
import de.pannous.xvantage.core.util.test.ObjectWithCollections;
import de.pannous.xvantage.core.util.test.ObjectWithPolymorph;
import de.pannous.xvantage.core.util.test.Person;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.Task;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectParsingTest extends XvantageTester {

    private StringWriter writer;
    private TransformerHandler transformerHandler;

    public ObjectParsingTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        transformerHandler = tf.newTransformerHandler();
        Transformer serializer = transformerHandler.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformerHandler.setResult(streamResult);
        transformerHandler.startDocument();
    }

    @Test
    public void testParseObject() throws Exception {
        Binding<SimpleObj> bind = newBinding("/test", SimpleObj.class);
        SimpleObj obj = parsing.parseObject(bind, "<test><name>name1</name></test>");
        assertEquals("name1", obj.getName());
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(3.56f, parsing.parseObjectAsProperty(Float.class, Helper.getRootFromString("<t>3.56</t>")));
        assertEquals(3L, parsing.parseObjectAsProperty(Long.class, Helper.getRootFromString("<t>3</t>")));
        assertEquals(4, parsing.parseObjectAsProperty(int.class, Helper.getRootFromString("<t>4</t>")));
        assertEquals(4, parsing.parseObjectAsProperty(int.class, Helper.getRootFromString("<t>4  </t>")));
        assertEquals(true, parsing.parseObjectAsProperty(boolean.class, Helper.getRootFromString("<t>true</t>")));

        assertTrue('c' == parsing.parseObjectAsProperty(char.class, Helper.getRootFromString("<t>c</t>")));
        assertEquals("", parsing.parseObjectAsProperty(char.class, Helper.getRootFromString("<t></t>")));
        assertEquals("", parsing.parseObjectAsProperty(char.class, Helper.getRootFromString("<t/>")));
    }

    @Test
    public void testParseCollections() throws Exception {
        List list = (List) parsing.parseObjectAsProperty(ArrayList.class,
                Helper.getRootFromString("<enclosing valueClass=\"String\"><value>1</value><value>2</value></enclosing>"));
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }

    @Test
    public void testParseMap() throws Exception {
        Map map = (HashMap) parsing.parseObjectAsProperty(HashMap.class,
                Helper.getRootFromString("<stringMap keyClass=\"Integer\" valueClass=\"String\">" +
                "<entry>            <key>1</key>            <value>test1</value>        </entry>" +
                "<entry>            <key>2</key>            <value>test2</value>        </entry>" +
                "</stringMap>"));
        assertEquals(2, map.size());
        assertEquals("test1", map.get(1));
        assertEquals("test2", map.get(2));
    }

    @Test
    public void testParseArray() throws Exception {
        Long[] list = (Long[]) parsing.parseObjectAsProperty(Long[].class,
                Helper.getRootFromString("<enclosing valueClass=\"long\"><value>2</value></enclosing>"));
        assertEquals(1, list.length);
        assertTrue(2L == list[0]);
    }

    @Test
    public void testParseBitSet() throws Exception {
        BitSet set = (BitSet) parsing.parseObjectAsProperty(BitSet.class,
                Helper.getRootFromString("<value>{0, 4, 7, 9}</value>"));
        assertEquals(4, set.cardinality());
        assertTrue(set.get(4));

        set = (BitSet) parsing.parseObjectAsProperty(BitSet.class,
                Helper.getRootFromString("<value>{0, 4, 7, 9, }</value>"));
        assertEquals(4, set.cardinality());
        assertTrue(set.get(4));
    }

    @Test
    public void testDoNotParseWithoutValueclassAttribute_ButDoNotThrowAnException() throws Exception {
        List list = (List) parsing.parseObjectAsProperty(ArrayList.class,
                Helper.getRootFromString("<enclosing><value>2</value></enclosing>"));
        assertEquals(0, list.size());
    }

    @Test
    public void testParseNonePrimitiveObjectWithoutException() throws Exception {
        SimpleObj obj = (SimpleObj) parsing.parseObjectAsProperty(SimpleObj.class,
                Helper.getRootFromString("<obj id=\"5\"><value>2</value></obj>"));
        assertNotNull(obj);
    }

    @Test
    public void testParseObjectWithCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithCollection.xml");
        Binding<ObjectWithCollections> bind = newBinding("/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        ObjectWithCollections owc = parsing.parseObject(bind, str);
        assertEquals(3, owc.getStringMap().size());
        assertEquals("test1", owc.getStringMap().get(1));
        assertNotNull(owc.getStringArray());
        assertEquals("str1", owc.getStringArray()[0]);

        assertEquals(1, owc.getStringCollection().size());
        assertEquals("str2", owc.getStringCollection().iterator().next());
    }

    @Test
    public void testParseObjectWithEmptyCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithEmptyCollection.xml");
        Binding<ObjectWithCollections> bind = newBinding("/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        ObjectWithCollections owc = parsing.parseObject(bind, str);
        assertEquals(0, owc.getStringSet().size());
        assertEquals(0, owc.getStringMap().size());
    }

    @Test
    public void testParseObjectReferences() throws Exception {
        Map<Long, Task> map = dataPool.getData(Task.class);
        Task task = new Task("task1", 1L);
        map.put(1L, task);

        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithReferences.xml");
        Binding<Person> bind = newBinding("/root/", Person.class);
        Person p1 = parsing.parseObject(bind, Helper.getAsString(iStream, 1024));
        assertEquals((Long) 5L, dataPool.getId(p1));

        assertEquals(2, p1.getTasks().size());
        assertEquals(1L, p1.getTasks().get(0).getId());

        assertTrue("should use existent task", task == p1.getTasks().get(0));
        assertNotNull("should create new task", p1.getTasks().get(1));
    }

    @Test
    public void testParseKnownObjectInToOneRelationship() throws Exception {
        Map<Long, Task> map = dataPool.getData(Task.class);
        Task task = new Task("task1", 4L);
        map.put(4L, task);

        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithReferences.xml");
        Binding<Person> bind = newBinding("/root/", Person.class);
        Person p1 = parsing.parseObject(bind, Helper.getAsString(iStream, 1024));
        assertEquals(2, p1.getTasks().size());

        assertTrue("should create new task", task == p1.getMainTask());
    }

    @Test
    public void testParseCollectionOfCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("parseCollectionOfCollection.xml");
        Binding<ObjectWithCollectionOfCollection> bind = newBinding("/obj", ObjectWithCollectionOfCollection.class);

        ObjectWithCollectionOfCollection p1 = parsing.parseObject(bind, Helper.getAsString(iStream, 1024));
        assertEquals(1, p1.getField().size());
        assertEquals(2, p1.getField().get(0).size());
        assertEquals("test1", p1.getField().get(0).get(0).getName());
        assertEquals("test2", p1.getField().get(0).get(1).getName());
    }

    @Test
    public void testReadObjectWithPropertyDifferentToReturnType() throws Exception {
        Binding bind = newBinding("/o", ObjectWithPolymorph.class);

        parsing.setSkipNullProperty(true);        
        ObjectWithPolymorph o = (ObjectWithPolymorph) parsing.parseObject(bind,
                "<o><collection jc=\"ArrayList\" valueClass=\"String\">" +
                "<value>String1</value><value jc=\"Integer\">4</value></collection></o>");
        assertEquals(ArrayList.class, o.getCollection().getClass());
        Iterator iter = o.getCollection().iterator();
        assertEquals(String.class, iter.next().getClass());
        assertEquals(Integer.class, iter.next().getClass());
    }
}
