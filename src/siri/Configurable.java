package siri;

public abstract class Configurable implements IConfigurable {

    private String m_tagName = "";
    private String m_id = "";
    
    public Configurable() {
    }
    
    public String getTag() { return m_tagName; }
    public String getId() { return m_id; }

    public void setTag(String a_tagName) { m_tagName = a_tagName; }
    public void setId(String a_id) { m_id = a_id; }
    
    public String toDebugString() {
        return this.toString();
    }
    
    public String toString() {
        return m_tagName + "<" + this.getClass().getName() + ", " + m_id + ">"; 
    }
}
