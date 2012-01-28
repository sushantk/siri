package siri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringDefault extends Configurable
                           implements IString {
    
    static final Logger s_logger = LoggerFactory.getLogger(StringDefault.class);
    
    private String m_value;

    public StringDefault() {
    }

    @SiriParameter(required=true)
    public void setValue(String a_value) {
        m_value = a_value;
        //TODO: check if this needs evaluation or is a literal
    }

    @Override
    public String get(Context a_context) {
        return StringDefault.evaluate(a_context, m_value);
    }
    
    public static String evaluate(Context a_context, String a_text) {
        String newText = a_text;
        if(a_context.isLogging()) s_logger.trace("{} - Evaluated {}=>{}", new Object[]{a_context, a_text, newText});
        return newText;
    }
}
