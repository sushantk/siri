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
        ModuleHandler handler1 = new ModuleHandler(requestContext, "iid1", moduleId);
        ModuleHandler handler2 = new ModuleHandler(requestContext, "iid2", moduleId);
        handler1.execute();
        handler2.execute();
        
        TaskManager taskManager = requestContext.getTaskManager();
        taskManager.run();
        
        System.out.println("data1: " + handler1.getData());
        System.out.println("data2: " + handler2.getData());
        
        System.out.println("exit");
     }

    String m_moduleId;
    Context m_context;
    IModule m_module;
    Data m_finalData;
    
    ModuleHandler(RequestContext a_requestContext, String a_iid, String a_moduleId) {
        m_context = new Context(a_requestContext, a_iid, a_moduleId);
    }
    
    // should take request context
    Result execute() {
        String tree = "{\"source\":{\"url\":\"http://news.yahoo.com/rss/\"}}";
        ObjectTree otree = ObjectFactory.parse(m_context, tree);
        if(null == otree)
            return Result.INVALID_OBJECT_TREE;

        m_module = new ModuleDefault(otree);
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
