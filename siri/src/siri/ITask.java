package siri;

public interface ITask {
    
    static public enum Status {
        TASK_DONE,
        TASK_NOT_DONE
    }
    
    static public enum Type {
        ASYNC,
        SYNC
    }

    Type getType();
    Status run();
}
