/*
 * All sources stands of Xvantage under public domain.
 */
package de.pannous.xvantage.core;

import de.pannous.xvantage.core.impl.DefaultDataPool;
import de.pannous.xvantage.core.impl.XHandler;
import de.pannous.xvantage.core.parsing.ObjectParsing;
import de.pannous.xvantage.core.parsing.Parsing;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.writing.ObjectWriting;
import de.pannous.xvantage.core.writing.Writing;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Xvantage {

    private String encoding = "UTF-8";
    private ObjectParsing objParsing;
    private ObjectWriting objWriting;
    private BindingTree bindingTree;
    private Class<? extends DataPool> defaultDataPool = DefaultDataPool.class;
    private boolean skipNull = false;

    public Xvantage() {
        objParsing = new ObjectParsing();
        objWriting = new ObjectWriting();

        bindingTree = new BindingTree(objWriting);
    }

    /**
     * @return readed objects, or empty if no objects found
     */
    public DataPool readObjects(Reader reader) {
        return readObjects(reader, null);
    }

    /**
     * This method reads objects from specified reader and uses already existing
     * objects from dataPool to syncronize references.
     */
    public DataPool readObjects(Reader reader, DataPool dataPool) {
        if (reader == null)
            throw new NullPointerException("Reader cannot be null!");

        try {
            if (dataPool == null) {
                Constructor<? extends DataPool> c = Helper.getPrivateConstructor(defaultDataPool);
                dataPool = c.newInstance();
            }

            objWriting.init(dataPool);
            objParsing.init(dataPool);

            XHandler handler = new XHandler(objParsing, bindingTree);
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            InputSource iSource = new InputSource(reader);
            iSource.setEncoding(encoding);
            xr.parse(iSource);

            return handler.getResult();
        } catch (SAXParseException ex) {
            throw new RuntimeException("Is the content of the xml correct?", ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return readed objects, or empty if no objects found
     */
    public DataPool readObjects(InputStream iStream) {
        return readObjects(iStream, null);
    }

    public DataPool readObjects(InputStream iStream, DataPool pool) {
        if (iStream == null)
            throw new NullPointerException("InputStream cannot be null!");

        try {
            return readObjects(new InputStreamReader(iStream, "UTF-8"), pool);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param dataPool should contain some objects you want to persist
     */
    public Writer saveObjects(Writer writer, DataPool dataPool) {
        if (writer == null)
            throw new NullPointerException("Writer cannot be null!");
        if (dataPool == null)
            throw new NullPointerException("DataPool cannot be null!");

        try {
            objParsing.init(dataPool);
            objWriting.init(dataPool);
            objParsing.setSkipNullProperty(skipNull);
            objWriting.setSkipNullProperty(skipNull);

            bindingTree.saveObjects(dataPool, writer, encoding);
            return writer;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method specifies at which path positions specific objects will/should
     * occur.
     * <br/>
     * Example 1. mount("/path/", YourClass.class) => the xml will look like <br/>
     * &lt;path &gt;<br/>
     * &lt;yourClass&gt; <br/>
     * &lt;prop1&gt;&lt;/prop1&gt;
     * <br/>
     * ...
     * <br/>
     * &lt;yourClass&gt;<br/>
     * &lt;/path&gt;
     * 
     * <br/>
     * <br/>
     * Example 2. mount("/path/obj", YourClass.class) => the xml will look like <br/>
     * &lt;path &gt;<br/>
     * &lt;obj&gt; <br/>
     * &lt;prop1&gt;&lt;/prop1&gt;
     * <br/>
     * ...
     * <br/>
     * &lt;obj&gt;<br/>
     * &lt;/path&gt;
     *
     * @param path the path and optionally an alternative class name
     * @param aClass the class which will be mounted into the path
     */
    public void mount(String path, Class aClass) {
        bindingTree.mount(new Binding(path, aClass));
    }

    /**
     * @return true if ignoring is possible
     */
    public boolean ignoreMethod(Class clazz, String method) {
        Binding bind = bindingTree.getBinding(clazz);
        if (bind != null) {
            bind.ignoreMethod(method);
            return true;
        }
        return false;
    }

    public void setSkipNullProperty(boolean skip) {
        this.skipNull = skip;
    }

    /**
     * To set your own implementation of how an xml string should be read.
     * All none POJOs are suitable for that, all POJOs should be handled by
     * Xvantages' default serialization mechanism.
     */
    public void putParsing(Class clazz, Parsing parsing) {
        objParsing.putParsing(clazz, parsing);
    }

    /**
     * To set your own implementation of how an object should be converted into
     * xml. All none POJOs are suitable for that, all POJOs should be handled by
     * Xvantages' default serialization mechanism.
     */
    public void putWriting(Class clazz, Writing w) {
        objWriting.putWriting(clazz, w);
    }

    /**
     * @param interfc the interface which should trigger a new instance of specified clazz
     * @param clazz the implementation of specified interfc
     */
    public <T> void setDefaultImplementation(Class<T> interfc, Class<? extends T> clazz) {
        if (Map.class.equals(interfc))
            objParsing.setDefaultMapImpl((Class) clazz);
        else if (List.class.equals(interfc))
            objParsing.setDefaultListImpl((Class) clazz);
        else if (Set.class.equals(interfc))
            objParsing.setDefaultSetImpl((Class) clazz);
        else
            throw new UnsupportedOperationException("Currently you can only set the default implementations for List, Map and Set");
    }

    public <T> Class<? extends T> getDefaultImplementation(Class<T> interfc) {
        if (Map.class.equals(interfc))
            return (Class<? extends T>) objParsing.getDefaultMapImpl();
        else if (List.class.equals(interfc))
            return (Class<? extends T>) objParsing.getDefaultListImpl();
        else if (Set.class.equals(interfc))
            return (Class<? extends T>) objParsing.getDefaultSetImpl();
        else
            throw new UnsupportedOperationException("Currently you can only set the default implementations for List, Map and Set");
    }

    public void setDefaultDataPool(Class<? extends DataPool> clazz) {
        defaultDataPool = clazz;
    }
}
