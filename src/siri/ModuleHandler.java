package siri;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleHandler implements IRequestCallback {
    
    static final Logger logger = LoggerFactory.getLogger(ModuleHandler.class);
    static ThreadPoolExecutor s_rendererExecutor = null;
    
    public static void main (String[] args) {

        if(args.length == 0) {
            System.out.println("Specify a module id");
            return;
        }
        
        int poolSize = 2;
        s_rendererExecutor = new ThreadPoolExecutor(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, 
                new ArrayBlockingQueue<Runnable>(poolSize), new ThreadPoolExecutor.CallerRunsPolicy());
        s_rendererExecutor.prestartAllCoreThreads();
        System.out.println("Active renderer threads: " + s_rendererExecutor.getActiveCount());
        
        RequestContext requestContext = new RequestContext();
        
        String moduleId = args[ 0 ];
        ModuleHandler handler1 = new ModuleHandler(requestContext, "iid1", moduleId);
        ModuleHandler handler2 = new ModuleHandler(requestContext, "iid2", moduleId);
        ModuleHandler handler3 = new ModuleHandler(requestContext, "iid3", moduleId);
        ModuleHandler handler4 = new ModuleHandler(requestContext, "iid4", moduleId);
        handler1.execute();
        handler2.execute();
        handler3.execute();
        handler4.execute();
        
        TaskManager taskManager = requestContext.getTaskManager();
        taskManager.run();
        
        System.out.println("data1: " + handler1.getData());
        System.out.println("data2: " + handler2.getData());
        System.out.println("data3: " + handler3.getData());
        System.out.println("data4: " + handler4.getData());
        
        s_rendererExecutor.shutdown();
        System.out.println("exit");
     }

    String m_moduleId;
    Context m_context;
    IModule m_module;
    Data m_finalData;
    Future<?> m_rendererTask = null;
    
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
        if(null == m_rendererTask) {
            return null;
        }
        
        try {
            m_rendererTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m_finalData;
    }

    @Override
    public void done(Context a_context, Data a_data) {
        System.out.println("Module request done: " + a_context.getModuleId());
        System.out.println("Active renderer threads: " + s_rendererExecutor.getActiveCount());
        
        final Data data = a_data;
        m_rendererTask = s_rendererExecutor.submit(new Runnable() {
            @Override
            public void run() {
                m_finalData = m_module.render(m_context, data);
            }
        });
    }

    @Override
    public void failed(Context a_context, Data a_data, boolean a_timedout) {
        System.out.println("Module request failed: " + a_context.getModuleId());
    }
}
