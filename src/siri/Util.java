package siri;

import java.util.List;

public class Util {

    public static String implode(List<String> a_list, String a_seperator) {
        String value = "";
        int length = a_list.size();
        for(int i = 0; i < length; i++) {
            if(i > 0) value += a_seperator;
            value += a_list.get(i);
        }
        
        return value;
    }

}
