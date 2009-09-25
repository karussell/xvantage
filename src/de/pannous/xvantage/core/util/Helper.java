/*
 *  Copyright 2009 Peter Karich, peat_hal ‘at’ users ‘dot’ sourceforge ‘dot’ net.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.pannous.xvantage.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Peter Karich, peat_hal ‘at’ users ‘dot’ sourceforge ‘dot’ net
 */
public class Helper {

    //                                                       01234567890  12345678
    private static final String localDateTimeFormatString = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Liefert einen DOM Parser zurück.
     */
    public static DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder;
    }

    public static Document getAsDocument(String xmlString) throws SAXException,
            IOException, ParserConfigurationException {
        return newDocumentBuilder().parse(
                new ByteArrayInputStream(xmlString.getBytes()));
    }

    public static Document parse(InputStream is) throws SAXException,
            IOException, ParserConfigurationException {
        return newDocumentBuilder().parse(is);
    }

    public static String getDocumentAsString(Document doc)
            throws TransformerException {
        StringWriter writer = new StringWriter();

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    public static String getFromFile(File file, int blockSize)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char character[] = new char[blockSize];
        StringBuilder sb = new StringBuilder(character.length);
        int ret;
        while ((ret = reader.read(character)) != -1) {
            sb.append(character, 0, ret);
        }

        return sb.toString();
    }

    public static String getAsString(InputStream is, int blockSize)
            throws IOException {
        BufferedInputStream reader = new BufferedInputStream(is);
        byte bytes[] = new byte[blockSize];
        StringBuilder sb = new StringBuilder(bytes.length);
        int ret;
        while ((ret = reader.read(bytes)) != -1) {
            sb.append(new String(bytes, 0, ret));
        }

        return sb.toString();
    }

    /**
     * This method counts the number of matches in the specified pattern in the
     * string str.
     */
    public static int countPattern(String str, String pattern) {
        int counter = 0;
        int index = 0;
        while ((index = str.indexOf(pattern, index)) > -1) {
            counter++;
            index++;
        }
        return counter;
    }

    public static String beautifyXML(String s) {
        return s.replace(">", ">\n");
    }

    /**
     * This method is useful to convert xml to embed this in
    /**
     * This method reads the specified string as date.
     * The info about the time zone will be neglected (e.g. -07:00).
     */
    public static Date fromLocalDateTime(String dateAsString)
            throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(localDateTimeFormatString);

        if (dateAsString.length() >= 19)
            dateAsString = dateAsString.substring(0, 19);

        return df.parse(dateAsString);
    }

    /**
     * This method returns a char from the specified date.
     *
     * @return string of Fdate in local time.
     */
    public static String toLocalDateTime(Date date) {
        return new SimpleDateFormat(localDateTimeFormatString).format(date);
    }

    public static Element getFirstElement(NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE)
                return (Element) list.item(i);
        }
        return null;
    }

    public static String getJavaModifier(String simpleName) {
        if (simpleName == null || simpleName.length() == 0)
            return simpleName;
        return Character.toLowerCase(simpleName.charAt(0)) +
                simpleName.substring(1);
    }

    // getName => name; setName => name; isSth => sth
    public static String getPropertyFromJavaMethod(String name, boolean bool) {
        if (bool) {
            //is/set
            if (name.charAt(0) == 'i' && name.charAt(1) == 's' && name.length() > 2)
                return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        //get/set
        if ((name.charAt(0) == 'g' || name.charAt(0) == 's') && name.charAt(1) == 'e' && name.charAt(2) == 't' && name.length() > 3)
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);

        return null;
    }

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getReturnType() == void.class && method.getParameterTypes().length == 1;
    }

    public static boolean isGetter(Method method) {
        return (method.getName().startsWith("is") || method.getName().startsWith("get")) &&
                method.getReturnType() != void.class && method.getParameterTypes().length == 0;
    }

    public static <T> Constructor<T> getPrivateConstructor(Class<T> aClass) throws NoSuchMethodException {
        Constructor constr = aClass.getDeclaredConstructor();
        constr.setAccessible(true);
        return constr;
    }

    public static Element getRootFromString(String str) throws SAXException, IOException, ParserConfigurationException {
        return getAsDocument(str).getDocumentElement();
    }
}
