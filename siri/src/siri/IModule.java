package siri;

public interface IModule extends IConfigurable {

    public Result execute(Context a_context, IRequestCallback a_callback);
    public Data render(Context a_context, Data a_data);
}
