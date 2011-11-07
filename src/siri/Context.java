package siri;

public class Context {
    private RequestContext m_requestContext;
    
    private String m_iid;
    private String m_moduleId;
    private String m_string;

    private boolean m_logging;

    Context(RequestContext a_requestContext, String a_iid, String a_moduleId) {
        m_requestContext = a_requestContext;

        m_iid = a_iid;
        m_moduleId = a_moduleId;
        
        //TODO: Use request context to check filters
        m_logging = true;
    }

    public RequestContext getRequestContext() {
        return m_requestContext;
    }
    
    public void set(String a_key, Object a_value) {        
    }

    public String get(String a_key) {
        return null;
    }

    public String getInstanceId() {
        return m_iid;
    }

    public String getModuleId() {
        return m_moduleId;
    }
    
    public boolean isLogging() {
        return m_logging;
    }
    
    public String toString() {
        if(null != m_string) {
            return m_string;
        }
        
        m_string = "Context[ " + m_moduleId + " ][ " + m_iid + " ]";
        return m_string;
    }

}
