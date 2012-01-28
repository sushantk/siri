package siri;

import java.util.Map;

public interface IParams extends IConfigurable {

    Map<String, Object> getMap(Context a_context);
}
