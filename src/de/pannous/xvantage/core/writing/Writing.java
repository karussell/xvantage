package de.pannous.xvantage.core.writing;

import javax.xml.transform.sax.TransformerHandler;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface Writing {

    /**
     * @param obj cannot be null
     * @return xml representation of the specified object
     */
    void toString(Object obj, TransformerHandler transformerHandler) throws Exception;
}
