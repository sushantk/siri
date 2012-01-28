package siri;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFactory {
    
    static final Logger s_logger = LoggerFactory.getLogger(ObjectFactory.class);
    static JsonFactory s_jsonMapper = new MappingJsonFactory();
    static JsonNodeFactory s_jsonFactory = JsonNodeFactory.instance;
    
    int m_nextId = 0;
    
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
        
        this.normalize((ObjectNode)node);
        s_logger.debug("Normalized tree: ");
        s_logger.debug(node.toString());
        return new ObjectTree((ObjectNode)node);
    }
    
    public Result build(IConfigurable a_object, ObjectTree a_tree) {
        Stack<String> tagStack = new Stack<String>();
        tagStack.push(a_object.toString());
        
        try {
            this.build(a_object, a_tree.getNode(), tagStack);
            return Result.SUCCESS;
        } catch(Exception ex) {
            String context = Util.implode(tagStack, "=>");
            s_logger.error("Failed to build: {}", context);
            s_logger.error("Reason: ", ex);
        }
        
        return Result.INVALID_OBJECT_TREE;
    }
    
    private void normalize(ObjectNode a_node) {        
        Iterator<Map.Entry<String, JsonNode>> it = a_node.getFields();
        while(it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String tagName = entry.getKey();
            if(tagName.startsWith("@")) {
                continue; // attributes
            }
            
            JsonNode childNode = entry.getValue();
            if(childNode.isObject()) {
                this.normalize((ObjectNode)childNode);
            } else if(childNode.isArray()) {
                ObjectNode arrayNode = this.normalizeArray((ArrayNode)childNode);
                a_node.put(tagName, arrayNode);
            } else {
                ObjectNode stringNode = this.normalizeValue((ValueNode)childNode);
                a_node.put(tagName, stringNode);
            }
        }
    }
    
    private ObjectNode normalizeArray(ArrayNode a_node) {
        ObjectNode arrayNode = s_jsonFactory.objectNode();
        
        Iterator<JsonNode> arrayit = a_node.iterator();
        while(arrayit.hasNext()) {
            JsonNode childNode = arrayit.next();
            
            ObjectNode objChildNode = null;
            String id = null;
            if(childNode.isObject()) {
                objChildNode = (ObjectNode)childNode;
                this.normalize(objChildNode);
            } else if(childNode.isArray()) {
                objChildNode = this.normalizeArray((ArrayNode)childNode);                
            } else {
                objChildNode = this.normalizeValue((ValueNode)childNode);
            }
            
            id = this.getId(objChildNode);
            arrayNode.put(id, objChildNode);
        }

        return arrayNode;
    }

    private ObjectNode normalizeValue(ValueNode a_node) {
        ObjectNode stringNode = s_jsonFactory.objectNode();
        
        String textValue = a_node.asText();
        stringNode.put(Consts.Value, textValue);
        stringNode.put(Consts._class, Consts.defaultStringClass);
        
        return stringNode;
    }

    private void build(IConfigurable a_object, ObjectNode a_node, Stack<String> a_tagStack) throws Exception {

        Class<?> objClass = a_object.getClass();
        Method objectSetMethod = null;
        SiriObject siriObject = objClass.getAnnotation(SiriObject.class);
        if((null != siriObject) && !siriObject.hasSetMethods()) {
            objectSetMethod = this.findSetMethod(objClass, Consts.defaultAddMethod);
        }
        
        Set<String> setMethods = new TreeSet<String>();
        
        Iterator<Map.Entry<String, JsonNode>> it = a_node.getFields();
        while(it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String tagName = entry.getKey();
            if(tagName.startsWith("@")) continue; // attributes
            
            s_logger.debug("Building object: {}", tagName);
            JsonNode childNode = entry.getValue();

            boolean isList = false;
            String defaultClassName = Consts.defaultParamsClass;

            Method setMethod = objectSetMethod;
            if(null == setMethod) {
                String setMethodName = "set" + tagName;
                setMethod = this.findSetMethod(objClass, setMethodName);
                if(null == setMethod) {
                    // ignored to have tags that may be used by tools
                    s_logger.info("Ignored tag: {}", tagName);
                    continue;
                }
                
                SiriParameter parameter = setMethod.getAnnotation(SiriParameter.class);
                if(null != parameter) {
                    defaultClassName = parameter.defaultClass();
                    isList = parameter.list();
                }
                
                setMethods.add(setMethodName);
            }
            
            if(childNode.isValueNode()) {
                setMethod.invoke(a_object, childNode.asText());
            } else if(isList) {
                List<IConfigurable> childList = new ArrayList<IConfigurable>();
                
                Iterator<Map.Entry<String, JsonNode>> listit = childNode.getFields();
                while(listit.hasNext()) {
                    Entry<String, JsonNode> listEntry = listit.next();
                    String listEntryId = listEntry.getKey();
                    ObjectNode listChildNode = (ObjectNode)listEntry.getValue(); // its normalized
                    
                    IConfigurable listChildObject = this.build(tagName, listChildNode, defaultClassName, a_tagStack);
                    listChildObject.setId(listEntryId); // tag is the id in this case
                    childList.add(listChildObject);
                }

                setMethod.invoke(a_object, childList);
            } else {
                IConfigurable childObject = this.build(tagName, (ObjectNode)childNode, defaultClassName, a_tagStack);                                
                setMethod.invoke(a_object, childObject);
            }
        }
        
        List<String> missingParams = new Vector<String>();
        if(!ObjectFactory.verifyObject(a_object, setMethods, missingParams)) {
            String error = "Missing required parameters: " + Util.implode(missingParams, Consts.comma);
            throw new Exception(error);
        }
    }
    
    private IConfigurable build(String a_tagName, ObjectNode a_node, String a_defaultClassName, Stack<String> a_tagStack) throws Exception {
    
        IConfigurable childObject = ObjectFactory.createObject(a_node, a_defaultClassName);
        childObject.setTag(a_tagName);
        childObject.setId(this.getId(a_node));
        
        a_tagStack.push(childObject.toString());
        this.build(childObject, a_node, a_tagStack);
        a_tagStack.pop();
        
        return childObject;
    }

    private String getId(ObjectNode a_node) {
        String id = "";
        JsonNode idNode = a_node.get(Consts.id);
        if(null != idNode) {
            id = idNode.asText();
        }
        
        if(id.isEmpty()) {
            id = String.format("_auto_%d", m_nextId++);
            a_node.put(Consts.id, id);
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
