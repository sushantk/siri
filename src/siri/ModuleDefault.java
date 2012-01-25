package siri;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleDefault extends Configurable 
                           implements IModule, IRequestCallback {

    static final Logger s_logger = LoggerFactory.getLogger(ModuleDefault.class);

    static public interface IController {
        ISource getSource(Context a_context, Map<String, ISource> a_sources);
        IRenderer getRenderer(Context a_context, Map<String, IRenderer> a_renderers);
    }
    
    static public class Controller implements IController {

        @Override
        public ISource getSource(Context a_context, Map<String, ISource> a_sources) {
            return a_sources.get(Consts._default);
        }
        
        @Override        
        public IRenderer getRenderer(Context a_context, Map<String, IRenderer> a_renderers) {
            return a_renderers.get(Consts._default);
        }
    }

    private IController m_controller;
    private Map<String, ISource> m_sources;
    private Map<String, IRenderer> m_renderers;
    
    private Context m_context;
    private IRequestCallback m_callback;

    @SiriParameter(defaultClass="siri.SourceWebService")
    public void setSource(IConfigurable a_source) {
        m_sources.put(Consts._default, (ISource)a_source);
    }

    public ModuleDefault() {
        m_sources = new TreeMap<String, ISource>();
        m_renderers = new TreeMap<String, IRenderer>();
    }

    @Override
    public Result execute(Context a_context, IRequestCallback a_callback) {
        m_context = a_context;
        m_callback = a_callback;

        if(null == m_controller) m_controller = new Controller();
        
        ISource source = m_controller.getSource(a_context, m_sources);
        if(null == source) source = new SourceDummy();
        // sources must call us back for runtime failure, failed get call is fatal
        return source.get(a_context, this);
    }
    
    @Override
    public Data render(Context a_context, Data a_data) {
        if(a_context.isLogging()) s_logger.debug("{} - render", a_context);
        
        IRenderer renderer = m_controller.getRenderer(a_context, m_renderers);
        if(null == renderer) renderer = new RendererIdentity();
        return renderer.transform(a_context, a_data);
    }

    @Override
    public void done(Context a_context, Data a_data) {
        // TODO: Give controller a callback to see if it wants to execute more source
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.done(a_context, a_data);
    }

    @Override
    public void failed(Context a_context, Data a_data, boolean a_timedout) {
        // TODO: Give controller a callback to see if it wants to execute more source
        // a_context.set(Consts.renderer_id, Consts._default);
        m_callback.failed(a_context, a_data, a_timedout);
    }
}
