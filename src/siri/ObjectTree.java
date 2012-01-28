package siri;

import org.codehaus.jackson.node.ObjectNode;

public final class ObjectTree {

    ObjectNode m_node;

    public ObjectTree(ObjectNode a_node) {
        m_node = a_node;
    }

    ObjectNode getNode() {
        return m_node;
    }    
}
