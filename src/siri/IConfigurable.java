package siri;

public interface IConfigurable {

    String getTag();
    String getId();
    
    void setTag(String a_tagName);
    void setId(String a_id);
    
    String toDebugString();
}
