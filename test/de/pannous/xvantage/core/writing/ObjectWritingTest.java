package de.pannous.xvantage.core.writing;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.XvantageTester;
import de.pannous.xvantage.core.util.test.ObjectWithNestedObject;
import de.pannous.xvantage.core.util.test.ObjectWithPolymorph;
import de.pannous.xvantage.core.util.test.Person;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.Task;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
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
public class ObjectWritingTest extends XvantageTester {

    private StringWriter writer;
    private TransformerHandler transformerHandler;

    public ObjectWritingTest() {
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
    public void testWriteBitSet() throws Exception {
        BitSet set = new BitSet();
        set.set(3);
        set.set(4);
        set.set(7);

        writing.writeObject(set, BitSet.class, "set", transformerHandler);
        assertEquals("<set>{3, 4, 7}</set>", writer.toString());
    }

    @Test
    public void testWriteObjectWithEmptyCollection() throws Exception {
        ArrayList al = new ArrayList();

        writing.writeObject(al, ArrayList.class, "list", transformerHandler);
        assertEquals("<list/>", writer.toString());
    }

    @Test
    public void testWriteArrayList() throws Exception {
        ArrayList al = new ArrayList();
        al.add("Test1");
        al.add("Test2");

        writing.writeObject(al, ArrayList.class, "list", transformerHandler);
        assertEquals("<list valueClass=\"String\">" +
                "<value>Test1</value>" +
                "<value>Test2</value>" +
                "</list>", writer.toString());
    }

    @Test
    public void testWriteArray() throws Exception {
        String[] array = {"Test1", "Test2"};

        writing.writeObject(array, String[].class, "list", transformerHandler);
        assertEquals("<list valueClass=\"String\">" +
                "<value>Test1</value>" +
                "<value>Test2</value>" +
                "</list>", writer.toString());
    }

    @Test
    public void testWriteMap() throws Exception {
        Map map = new HashMap();
        map.put(1, "test");
        map.put(2, "test2");

        writing.writeObject(map, Map.class, "list", transformerHandler);

        String str = writer.toString();
        assertTrue(str.contains("keyClass=\"Integer\""));
        assertTrue(str.contains("valueClass=\"String\""));
        assertTrue(str.contains("<list"));
        assertTrue(str.contains("<entry><key>1</key><value>test</value></entry>"));
        assertTrue(str.contains("<entry><key>2</key><value>test2</value></entry>"));
    }

    @Test
    public void testWriteObjectWithReferences() throws Exception {
        Person p1 = new Person("p1", 1L);
        p1.getTasks().add(new Task("t1", 1L));
        p1.setMainTask(new Task("t4", 4L));

        Map<Long, Person> persons = dataPool.getData(Person.class);
        persons.put(p1.getId(), p1);

        writing.writeObject(p1, Person.class, "p", transformerHandler);

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
        bind.ignoreMethod("getName");
        Map<Long, Person> objects = (Map<Long, Person>) dataPool.getData(p1.getClass());
        objects.put(p1.getId(), p1);
        writing.writePOJO(bind, p1, transformerHandler);

        assertEquals("<person jc=\"de.pannous.xvantage.core.writing.ObjectWritingTest$1\" id=\"1\">" +
                "<mainTask/><tasks jc=\"ArrayList\"/><id>1</id>" +
                "</person>", writer.toString());
    }

    @Test
    public void testWriteObjectWithNull() throws Exception {
        Person p1 = new Person("p1", 1L);
        Binding bind = newBinding("/p/", Person.class);

        Map<Long, Person> objects = (Map<Long, Person>) dataPool.getData(p1.getClass());
        objects.put(p1.getId(), p1);
        writing.setSkipNullProperty(true);
        writing.writePOJO(bind, p1, transformerHandler);
        assertEquals("<person id=\"1\"><name>p1</name><id>1</id></person>", writer.toString());
    }

    @Test
    public void testWriteObjectWithUnknownPropertyClass() throws Exception {
        // SimpleObj -> which has no id but getters
        // EmptyObj  -> which has no id and no getters
        ObjectWithNestedObject obj = new ObjectWithNestedObject("p1");

        // create subclass with another name for simpleObj
        obj.setSimpleObject(new SimpleObj("neu") {
        });

        Binding bind = newBinding("/root/o", ObjectWithNestedObject.class);

        Map<Long, ObjectWithNestedObject> objects = (Map<Long, ObjectWithNestedObject>) dataPool.getData(obj.getClass());
        objects.put(1L, obj);

        writing.writePOJO(bind, obj, transformerHandler);
        assertEquals(
                "<o id=\"1\">" +
                "<simpleObject jc=\"de.pannous.xvantage.core.writing.ObjectWritingTest$2\">" +
                "<name>neu</name>" +
                "</simpleObject>" +
                "<emptyObject/>" +
                "</o>", writer.toString());
    }

    @Test
    public void testWriteObjectWithPropertyDifferentToReturnType() throws Exception {
        ObjectWithPolymorph dad = new ObjectWithPolymorph();
        ObjectWithPolymorph obj = new ObjectWithPolymorph();
        dad.setObject(obj);

        Binding bind = newBinding("/root/o", ObjectWithPolymorph.class);

        Map<Long, ObjectWithPolymorph> objects = dataPool.getData((Class) obj.getClass());
        objects.put(1L, obj);

        obj.getCollection().add("String1");
        obj.getCollection().add((Integer) 4);

        writing.setSkipNullProperty(true);
        writing.writePOJO(bind, obj, transformerHandler);
        String str = writer.toString();

        assertTrue(str.contains("<collection jc=\"ArrayList\" valueClass=\"String\"><value>String1</value><value jc=\"Integer\">4</value></collection>"));
    }
}
