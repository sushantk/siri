package siri;

public class RequestContext {

    private TaskManager m_taskManager;

    public RequestContext() {
        // Are we logging?
        m_taskManager = new TaskManager(true);
    }
    
    public TaskManager getTaskManager() {
        return m_taskManager;
    }
    
    public void set(String a_key, Object a_value) {        
    }

    public String get(String a_key) {
        return null;
    }
}
