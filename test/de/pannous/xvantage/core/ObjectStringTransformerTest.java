/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.BiMap;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.ObjectWithCollections;
import de.pannous.xvantage.core.util.test.Person;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.Task;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
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
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectStringTransformerTest extends XvantageTester {

    private StringWriter writer;
    private TransformerHandler transformerHandler;

    public ObjectStringTransformerTest() {
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
        SimpleObj obj = objectParser.parseObject(bind, "<test><name>name1</name></test>").getValue();
        assertEquals("name1", obj.getName());
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(3.56f, objectParser.parseObject(Float.class, Helper.getRootFromString("<t>3.56</t>")));
        assertEquals(3L, objectParser.parseObject(Long.class, Helper.getRootFromString("<t>3</t>")));
        assertEquals(4, objectParser.parseObject(int.class, Helper.getRootFromString("<t>4</t>")));
        assertEquals(4, objectParser.parseObject(int.class, Helper.getRootFromString("<t>4  </t>")));
        assertEquals(true, objectParser.parseObject(boolean.class, Helper.getRootFromString("<t>true</t>")));

        assertTrue('c' == objectParser.parseObject(char.class, Helper.getRootFromString("<t>c</t>")));
        assertEquals("", objectParser.parseObject(char.class, Helper.getRootFromString("<t></t>")));
        assertEquals("", objectParser.parseObject(char.class, Helper.getRootFromString("<t/>")));
    }

    @Test
    public void testParseCollections() throws Exception {
        List list = (List) objectParser.parseObject(ArrayList.class,
                Helper.getRootFromString("<enclosing valueClass=\"String\"><value>1</value><value>2</value></enclosing>"));
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }

    @Test
    public void testParseMap() throws Exception {
        Map map = (HashMap) objectParser.parseObject(HashMap.class,
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
        Long[] list = (Long[]) objectParser.parseObject(Long[].class,
                Helper.getRootFromString("<enclosing valueClass=\"long\"><value>2</value></enclosing>"));
        assertEquals(1, list.length);
        assertTrue(2L == list[0]);
    }

    @Test
    public void testParseBitSet() throws Exception {
        BitSet set = (BitSet) objectParser.parseObject(BitSet.class,
                Helper.getRootFromString("<value>{0, 4, 7, 9}</value>"));
        assertEquals(4, set.cardinality());
        assertTrue(set.get(4));

        set = (BitSet) objectParser.parseObject(BitSet.class,
                Helper.getRootFromString("<value>{0, 4, 7, 9, }</value>"));
        assertEquals(4, set.cardinality());
        assertTrue(set.get(4));
    }

    @Test
    public void testWriteBitSet() throws Exception {
        BitSet set = new BitSet();
        set.set(3);
        set.set(4);
        set.set(7);

        objectParser.writeObjectAsProperty(set, BitSet.class, "set", transformerHandler);
        assertEquals("<set>{3, 4, 7}</set>", writer.toString());
    }

    @Test
    public void testDoNotParseWithoutValueclassAttribute_ButDoNotThrowAnException() throws Exception {
        List list = (List) objectParser.parseObject(ArrayList.class,
                Helper.getRootFromString("<enclosing><value>2</value></enclosing>"));
        assertEquals(0, list.size());
    }

    @Test
    public void testParseNonePrimitiveObjectWithoutException() throws Exception {
        SimpleObj obj = (SimpleObj) objectParser.parseObject(SimpleObj.class,
                Helper.getRootFromString("<obj id=\"5\"><value>2</value></obj>"));
        assertNotNull(obj);
    }

    @Test
    public void testParseObjectWithCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithCollection.xml");
        Binding bind = newBinding("/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        Entry<Long, ObjectWithCollections> res = objectParser.parseObject(bind, str);
        ObjectWithCollections owc = (ObjectWithCollections) res.getValue();
        assertEquals(3, owc.getStringMap().size());
        assertEquals("test1", owc.getStringMap().get(1));
        assertNotNull(owc.getStringArray());
        assertEquals("str1", owc.getStringArray()[0]);
    }

    @Test
    public void testParseObjectWithEmptyCollection() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithEmptyCollection.xml");
        Binding bind = newBinding("/obj", ObjectWithCollections.class);
        String str = Helper.getAsString(iStream, 1024);
        Entry<Long, ObjectWithCollections> res = objectParser.parseObject(bind, str);
        ObjectWithCollections owc = res.getValue();
        assertEquals(0, owc.getStringMap().size());
        assertEquals(0, owc.getStringMap().size());
    }

    @Test
    public void testParseObjectReferences() throws Exception {
        BiMap<Long, Task> map = dataPool.getData(Task.class);
        Task task = new Task("task1", 1L);
        map.put(1L, task);

        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithReferences.xml");
        Binding<Person> bind = newBinding("/root/", Person.class);
        Entry<Long, Person> res = objectParser.parseObject(bind, Helper.getAsString(iStream, 1024));
        assertEquals((Long) 5L, res.getKey());
        Person p1 = res.getValue();

        assertEquals(2, p1.getTasks().size());
        assertEquals(1L, p1.getTasks().get(0).getId());

        assertTrue("should use existent task", task == p1.getTasks().get(0));
        assertNotNull("should create new task", p1.getTasks().get(1));
    }

    @Test
    public void testParseKnownToOneObject() throws Exception {
        BiMap<Long, Task> map = dataPool.getData(Task.class);
        Task task = new Task("task1", 4L);
        map.put(4L, task);

        InputStream iStream = getClass().getResourceAsStream("bindingTestParseObjectWithReferences.xml");
        Binding<Person> bind = newBinding("/root/", Person.class);

        Entry<Long, Person> res = objectParser.parseObject(bind, Helper.getAsString(iStream, 1024));

        Person p1 = res.getValue();
        assertEquals(2, p1.getTasks().size());

        assertTrue("should create new task", task == p1.getMainTask());
    }

    @Test
    public void testWriteArrayList() throws Exception {
        ArrayList al = new ArrayList();
        al.add("Test1");
        al.add("Test2");

        objectParser.writeObjectAsProperty(al, ArrayList.class, "list", transformerHandler);
        assertEquals("<list valueClass=\"string\">" +
                "<value>Test1</value>" +
                "<value>Test2</value>" +
                "</list>", writer.toString());
    }

    @Test
    public void testWriteArray() throws Exception {
        String[] array = {"Test1", "Test2"};

        objectParser.writeObjectAsProperty(array, String[].class, "list", transformerHandler);
        assertEquals("<list valueClass=\"string\">" +
                "<value>Test1</value>" +
                "<value>Test2</value>" +
                "</list>", writer.toString());
    }

    @Test
    public void testWriteMap() throws Exception {
        Map map = new HashMap();
        map.put(1, "test");
        map.put(2, "test2");

        objectParser.writeObjectAsProperty(map, Map.class, "list", transformerHandler);

        String str = writer.toString();
        assertTrue(str.contains("keyClass=\"integer\""));
        assertTrue(str.contains("valueClass=\"string\""));
        assertTrue(str.contains("<list"));
        assertTrue(str.contains("<entry><key>1</key><value>test</value></entry>"));
        assertTrue(str.contains("<entry><key>2</key><value>test2</value></entry>"));
    }

    @Test
    public void testWriteObjectWithReferences() throws Exception {
        Person p1 = new Person("p1", 1L);
        p1.getTasks().add(new Task("t1", 1L));
        p1.setMainTask(new Task("t4", 4L));

        BiMap<Long, Person> persons = dataPool.getData(Person.class);
        persons.put(p1.getId(), p1);

        objectParser.writeObjectAsProperty(p1, Person.class, "p", transformerHandler);

        String str = writer.toString();
        assertEquals("<p>1</p>", str);
    }

    @Test
    public void testWriteObjectEvenIfGetterThrowsException() throws Exception {
        Person p1 = new Person("p1", 1L) {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("test");
            }
        };
        Binding bind = newBinding("/p/", Person.class);
        objectParser.writeObject(bind, p1, transformerHandler);
        assertEquals("<p><id>1</id></p>", writer.toString());
    }
}
