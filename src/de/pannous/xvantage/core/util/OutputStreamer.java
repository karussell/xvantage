package de.pannous.xvantage.core.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class OutputStreamer extends OutputStream {

    public OutputStreamer() {

    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
