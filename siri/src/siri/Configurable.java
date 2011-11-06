package siri;

public abstract class Configurable implements IConfigurable {
    
    public Configurable(ObjectTree a_tree) {
        assert a_tree != null;
        m_tree = a_tree;
    }
    
    public ObjectTree getObjectTree() {
        return m_tree;
    }
    
    protected ObjectTree m_tree;
}
