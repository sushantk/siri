package siri;

import java.io.InputStream;

public class Data {

    private InputStream m_stream;
    
    public Data() {
    }

    InputStream getStream() {
        return m_stream;
    }
    
    void setStream(InputStream a_stream) {
        m_stream = a_stream;
    }
    
}
