package siri;

public class RendererIdentity extends Configurable
                              implements IRenderer {

    public RendererIdentity(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public Data transform(Context a_context, Data a_data) {
        return a_data;
    }

}
