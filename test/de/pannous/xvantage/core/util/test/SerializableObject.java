package de.pannous.xvantage.core.util.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class SerializableObject implements Serializable {

    private String name;

    public String getName() {
        throw new RuntimeException("Serializable interface should be used");
    }

    public void setName(String name) {
        throw new RuntimeException("Serializable interface should be used");
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(name);
    }

    private void readObject(ObjectInputStream oos) throws IOException, ClassNotFoundException {
        name = (String) oos.readObject();
    }
}
