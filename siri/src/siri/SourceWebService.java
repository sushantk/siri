package siri;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.client.HttpAsyncClient;
/*import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;*/
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;


public class SourceWebService implements ISource {

    HttpAsyncClient m_httpclient;
    
    Context m_context;
    IRequestCallback m_callback;
    ITask m_task;
    HttpUriRequest m_request;
    HttpResponse m_response;
    
    @Override
    public Result get(Context a_context, IRequestCallback a_callback) {
        
        m_context = a_context;
        m_callback = a_callback;
        
        final TaskManager taskManager = a_context.getRequestContext().getTaskManager();
        m_task = this.new WebServiceTask();
        taskManager.addTask(m_task);

        try {
            m_httpclient = new DefaultHttpAsyncClient();
            m_httpclient.start();
            
            m_request = new HttpGet("http://news.yahoo.com/rss/");
            m_httpclient.execute(m_request, new FutureCallback<HttpResponse>() {

                public void completed(final HttpResponse response) {
                    System.out.println(m_request.getRequestLine() + "->" + response.getStatusLine());
                    
                    m_response = response;
                    // tell task manager we are good to go
                    taskManager.notifyTask(m_task);
                }

                public void failed(final Exception ex) {
                    System.out.println(m_request.getRequestLine() + "->" + ex);
                    
                    taskManager.notifyTask(m_task);
                }

                public void cancelled() {
                    System.out.println(m_request.getRequestLine() + " cancelled");
                    
                    taskManager.notifyTask(m_task);
                }
            });

            System.out.println("Request initialized: " + m_request);
        } catch(Exception ex) {
            System.out.println("Exception");
        }        
        
        return Result.SUCCESS;
    }
    
    private class WebServiceTask implements ITask {
        
        public WebServiceTask() {
        }

        @Override
        public ITask.Status run() {
            InputStream stream = null;
            if(null != m_response) {
                try {
                    stream = m_response.getEntity().getContent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            Data data = new Data();
            data.setStream(stream);            
            if(null == stream) {
                m_callback.failed(m_context, data, false);
            } else {
                m_callback.done(m_context, data);
            }
            return ITask.Status.TASK_DONE;
        }

        @Override
        public Type getType() {
            return ITask.Type.ASYNC;
        }
    }

}
