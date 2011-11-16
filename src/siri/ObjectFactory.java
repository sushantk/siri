package siri;

import java.lang.reflect.Constructor;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFactory {
    
    static final Logger logger = LoggerFactory.getLogger(ObjectFactory.class);
    static JsonFactory s_jsonMapper = new MappingJsonFactory();
    static JsonNodeFactory s_jsonFactory = JsonNodeFactory.instance;

    public static ObjectTree tree() {
        return new ObjectTree(s_jsonFactory.objectNode());
    }
    
    public static ObjectTree parse(Context a_context, String a_json) {
        JsonNode node = null;
        try {
            JsonParser parser = s_jsonMapper.createJsonParser(a_json);
            node = parser.readValueAsTree();
        } catch(Exception ex) {
            logger.error("", ex);
        }
        
        if((null == node) || !node.isObject()) {
            logger.error("{} - Invalid object tree: {}", a_context, a_json);
            return null;
        }
        
        return new ObjectTree((ObjectNode)node);
    }
    
    public static ObjectTree find(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, a_id);
        if(null != node) {
            return new ObjectTree(node);
        }
        
        return null;
    }
    
    public static ObjectTree fork(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode root = a_parent.getRoot();
        JsonNode node = root.get(a_tag);
        ObjectNode forkedNode = s_jsonFactory.objectNode();
        if(null != node) {
            if((null == a_id) || a_id.isEmpty()) {
                if(!node.isObject()) return null;
                forkedNode.putAll((ObjectNode)node);
            } else if(node.isArray()) {
                // look for id
            }
            
            return new ObjectTree(forkedNode);            
        }
        
        return null;
    }

    //TODO: could it be generic to return the right interface
    public static IConfigurable create(Context a_context, ObjectTree a_parent, String a_tag, String a_id, String a_defaultClass) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, a_id);
        if(null != node) {
            if(node.isObject()) {
                ObjectFactory.create(a_context, (ObjectNode)node, a_defaultClass);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, a_parent.getRoot(), a_tag, "Expecting object"));
            }
        }
        
        return null;
    }
    
    public static IConfigurable create(Context a_context, ObjectTree a_tree, String a_defaultClass) {
        JsonNode node = a_tree.getRoot();
        if(null != node) {
            if(node.isObject()) {
                return ObjectFactory.create(a_context, (ObjectNode)node, a_defaultClass);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, a_tree.getRoot(), "Expecting object"));
            }
        }
        
        return null;
    }
    
    public static String getString(Context a_context, ObjectTree a_parent, String a_tag) {        
        JsonNode root = a_parent.getRoot();
        JsonNode node = root.get(a_tag);
        if(null == node) return null;

        IString stringObject = null;
        if(node.isValueNode()) {
            stringObject = new StringDefault(new ObjectTree(node));
        } else if(node.isObject()) {
            stringObject = (IString)ObjectFactory.create(a_context, (ObjectNode)node, null);
        } else {
            throw new Error(ObjectFactory.fatalError(a_context, root, a_tag, "Invalid object tree for a string"));
        }
        
        return stringObject.get(a_context);
    }
    
    private static JsonNode findNode(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode root = a_parent.getRoot();
        JsonNode node = root.get(a_tag);
        if(null != node) {
            if((null == a_id) || a_id.isEmpty()) {
                return node;
            }
            
            // look for id
            if(node.isArray()) {
            }
        }
        
        return null;
    }

    private static IConfigurable create(Context a_context, ObjectNode a_node, String a_defaultClass) {
        String className = a_defaultClass;
        JsonNode classAttr = a_node.get("@class");
        if((null != classAttr) && classAttr.isTextual()) {
            className = classAttr.asText();
        }

        try {
            Class<?> _class = Class.forName(className);
            Constructor<?> tc = _class.getConstructor(ObjectTree.class);
            Object t = tc.newInstance(new ObjectTree(a_node));
            return (IConfigurable)t;
        } catch (Exception ex) {
            throw new Error(ObjectFactory.fatalError(a_context, a_node, ex.getMessage()));
        }
    }

    public static void logError(Context a_context, String a_parentTag, ObjectTree a_parent,
                                String a_tag, String a_error) {
        ObjectFactory.logError(a_context, a_parentTag, a_parent, a_tag, null, a_error);
    }

    public static void logError(Context a_context, String a_parentTag, ObjectTree a_parent,
                                String a_tag, String a_id, String a_error) {
        if(null == a_id) {
            logger.error("{} - " + a_error + ", \"{}\" inside \"{}\"", 
                         new Object[]{a_context, a_tag, a_parentTag});
        } else {
            logger.error("{} - " + a_error + ", \"{}\" with id \"{}\" inside \"{}\"", 
                    new Object[]{a_context, a_tag, a_id, a_parentTag});            
        }
        
        if(a_context.isLogging()) logger.debug("{} - {} object tree: {}", 
                                  new Object[] { a_context, a_parentTag, a_parent.getRoot().toString()});
    }

    private static String fatalError(Context a_context, JsonNode a_node, String a_error) {
        return (a_context.toString() + " - " + a_error + ", object tree: " + a_node.toString());
    }

    private static String fatalError(Context a_context, JsonNode a_parent, String a_tag, String a_error) {
        return (a_context.toString() + " - " + a_error + ", \"" + a_tag + "\" inside: " + a_parent.toString());
    }
}
