package siri;

import org.codehaus.jackson.JsonNode;

public final class ObjectTree {

    String m_tag;
    JsonNode m_node;
    Object m_userData;

    public ObjectTree(String a_tag, JsonNode a_node) {
        m_tag = a_tag;
        m_node = a_node;
    }

    String getTag() {
        return m_tag;
    }

    JsonNode getNode() {
        return m_node;
    }
    
    Object getUserData() {
        return m_userData;
    }
    
    void setUserData(Object a_data) {
        m_userData = a_data;
    }
}
