package siri;

public class ModuleDefault extends Configurable 
                           implements IModule, IRequestCallback {
    
    private Context m_context;
    private IRequestCallback m_callback;

    public ModuleDefault(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public Result execute(Context a_context, IRequestCallback a_callback) {
        m_context = a_context;
        m_callback = a_callback;
        
        // TODO: ask controller for the source id
        ISource source = (ISource) ObjectFactory.create(m_context, this, Consts.source, null, Consts.SourceWebService, true);
        if(null == source) {
            source = new SourceWebService(null);
        }
        
        return source.get(a_context, this);
    }
    
    @Override
    public Data render(Context a_context, Data a_data) {
        // TODO Ask controller for the renderer id
        String rendererId = a_context.get(Consts.renderer_id);
        
        //TODO: stringtemplate?
        IRenderer renderer = (IRenderer) ObjectFactory.create(m_context, this, Consts.renderer, rendererId, Consts.RendererIdentity, false);
        if(null == renderer) {
            renderer = new RendererIdentity(null);
        }
                
        return renderer.transform(a_context, a_data);
    }

    @Override
    public void done(Context a_context, Data a_data) {
        // TODO Ask controller for the renderer id
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.done(a_context, a_data);
    }

    @Override
    public void failed(Context a_context, Data a_data, boolean a_timedout) {
        // TODO Ask controller for the renderer id
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.failed(a_context, a_data, a_timedout);
    }
}
