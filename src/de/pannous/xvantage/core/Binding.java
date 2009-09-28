package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.Helper;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Binds a path to a class, then it is possible to serialize several objects into
 * this path.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Binding<T> {

    private String elementName;
    private String pathName;
    private Class<T> clazz;
    private Map<String, Method> getterMethods;
    private Map<String, Method> setterMethods;
    private Set<String> ignoreMethods;

    public Binding(String pathAndElement, Class<T> clazz) {
        if (clazz == null)
            throw new NullPointerException("Clazz cannot be null to create a binding: " + pathAndElement);

        this.clazz = clazz;
        getterMethods = new HashMap();
        setterMethods = new HashMap();
        ignoreMethods = new HashSet();
        for (Method method : clazz.getMethods()) {

            String xmlElement = Helper.getPropertyFromJavaMethod(method.getName(), method.getReturnType() == boolean.class);
            if (xmlElement != null) {
                method.setAccessible(true);

                if (Helper.isSetter(method)) {
                    setterMethods.put(xmlElement, method);
                } else if (Helper.isGetter(method)) {
                    if (!method.getReturnType().equals(Class.class))
                        getterMethods.put(xmlElement, method);
                }
            }
        }

        if (pathAndElement != null) {
            if (!pathAndElement.startsWith("/"))
                pathAndElement = "/" + pathAndElement;

            int lastIndex = pathAndElement.lastIndexOf('/');
            if (lastIndex < 0) {
                throw new IllegalStateException("Cannot happen");
            }

            elementName = pathAndElement.substring(lastIndex + 1).trim();
            pathName = pathAndElement.substring(0, lastIndex + 1);
        }

        if (elementName == null || elementName.length() == 0) {
            elementName = Helper.getJavaModifier(clazz.getSimpleName());
        }
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getPath() {
        return pathName;
    }

    public void setPath(String pathName) {
        this.pathName = pathName;
    }

    public Class getClassObject() {
        return clazz;
    }

    public Map<String, Method> getGetterMethods() {
        return getterMethods;
    }

    public void setGetterMethods(Map<String, Method> getterMethods) {
        this.getterMethods = getterMethods;
    }

    public Map<String, Method> getSetterMethods() {
        return setterMethods;
    }

    public void setSetterMethods(Map<String, Method> setterMethods) {
        this.setterMethods = setterMethods;
    }

    public void ignoreMethod(String method) {
        ignoreMethods.add(method);
    }

    public boolean shouldIgnore(String name) {
        return ignoreMethods.contains(name);
    }

    @Override
    public String toString() {
        return clazz.getName() + " -> " + elementName;
    }
}
