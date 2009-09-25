package de.pannous.xvantage.core.util.test;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ComplexObject {

    private String name;
    private float yourFloat;
    private Long id;

    private ComplexObject() {
    }

    public ComplexObject(String n) {
        setName(n);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getYourFloat() {
        return yourFloat;
    }

    public void setYourFloat(float yourFloat) {
        this.yourFloat = yourFloat;
    }

    @Override
    public String toString() {
        return "name:" + name;
    }
}
