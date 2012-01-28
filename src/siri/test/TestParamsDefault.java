package siri.test;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siri.Configurable;
import siri.Context;
import siri.IParams;
import siri.ObjectFactory;
import siri.RequestContext;
import siri.Result;
import siri.SiriParameter;

public class TestParamsDefault {

    static final Logger s_logger = LoggerFactory.getLogger(TestParamsDefault.class);

    @Test
    public void testParse() {
        ObjectFactory factory = new ObjectFactory();
        ParamsBuilder builder = new ParamsBuilder();        
        Context context = new Context(new RequestContext(), "", ""); 
        
        Map<String, Object> map;
        String params = "[]";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        map = builder.getParamsMap(context);
        assertTrue("Empty params", map.isEmpty());
        params = "[\"p1\", \"p2\"]";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        map = builder.getParamsMap(context);
        assertTrue("Array params", (2 == map.size()));
        params = "{\"n1\" : \"v1\", \"n2\" : {\"x1\" : \"y1\"}}";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        map = builder.getParamsMap(context);
        assertTrue("Map params", (2 == map.size()));
        Map<String, Object> childMap = (Map<String, Object>)map.get("n2");
        assertTrue("Child map params", (1 == childMap.size()));
        assertTrue("Child map params", (childMap.get("x1").toString().equals("y1")));
        params = "{\"n1\" : \"v1\", \"n2\" : [\"x1\", \"x2\"] }";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        map = builder.getParamsMap(context);
        assertTrue("Map params", (2 == map.size()));
        childMap = (Map<String, Object>)map.get("n2");
        assertTrue("Child map params", (2 == childMap.size()));
    }
    
    public static class ParamsBuilder extends Configurable {
        
        IParams m_params;
        
        @SiriParameter(required=true)
        public void setParams(IParams a_params) {
            m_params = a_params;
        }
        
        public Map<String, Object> getParamsMap(Context a_context) {
            Map<String, Object> map = m_params.getMap(a_context);
            if(null != map) {
                s_logger.debug("Map: {}", map);
            }
            return map;
        }
    }
}
