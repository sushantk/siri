package siri;

public interface IRenderer extends IConfigurable {
    
    Data transform(Context a_context, Data a_data);
}
