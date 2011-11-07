package siri;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.node.ObjectNode;

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

    static JsonFactory s_jsonFactory = new MappingJsonFactory();

    String m_moduleId;
    Context m_context;
    IModule m_module;
    Data m_finalData;
    
    ModuleHandler(RequestContext a_requestContext, String a_iid, String a_moduleId) {
        m_context = new Context(a_requestContext, a_iid, a_moduleId);
    }
    
    // should take request context
    Result execute() {

        JsonNode node = null;
        String tree = "{\"source\":{\"url\":\"http://news.yahoo.com/rss/\"}}";
        try {
            JsonParser parser = s_jsonFactory.createJsonParser(tree);
            node = parser.readValueAsTree();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        if((null == node) || !node.isObject()) {
            logger.error("{} - Invalid object tree: {}", m_context, tree);
            logger.error("node: {}", node.asToken());
            return Result.INVALID_OBJECT_TREE;
        }
        
        ObjectTree otree = new ObjectTree((ObjectNode)node);
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
