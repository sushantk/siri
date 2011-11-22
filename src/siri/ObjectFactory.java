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
    
    static final Logger s_logger = LoggerFactory.getLogger(ObjectFactory.class);
    static JsonFactory s_jsonMapper = new MappingJsonFactory();
    static JsonNodeFactory s_jsonFactory = JsonNodeFactory.instance;

    public static ObjectTree tree(String a_tag) {
        return new ObjectTree(a_tag, s_jsonFactory.objectNode());
    }
    
    public static ObjectTree parse(Context a_context, String a_tag, String a_json) {
        JsonNode node = null;
        try {
            JsonParser parser = s_jsonMapper.createJsonParser(a_json);
            node = parser.readValueAsTree();
        } catch(Exception ex) {
            s_logger.error("", ex);
        }
        
        if((null == node) || !node.isObject()) {
            s_logger.error("{} - Invalid object tree for \"{}\": {}", 
                    new Object[] {a_context, a_tag, a_json});
            return null;
        }
        
        return new ObjectTree(a_tag, node);
    }

    public static ObjectTree find(Context a_context, ObjectTree a_parent, String a_tag, boolean a_required) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, null);
        if(null != node) {
            return new ObjectTree(a_tag, node);
        }

        if(a_required) {
            ObjectFactory.logError(a_context, a_parent, a_tag, Consts.error.missingElement);
        }
        return null;
    }

    public static ObjectTree find(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, a_id);
        if(null != node) {
            return new ObjectTree(a_tag, node);
        }
        
        ObjectFactory.logError(a_context, a_parent, a_tag, a_id, Consts.error.missingElement);
        return null;
    }
    
    public static ObjectTree fork(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode root = a_parent.getNode();
        JsonNode node = root.get(a_tag);
        ObjectNode forkedNode = s_jsonFactory.objectNode();
        if(null != node) {
            if((null == a_id) || a_id.isEmpty()) {
                if(!node.isObject()) return null;
                forkedNode.putAll((ObjectNode)node);
            } else if(node.isArray()) {
                // look for id
            }
            
            return new ObjectTree(a_tag, forkedNode);            
        }
        
        return null;
    }

    //TODO: could it be generic to return the right interface
    public static IConfigurable create(Context a_context, ObjectTree a_parent, String a_tag, String a_defaultClass, boolean a_required) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, null);
        if(null != node) {
            if(node.isObject()) {
                return ObjectFactory.create(a_context, (ObjectNode)node, a_tag, a_defaultClass);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, a_parent.getNode(), a_tag, "Expecting object node"));
            }
        }
        
        if(a_required) ObjectFactory.logError(a_context, a_parent, a_tag, Consts.error.missingElement);
        return null;
    }

    public static IConfigurable create(Context a_context, ObjectTree a_parent, String a_tag, String a_id, String a_defaultClass) {
        JsonNode node = ObjectFactory.findNode(a_context, a_parent, a_tag, a_id);
        if(null != node) {
            if(node.isObject()) {
                return ObjectFactory.create(a_context, (ObjectNode)node, a_tag, a_defaultClass);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, a_parent.getNode(), a_tag, "Expecting object node"));
            }
        }
        
        ObjectFactory.logError(a_context, a_parent, a_tag, a_id, Consts.error.missingElement);
        return null;
    }
    
    public static IConfigurable create(Context a_context, ObjectTree a_tree, String a_defaultClass) {
        JsonNode node = a_tree.getNode();
        if(null != node) {
            if(node.isObject()) {
                return ObjectFactory.create(a_context, (ObjectNode)node, a_tree.getTag(), a_defaultClass);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, a_tree.getNode(), "Expecting object node"));
            }
        }
        
        return null;
    }
    
    public static String getString(Context a_context, ObjectTree a_parent, String a_tag, boolean a_required) {        
        JsonNode root = a_parent.getNode();
        JsonNode node = root.get(a_tag);
        if(null != node) {
            if(node.isValueNode()) {
                return StringDefault.evaluate(a_context, node.asText());
            } else if(node.isObject()) {
                IString stringObject = (IString)ObjectFactory.create(a_context, (ObjectNode)node, a_tag, null);
                return stringObject.get(a_context);
            } else {
                throw new Error(ObjectFactory.fatalError(a_context, root, a_tag, "Invalid object tree for a string"));
            }
        }
        
        if(a_required) ObjectFactory.logError(a_context, a_parent, a_tag, Consts.error.missingElement);
        return null;
    }
    
    private static JsonNode findNode(Context a_context, ObjectTree a_parent, String a_tag, String a_id) {
        JsonNode root = a_parent.getNode();
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

    private static IConfigurable create(Context a_context, ObjectNode a_node, String a_tag, String a_defaultClass) {
        String className = a_defaultClass;
        JsonNode classAttr = a_node.get("@class");
        if((null != classAttr) && classAttr.isTextual()) {
            className = classAttr.asText();
        }
        if((null == className) || className.isEmpty()) {
            throw new Error(ObjectFactory.fatalError(a_context, a_node, Consts.error.missingClass));
        }

        try {
            Class<?> _class = Class.forName(className);
            Constructor<?> tc = _class.getConstructor(ObjectTree.class);
            Object t = tc.newInstance(new ObjectTree(a_tag, a_node));
            return (IConfigurable)t;
        } catch (Exception ex) {
            throw new Error(ObjectFactory.fatalError(a_context, a_node, ex.getMessage()));
        }
    }

    public static void logError(Context a_context, ObjectTree a_parent,
                                String a_tag, String a_error) {
        ObjectFactory.logError(a_context, a_parent, a_tag, null, a_error);
    }

    public static void logError(Context a_context, ObjectTree a_parent,
                                String a_tag, String a_id, String a_error) {
        if(null == a_id) {
            s_logger.error("{} - " + a_error + ", \"{}\" inside \"{}\"", 
                         new Object[]{a_context, a_tag, a_parent.getTag()});
        } else {
            s_logger.error("{} - " + a_error + ", \"{}\" with id \"{}\" inside \"{}\"", 
                    new Object[]{a_context, a_tag, a_id, a_parent.getTag()});            
        }
        
        if(a_context.isLogging()) s_logger.debug("{} - {} object tree: {}", 
                                  new Object[] {a_context, a_parent.getTag(), a_parent.getNode().toString()});
    }

    private static String fatalError(Context a_context, JsonNode a_node, String a_error) {
        return (a_context.toString() + " - " + a_error + ", object tree: " + a_node.toString());
    }

    private static String fatalError(Context a_context, JsonNode a_parent, String a_tag, String a_error) {
        return (a_context.toString() + " - " + a_error + ", \"" + a_tag + "\" inside: " + a_parent.toString());
    }
}
