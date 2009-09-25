/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.TesterMain;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectStringTransformerTest extends TesterMain {

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
    public void testParse() throws Exception {
        assertEquals(3.56f, objectParser.parsePrimitiveOrCollection(Float.class, Helper.getRootFromString("<t>3.56</t>")));
        assertEquals(3L, objectParser.parsePrimitiveOrCollection(Long.class, Helper.getRootFromString("<t>3</t>")));
        assertEquals(4, objectParser.parsePrimitiveOrCollection(int.class, Helper.getRootFromString("<t>4</t>")));
        assertEquals(true, objectParser.parsePrimitiveOrCollection(boolean.class, Helper.getRootFromString("<t>true</t>")));

        assertTrue('c' == objectParser.parsePrimitiveOrCollection(char.class, Helper.getRootFromString("<t>c</t>")));
        assertEquals("", objectParser.parsePrimitiveOrCollection(char.class, Helper.getRootFromString("<t></t>")));
        assertEquals("", objectParser.parsePrimitiveOrCollection(char.class, Helper.getRootFromString("<t/>")));
    }

    @Test
    public void testParseCollections() throws Exception {
        List list = (List) objectParser.parsePrimitiveOrCollection(ArrayList.class,
                Helper.getRootFromString("<enclosing valueClass=\"String\"><value>1</value><value>2</value></enclosing>"));
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }

    @Test
    public void testParseMap() throws Exception {
        Map map = (HashMap) objectParser.parsePrimitiveOrCollection(HashMap.class,
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
        Long[] list = (Long[]) objectParser.parsePrimitiveOrCollection(Long[].class,
                Helper.getRootFromString("<enclosing valueClass=\"long\"><value>2</value></enclosing>"));
        assertEquals(1, list.length);
        assertTrue(2L == list[0]);
    }

    @Test
    public void testDoNotParseWithoutValueclassAttribute_ButDoNotThrowAnException() throws Exception {
        List list = (List) objectParser.parsePrimitiveOrCollection(ArrayList.class,
                Helper.getRootFromString("<enclosing><value>2</value></enclosing>"));
        assertEquals(0, list.size());
    }

    @Test
    public void testWriteArrayList() throws Exception {
        ArrayList al = new ArrayList();
        al.add("Test1");
        al.add("Test2");

        objectParser.writePrimitiveOrCollection(al, ArrayList.class, "list", transformerHandler);
        assertEquals("<list valueClass=\"string\">" +
                "<value>Test1</value>" +
                "<value>Test2</value>" +
                "</list>", writer.toString());
    }

    @Test
    public void testWriteArray() throws Exception {
        String[] array = {"Test1", "Test2"};

        objectParser.writePrimitiveOrCollection(array, String[].class, "list", transformerHandler);
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

        objectParser.writePrimitiveOrCollection(map, Map.class, "list", transformerHandler);

        String str = writer.toString();
        assertTrue(str.contains("keyClass=\"integer\""));
        assertTrue(str.contains("valueClass=\"string\""));
        assertTrue(str.contains("<list"));
        assertTrue(str.contains("<entry><key>1</key><value>test</value></entry>"));
        assertTrue(str.contains("<entry><key>2</key><value>test2</value></entry>"));
    }
}
