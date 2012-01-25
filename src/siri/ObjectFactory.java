package siri;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

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
    
    public ObjectTree parse(String a_json) {
        JsonNode node = null;
        try {
            JsonParser parser = s_jsonMapper.createJsonParser(a_json);
            node = parser.readValueAsTree();
        } catch(Exception ex) {
            s_logger.error("", ex);
        }
        
        if((null == node) || !node.isObject()) {
            s_logger.error("Invalid object tree: {}", a_json);
            return null;
        }
        
        return new ObjectTree(node);
    }
    
    public Result build(IConfigurable a_object, ObjectTree a_tree) {
        Stack<String> tagStack = new Stack<String>();
        tagStack.push(a_object.getTag() + "<" + a_object.getId() + ">");
        
        try {
            this.build(a_object, a_tree.getNode(), tagStack);
            return Result.SUCCESS;
        } catch(Exception ex) {
            String context = Util.implode(tagStack, ":");
            s_logger.error("Failed to build: {}", context);
            s_logger.error("Reason: ", ex);
        }
        
        return Result.INVALID_OBJECT_TREE;
    }

    private void build(IConfigurable a_object, JsonNode a_node, Stack<String> a_tagStack) throws Exception {

        Class<?> objClass = a_object.getClass();
        Set<String> setMethods = new TreeSet<String>();
        
        Iterator<Map.Entry<String, JsonNode>> it = a_node.getFields();
        while(it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String tagName = entry.getKey();
            if(tagName.startsWith("@")) {
                // attributes
                continue;
            }
            
            JsonNode childNode = entry.getValue();
            
            s_logger.debug("Building object: {}", tagName);
            
            String setMethodName = "set" + tagName;
            Method setMethod = this.findSetMethod(objClass, setMethodName);
            if(null == setMethod) {
                // ignored to have tags that may be used by tools
                s_logger.info("Ignored tag: {}", tagName);
                continue;
            }
            setMethods.add(setMethodName);
            
            String defaultClassName = Consts.defaultClass; 
            SiriParameter parameter = setMethod.getAnnotation(SiriParameter.class);
            if(null != parameter) {
                defaultClassName = parameter.defaultClass();
            }

            if(childNode.isArray()) {
                Map<String, IConfigurable> childMap = new TreeMap<String, IConfigurable>();
                Iterator<JsonNode> arrayit = childNode.iterator();
                while(arrayit.hasNext()) {
                    JsonNode arrayNode = arrayit.next();
                    IConfigurable arrayChildObject = this.build(tagName, arrayNode, defaultClassName, a_tagStack);
                    childMap.put(arrayChildObject.getId(), arrayChildObject);
                }
                setMethod.invoke(a_object, childMap);
            } else {
                IConfigurable childObject = this.build(tagName, childNode, defaultClassName, a_tagStack);                                
                setMethod.invoke(a_object, childObject);
            }
        }
        
        List<String> missingParams = new Vector<String>();
        if(!ObjectFactory.verifyObject(a_object, setMethods, missingParams)) {
            String error = "Missing required parameters: " + Util.implode(missingParams, Consts.comma);
            throw new Exception(error);
        }
    }
    
    private IConfigurable build(String a_tagName, JsonNode a_node, String a_defaultClassName, Stack<String> a_tagStack) throws Exception {
        String id = ObjectFactory.getId(a_node);
        a_tagStack.push(a_tagName + "<" + id + ">");
    
        IConfigurable childObject = ObjectFactory.createObject(a_node, a_defaultClassName);
        childObject.setTag(a_tagName);
        childObject.setId(id);
        
        if(a_node.isObject()) {
            this.build(childObject, a_node, a_tagStack);
        } else {
            Method valueMethod = childObject.getClass().getMethod("setValue", String.class);
            valueMethod.invoke(childObject, a_node.asText());
        }
        a_tagStack.pop();
        
        return childObject;
    }
    
    private static String getId(JsonNode a_node) {
        String id = "";
        JsonNode idNode = a_node.get(Consts.id);
        if(null != idNode) {
            id = idNode.asText();
        }
        
        if(id.isEmpty()) {
            // id.format();
        }
        
        return id;
    }

    private static IConfigurable createObject(JsonNode a_node, String a_defaultClassName) throws Exception {
        String className = a_defaultClassName;
        
        IConfigurable childObject = null;
        if(a_node.isObject()) {
            JsonNode classNode = a_node.get(Consts._class);
            if(null != classNode) {
                String nodeClassName = classNode.asText();
                if((null != nodeClassName) && !nodeClassName.isEmpty()) {
                    className = nodeClassName;
                }
            }

            childObject = ObjectFactory.createObject(className);
        } else {
            childObject = ObjectFactory.createObject(className);
        }
        
        return childObject;
    }

    private Method findSetMethod(Class<?> a_class, String a_methodName) {
        Method[] methods = a_class.getMethods();
        for(Method method:methods)
        {
            String methodName = method.getName();
            if(methodName.equals(a_methodName)) {
                return method;
            }
        }
        
        return null;
    }
        
    private static boolean verifyObject(IConfigurable a_object, Set<String> a_setMethods, List<String> a_missingParams) {
        boolean valid = true;
        Class<?> objClass = a_object.getClass();
        Method[] methods = objClass.getMethods();
        for(Method method:methods)
        {
            SiriParameter parameter = method.getAnnotation(SiriParameter.class);
            if(null != parameter) {
                String methodName = method.getName();
                boolean required = parameter.required();
                if(required && !a_setMethods.contains(methodName)) {
                    valid = false;
                    a_missingParams.add(methodName.substring(3));
                }
            }            
        }
        
        return valid;
    }

    private static IConfigurable createObject(String a_className) throws Exception {
        Class<?> _class = Class.forName(a_className);
        Constructor<?> tc = _class.getConstructor();
        Object t = tc.newInstance();
        return (IConfigurable)t;
    }
}
