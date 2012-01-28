package siri.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siri.Configurable;
import siri.IConfigurable;
import siri.IParams;
import siri.IString;
import siri.SiriParameter;
import siri.ObjectFactory;
import siri.Result;

public class TestObjectFactory {

    static final Logger s_logger = LoggerFactory.getLogger(TestObjectFactory.class);

    @Test
    public void testParse() {
        ObjectFactory factory = new ObjectFactory();
        assertNull(factory.parse(""));
        assertNull(factory.parse("foo"));
        assertNull(factory.parse("{-}"));
        assertNotNull(factory.parse("{}"));
        assertNotNull(factory.parse("{\"foo\" : \"bar\" }"));
        String tree = "{\"n1\":\"v1\", \"n2\":[\"x1\", {\"@id\":\"i2\", \"x2\":\"y2\"}, [\"x31\", \"x32\"]], \"n3\":{\"m1\":\"n1\"} }";
        assertNotNull(factory.parse(tree));
    }

    @Test
    public void testBuild() {
        ObjectFactory factory = new ObjectFactory();
        ObjectBuilder builder = new ObjectBuilder();
        
        assertTrue(Result.SUCCESS != factory.build(builder, factory.parse("{}")));
        assertTrue(Result.SUCCESS != factory.build(builder, factory.parse("{\"Unknown\" : {}}")));
        assertTrue(Result.SUCCESS != factory.build(builder, factory.parse("{\"Child\" : {}}")));
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Child\" : { \"Url\" : \"url\" }}")));
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Child\" : { \"@class\" : \"siri.test.TestObjectFactory$ChildObject2\" }}")));
    }
    
    @Test
    public void testBuildParams() {
        ObjectFactory factory = new ObjectFactory();
        ParamsBuilder builder = new ParamsBuilder();        
        
        String params = "\"string\"";
        assertTrue(Result.SUCCESS != factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        params = "[]";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        params = "[\"p1\", \"p2\"]";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        params = "{\"n1\" : \"v1\", \"n2\" : \"v2\"}";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        params = "{\"n1\" : \"v1\", \"n2\" : {\"x1\" : \"y1\"}}";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
        params = "{\"n1\" : \"v1\", \"n2\" : [\"x1\", \"x2\"] }";
        assertTrue(Result.SUCCESS == factory.build(builder, factory.parse("{\"Params\" : " + params + " }")));
    }

    public static class ObjectBuilder extends Configurable {
        @SiriParameter(required=true, defaultClass="siri.test.TestObjectFactory$ChildObject")
        public void setChild(IConfigurable a_child) {
        }
    }

    public static class ParamsBuilder extends Configurable {
        @SiriParameter(required=true)
        public void setParams(IParams a_params) {
            s_logger.debug("Params: {}", a_params.toDebugString());
        }
    }

    public static class ChildObject extends Configurable {
        @SiriParameter(required=true, defaultClass="siri.StringDefault")
        public void setUrl(IString a_string) {
        }
    }
    
    public static class ChildObject2 extends Configurable {
        public void setUrl(IString a_string) {
        }
    }
}
