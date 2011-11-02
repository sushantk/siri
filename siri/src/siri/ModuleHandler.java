package siri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleHandler implements IRequestCallback {
    
    static final Logger logger = LoggerFactory.getLogger(ModuleHandler.class);
    
    public static void main (String[] args) {

        if(args.length == 0) {
            System.out.println("Specify a module id");
            return;
        }
                
        RequestContext requestContext = new RequestContext();
        
        String moduleId = args[ 0 ];
        ModuleHandler handler = new ModuleHandler(requestContext, moduleId, moduleId);
        handler.execute();
        
        TaskManager taskManager = requestContext.getTaskManager();
        taskManager.run();
        
        Data data = handler.getData();
        System.out.println("data: " + data);
        
        System.out.println("exit");
     }

    String m_moduleId;
    private Context m_context;
    private IModule m_module;
    private Data m_finalData;
    
    ModuleHandler(RequestContext a_requestContext, String a_iid, String a_moduleId) {
        m_context = new Context(a_requestContext, a_iid, a_moduleId);
    }
    
    // should take request context
    Result execute() {
        m_module = new ModuleDefault(null);
        return m_module.execute(m_context, this);
    }
    
    Data getData() {
        return m_finalData;
    }

    @Override
    public void done(Context a_context, Data a_data) {
        System.out.println("Module request done: " + a_context.getModuleId());
        m_finalData = m_module.render(a_context, a_data);
    }

    @Override
    public void failed(Context a_context, Data a_data, boolean a_timedout) {
        System.out.println("Module request failed: " + a_context.getModuleId());
    }
}
