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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SourceWebService extends Configurable
                              implements ISource {

    static final Logger s_logger = LoggerFactory.getLogger(SourceWebService.class);
    static protected HttpAsyncClient s_httpClient = null;

    protected IString m_iurl;
    protected String m_url;

    transient Context m_context;
    transient IRequestCallback m_callback;
    transient HttpUriRequest m_request;
    transient HttpResponse m_response;
    transient ITask m_task;
    
    public SourceWebService() {
    }
    
    @SiriParameter(required=true)
    public void setUrl(IString a_url) {
        m_iurl = a_url;
    }

    @Override
    public Result get(Context a_context, IRequestCallback a_callback) {
        
        m_context = a_context;
        m_callback = a_callback;
        
        m_url = m_iurl.get(m_context);
        if(null == m_url) return Result.FAILED;

        final TaskManager taskManager = a_context.getRequestContext().getTaskManager();
        m_task = this.new WebServiceTask();
        taskManager.addTask(m_task);

        try {
            if(null == s_httpClient) {
                s_httpClient = new DefaultHttpAsyncClient();
                s_httpClient.start();
            }                    
                    
            m_request = new HttpGet(m_url);
            s_httpClient.execute(m_request, new FutureCallback<HttpResponse>() {

                public void completed(final HttpResponse response) {
                    if(m_context.isLogging()) s_logger.info("{} - Request done: {}=>{}", 
                                              new Object[]{m_context, m_request.getRequestLine(), response.getStatusLine()});
                    m_response = response;
                    taskManager.notifyTask(m_task);
                }

                public void failed(final Exception ex) {
                    s_logger.error("{} - Request failed: {}. {}", 
                                 new Object[]{m_context, m_url, m_request.getRequestLine()});
                    if(m_context.isLogging()) s_logger.debug("", ex);
                    
                    taskManager.notifyTask(m_task);
                }

                public void cancelled() {
                    if(m_context.isLogging()) s_logger.info("{} - Request cancelled: {}. {}", 
                                              new Object[]{m_context, m_url, m_request.getRequestLine()});                    
                    taskManager.notifyTask(m_task);
                }
            });

            if(m_context.isLogging()) s_logger.info("{} - Request initialized: {}", m_context, m_url);
        } catch(Exception ex) {
            s_logger.error("{} - Request failed: {}. {}", 
                    new Object[]{m_context, m_url, m_request.getRequestLine()});
            if(m_context.isLogging()) s_logger.debug("", ex);

            taskManager.notifyTask(m_task);
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
