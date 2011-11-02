package siri;

public abstract class Configurable implements IConfigurable {
    
    public Configurable(ConfigTree a_tree) {
        assert a_tree != null;
        m_tree = a_tree;
    }
    
    protected ConfigTree m_tree;
}
