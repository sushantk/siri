package siri;

public interface IRequestCallback {
    void done(Context a_context, Data a_data);
    void failed(Context a_context, Data a_data, boolean a_timedout);
}
