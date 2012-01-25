package siri;

public class RendererIdentity extends Configurable
                              implements IRenderer {

    public RendererIdentity() {
    }

    @Override
    public Data transform(Context a_context, Data a_data) {
        return a_data;
    }

}
