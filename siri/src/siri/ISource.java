package siri;

public interface ISource extends IConfigurable {

    Result get(Context a_context, IRequestCallback a_callback);
}
