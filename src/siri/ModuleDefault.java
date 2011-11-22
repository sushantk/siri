package siri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleDefault extends Configurable 
                           implements IModule, IRequestCallback {

    static final Logger s_logger = LoggerFactory.getLogger(ModuleDefault.class);

    static public interface IController {
        ObjectTree[] getSource(Context a_context, ObjectTree a_tree);
    }
    
    static public class Controller implements IController {

        @Override
        public ObjectTree[] getSource(Context a_context, ObjectTree a_tree) {
            ObjectTree sourceTree = ObjectFactory.find(a_context, a_tree, Consts.source, true);
            if(null == sourceTree) return null;

            return new ObjectTree[] {sourceTree};
        }
        
    }

    private Context m_context;
    private IRequestCallback m_callback;
    private IController m_controller = null;
    private boolean m_aborted = true;

    public ModuleDefault(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public Result execute(Context a_context, IRequestCallback a_callback) {
        m_context = a_context;
        m_callback = a_callback;

        m_controller = (IController) ObjectFactory.create(a_context, m_tree, Consts.controller, Consts.ControllerDefault, false);
        if(null == m_controller) m_controller = new Controller();
        
        ObjectTree[] sourceTrees = m_controller.getSource(a_context, m_tree);
        if(null == sourceTrees) return Result.FAILED;
        
        for(ObjectTree sourceTree : sourceTrees) {
            if(null != sourceTree) {
                ISource source = (ISource) ObjectFactory.create(m_context, sourceTree, Consts.SourceWebService);
                // sources must call us back for runtime failure, failed get call is fatal
                if(Result.SUCCESS != source.get(a_context, this)) {
                    m_aborted = true;
                    break;
                }
            }
            
            m_aborted = true;
            break;
        }
        
        return Result.SUCCESS;
    }
    
    @Override
    public Data render(Context a_context, Data a_data) {
        if(a_context.isLogging()) s_logger.debug("{} - render", a_context);
        
        // TODO: Ask controller for the renderer tree
        IRenderer renderer = null;
        ObjectTree rendererTree = ObjectFactory.find(m_context, m_tree, Consts.renderer, false);
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
