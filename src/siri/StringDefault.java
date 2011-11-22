package siri;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringDefault extends Configurable
                           implements IString {
    
    static final Logger s_logger = LoggerFactory.getLogger(StringDefault.class);
    
    public StringDefault(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public String get(Context a_context) {
        JsonNode node = m_tree.getNode();
        if(node.isValueNode()) {
            return StringDefault.evaluate(a_context, node.asText());
        }

        return null;
    }
    
    public static String evaluate(Context a_context, String a_text) {
        String newText = a_text;
        if(a_context.isLogging()) s_logger.trace("{} - Evaluated {}=>{}", new Object[]{a_context, a_text, newText});
        return newText;
    }
}
