package siri;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SiriObject(hasSetMethods=false)
public class ParamsDefault extends Configurable
                           implements IParams {
    
    static final Logger s_logger = LoggerFactory.getLogger(StringDefault.class);
    
    ArrayList<IConfigurable> m_params;

    public ParamsDefault() {
        m_params = new ArrayList<IConfigurable>();
    }

    public void addParameter(IConfigurable a_param) {
        m_params.add(a_param);
    }

    @Override
    public Map<String, Object> getMap(Context a_context) {
        
        TreeMap<String, Object> values = new TreeMap<String, Object>();
        
        Iterator<IConfigurable> iter = m_params.iterator();
        while(iter.hasNext()) {
            IConfigurable param = iter.next();
            if(param instanceof IString) {
                IString istring = (IString)param;
                String string = istring.get(a_context);
                if(null == string) {
                    s_logger.error("{} - Failed to evaluate parameter: {} inside {}", 
                            new Object[]{a_context, param, this});
                    return null;
                }
                values.put(param.getTag(), string);
            } else if(param instanceof IParams) {
                IParams iparams = (IParams)param;
                Map<String, Object> childValues = iparams.getMap(a_context);
                if(null == childValues) {
                    s_logger.error("{} - Failed to evaluate parameter: {} inside {}", 
                            new Object[]{a_context, param, this});
                    return null;
                }
                values.put(param.getTag(), childValues);                
            }
            else {
                s_logger.error("{} - Invalid parameter type, string or map expected: {} inside {}", 
                        new Object[]{a_context, param, this});
                return null;
            }
        }

        return values;
    }
    
    public String toDebugString() {
        return m_params.toString();
    }
}