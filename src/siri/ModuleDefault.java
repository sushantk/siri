package siri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleDefault extends Configurable 
                           implements IModule, IRequestCallback {

    static final Logger logger = LoggerFactory.getLogger(ModuleDefault.class);

    static public interface IController {
        String getSource(Context a_context, ObjectTree a_tree);
    }

    private Context m_context;
    private IRequestCallback m_callback;

    public ModuleDefault(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public Result execute(Context a_context, IRequestCallback a_callback) {
        m_context = a_context;
        m_callback = a_callback;

        // TODO: ask controller for the source tree
        ObjectTree sourceTree = ObjectFactory.find(m_context, m_tree, Consts.source, null);
        if(null == sourceTree) {
            ObjectFactory.logError(m_context, Consts.module, m_tree, Consts.source, Consts.error.required);
            return Result.INVALID_OBJECT_TREE;
        }
        ISource source = (ISource) ObjectFactory.create(m_context, sourceTree, Consts.SourceWebService);        
        return source.get(a_context, this);
    }
    
    @Override
    public Data render(Context a_context, Data a_data) {
        if(a_context.isLogging()) logger.debug("{} - render", a_context);
        
        // TODO: Ask controller for the renderer tree
        IRenderer renderer = null;
        ObjectTree rendererTree = ObjectFactory.find(m_context, m_tree, Consts.renderer, null);
        if(null != rendererTree) {
            // TODO: default renderer class?
            renderer = (IRenderer) ObjectFactory.create(m_context, rendererTree, Consts.RendererIdentity);
        } else {
            renderer = new RendererIdentity(null);
        }
        return renderer.transform(a_context, a_data);
    }

    @Override
    public void done(Context a_context, Data a_data) {
        // TODO: Ask controller for the renderer id
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.done(a_context, a_data);
    }

    @Override
    public void failed(Context a_context, Data a_data, boolean a_timedout) {
        // TODO: Ask controller for the renderer id
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.failed(a_context, a_data, a_timedout);
    }
}
