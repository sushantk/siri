package siri;

import org.codehaus.jackson.JsonNode;

public final class ObjectTree {

    JsonNode m_node;

    public ObjectTree(JsonNode a_node) {
        m_node = a_node;
    }

    JsonNode getNode() {
        return m_node;
    }    
}
