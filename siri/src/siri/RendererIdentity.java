package siri;

public class RendererIdentity implements IRenderer {

    @Override
    public Data transform(Context a_context, Data a_data) {
        return a_data;
    }

}
