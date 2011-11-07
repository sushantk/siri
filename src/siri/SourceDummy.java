package siri;

public class SourceDummy extends Configurable
                              implements ISource {

    public SourceDummy(ObjectTree a_tree) {
        super(a_tree);
    }

    @Override
    public Result get(Context a_context, IRequestCallback a_callback) {
        Data data = new Data();
        a_callback.done(a_context, data);
        return Result.SUCCESS;
    }

}
