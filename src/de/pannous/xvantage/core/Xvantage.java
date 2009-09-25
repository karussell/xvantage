/*
 * All sources stands of Xvantage under public domain.
 */
package de.pannous.xvantage.core;

import de.pannous.xvantage.core.impl.DefaultDataPool;
import de.pannous.xvantage.core.impl.XHandler;
import de.pannous.xvantage.core.util.Helper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Xvantage {

    private String encoding = "UTF-8";
    private BindingTree bindingTree = new BindingTree();
    private List<Exception> exceptions = new ArrayList<Exception>();
    private ObjectStringTransformer parser = new ObjectStringTransformer();
    private boolean related = false;
    private Class<? extends DataPool> defaultDataPool = DefaultDataPool.class;

    public Xvantage() {
    }

    /**
     * @return readed objects, or empty if no objects found
     */
    public DataPool readObjects(Reader reader) {
        try {
            Constructor<? extends DataPool> c = Helper.getPrivateConstructor(defaultDataPool);
            DataPool dataPool = c.newInstance();
            XHandler handler = new XHandler(dataPool, bindingTree, exceptions);
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            InputSource iSource = new InputSource(reader);
            iSource.setEncoding(encoding);
            xr.parse(iSource);

            return handler.getResult();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return readed objects, or empty if no objects found
     */
    public DataPool readObjects(InputStream iStream) {
        try {
            return readObjects(new InputStreamReader(iStream, "UTF-8"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param pool should contain some objects you want to persist
     */
    public void saveObjects(DataPool pool, Writer writer) {
        try {
            bindingTree.saveObjects(exceptions, pool, writer, encoding);
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
        bindingTree.mount(new Binding(parser, path, aClass));
    }

    /**
     * @param interfc the interface which should trigger a new instance of specified clazz
     * @param clazz the implementation of specified interfc
     */
    public <T> void setDefaultImplementation(Class<T> interfc, Class<? extends T> clazz) {
        if (Map.class.equals(interfc))
            parser.setDefaultMapImpl((Class) clazz);
        else if (List.class.equals(interfc))
            parser.setDefaultListImpl((Class) clazz);
        else if (Set.class.equals(interfc))
            parser.setDefaultSetImpl((Class) clazz);
        else
            throw new UnsupportedOperationException("Currently you can only set the default implementations for List, Map and Set");
    }

    public <T> Class<? extends T> getDefaultImplementation(Class<T> interfc) {
        if (Map.class.equals(interfc))
            return (Class<? extends T>) parser.getDefaultMapImpl();
        else if (List.class.equals(interfc))
            return (Class<? extends T>) parser.getDefaultListImpl();
        else if (Set.class.equals(interfc))
            return (Class<? extends T>) parser.getDefaultSetImpl();
        else
            throw new UnsupportedOperationException("Currently you can only set the default implementations for List, Map and Set");
    }

    /**
     * @return a list of exceptions, which could occur while parsing or
     * writing a single object.
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }

    /**
     * TODO this should be default behaviour
     * @deprecated 
     */
    public void setRelated(boolean b) {
        related = b;
    }

    public void setDefaultDataPool(Class<? extends DataPool> clazz) {
        defaultDataPool = clazz;
    }
}