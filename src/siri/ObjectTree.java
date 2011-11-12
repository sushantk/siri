package siri;

import org.codehaus.jackson.JsonNode;

public final class ObjectTree {

    JsonNode m_root;

    public ObjectTree(JsonNode a_node) {
        m_root = a_node;
    }

    JsonNode getRoot() {
        return m_root;
    }
}
