package siri;

import java.lang.reflect.Constructor;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFactory {
    
    static final Logger logger = LoggerFactory.getLogger(ObjectFactory.class);

    //TODO: could be generic to return the right interface
    public static IConfigurable create(Context a_context, IConfigurable a_parent, 
                                       String a_tag, String a_id, String a_defaultClass,
                                       boolean a_required) {

        ObjectTree tree = a_parent.getObjectTree();
        JsonNode root = tree.getRoot();
        JsonNode node = root.get(a_tag);
        //TODO: handle array with a_id
        if((null == node) || !node.isObject()) {
            ObjectFactory.logError(a_context, a_parent, a_tag, a_id, a_required);
            return null;
        }
        
        return ObjectFactory.create(a_context, a_parent, node, a_tag, a_id, a_defaultClass);        
    }
    
    public static String getString(Context a_context, IConfigurable a_parent,
                                   String a_tag, boolean a_required) {
        
        ObjectTree tree = a_parent.getObjectTree();
        JsonNode root = tree.getRoot();
        JsonNode node = root.get(a_tag);
        if(null == node) {
            ObjectFactory.logError(a_context, a_parent, a_tag, a_required);
            return null;
        }

        IString stringObject = null;
        if(node.isValueNode()) {
            stringObject = new StringDefault(new ObjectTree(node));
        } else if(node.isObject()) {
            stringObject = (IString)ObjectFactory.create(a_context, a_parent, node, a_tag, null, null);
            if(null == stringObject)
                return null;
        } else {
            ObjectFactory.logError(a_context, a_parent, a_tag, "Invalid type for a string");
            return null;
        }
        
        return stringObject.get(a_context);
    }
    
    private static IConfigurable create(Context a_context, IConfigurable a_parent,  
                                        JsonNode a_node, String a_tag, String a_id, String a_defaultClass) {

        JsonNode classAttr = a_node.get("@class");
        String className = a_defaultClass;
        //TODO: handle class name attribute
        if(null != classAttr) {
        }

        try {
            Class<?> _class = Class.forName(className);
            Constructor<?> tc = _class.getConstructor(ObjectTree.class);
            Object t = tc.newInstance(new ObjectTree(a_node));
            return (IConfigurable)t;
        } catch (Exception ex) {
            ObjectFactory.logError(a_context, a_parent, a_tag, a_id, "Failed to instantiate " + className);
            logger.error("", ex);
        }

        return null;
    }

    private static void logError(Context a_context, IConfigurable a_parent, 
                                 String a_tag, boolean a_required) {        
        if(a_required) {
            ObjectFactory.logError(a_context, a_parent, a_tag, null, "A required element is missing");
        }
    }

    private static void logError(Context a_context, IConfigurable a_parent, 
                                 String a_tag, String a_id, boolean a_required) {        
        if(a_required) {
            ObjectFactory.logError(a_context, a_parent, 
                    a_tag, a_id, "Could not find a required element");
        }
    }

    private static void logError(Context a_context, IConfigurable a_parent, 
                                 String a_tag, String a_error) {
        ObjectFactory.logError(a_context, a_parent, a_tag, a_error);
    }

    private static void logError(Context a_context, IConfigurable a_parent, 
                                 String a_tag, String a_id, String a_error) {
        if(null == a_id) {
            logger.error("{} - " + a_error + ", \"{}\" inside {}", 
                         new Object[]{a_context, a_tag, a_parent.getClass().getName()});
        } else {
            logger.error("{} - " + a_error + ", \"{}\" with id \"{}\" inside {}", 
                    new Object[]{a_context, a_tag, a_id, a_parent.getClass().getName()});            
        }
        
        if(a_context.isLogging()) logger.debug("{} - {} Object tree: {}", 
                                  new Object[] { a_context, a_parent.getClass().getName(), a_parent.getObjectTree().getRoot().toString()});
    }
}
