package siri;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskManager {
    
    static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    static long WAIT_QUANTUM = 100;

    IdentityHashMap<ITask, ITask> m_tasks = new IdentityHashMap<ITask, ITask>();
    short m_taskCount = 0;
    
    LinkedList<ITask> m_syncTasks = new LinkedList<ITask>();
    ConcurrentLinkedQueue<ITask> m_asyncTasks = new ConcurrentLinkedQueue<ITask>();
    
    public void notifyTask(ITask a_task) {
        if(logger.isTraceEnabled()) logger.trace("Task is ready: {}", a_task);

        m_asyncTasks.add(a_task);
        
        synchronized(m_asyncTasks) {
            m_asyncTasks.notify();
        }
    }

    public void addTask(ITask a_task) {
        if(m_tasks.containsKey(a_task)) {
            if(logger.isTraceEnabled()) logger.trace("Task already exists: {}", a_task);
            return;
        }        
        m_tasks.put(a_task,  a_task);        
        
        ITask.Type type = a_task.getType();
        if(type == ITask.Type.SYNC) {
            m_syncTasks.add(a_task);
        }
        // async tasks must tell us when they are ready to be run
        
        m_taskCount++;
        if(logger.isTraceEnabled()) logger.trace("Task added: {}", a_task);
    }

    public void run() {
        
        // - run a single sync task
        // - run all async tasks that are ready
        // - if nothing to run, wait
        while(true) {
            
            boolean yield = true;
            
            ITask syncTask = m_syncTasks.poll();
            if(null != syncTask) {
                if(logger.isTraceEnabled()) logger.trace("Running sync task: {}", syncTask);
                
                // needs more cycle, so lets push it back again
                if(ITask.Status.TASK_DONE != syncTask.run()) {
                    if(logger.isTraceEnabled()) logger.trace("Sync taskis NOT done: {}", syncTask);
                    m_syncTasks.add(syncTask);
                } else {
                    if(logger.isTraceEnabled()) logger.trace("Sync task is done: {}", syncTask);
                    m_tasks.remove(syncTask);
                }
                
                yield = false;
            }
            
            ITask asyncTask;
            while(null != (asyncTask = m_asyncTasks.poll())) {
                if(logger.isTraceEnabled()) logger.trace("Running async task: {}", asyncTask);
                
                // async tasks need to tell us, if they need more cycles
                if(ITask.Status.TASK_DONE == asyncTask.run()) {
                    if(logger.isTraceEnabled()) logger.trace("Async task is done: {}", asyncTask);
                    m_tasks.remove(asyncTask);
                }
                
                yield = false;
            }
            
            if(yield) {
                if(m_tasks.isEmpty()) {
                    break;
                }
                
                try {
                    if(logger.isTraceEnabled()) logger.trace("Waiting for async tasks to get ready.");
                    synchronized(m_asyncTasks) {
                        m_asyncTasks.wait(WAIT_QUANTUM);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            
            // abort all async tasks at some point
        }
        
        if(logger.isTraceEnabled()) logger.trace("TaskManager stat (tasks/left): {}/{}", m_taskCount, m_tasks.size());
    }
}
