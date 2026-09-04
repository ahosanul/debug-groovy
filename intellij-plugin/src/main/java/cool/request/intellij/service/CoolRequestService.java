package cool.request.intellij.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import cool.request.intellij.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for communicating with the Grails application's Cool Request API
 */
public class CoolRequestService {
    
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String COOL_REQUEST_PATH = "/cool-request/api";
    
    private final Gson gson = new Gson();
    private String baseUrl = DEFAULT_BASE_URL;
    private boolean available = false;
    
    public static CoolRequestService getInstance() {
        return ApplicationManager.getApplication().getService(CoolRequestService.class);
    }
    
    /**
     * Check if the Cool Request API is available
     */
    public CompletableFuture<Boolean> checkAvailability(Project project) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String url = baseUrl + COOL_REQUEST_PATH + "/controllers";
                HttpGet request = new HttpGet(url);
                request.setHeader("Accept", "application/json");
                
                try (CloseableHttpResponse response = client.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    available = (status == 200);
                    
                    if (!available) {
                        Notifications.Bus.notify(
                            new Notification(
                                "Cool Request",
                                "Cool Request API Not Available",
                                "Could not connect to Cool Request API at " + url + 
                                ". Please ensure your Grails application is running.",
                                NotificationType.WARNING
                            ),
                            project
                        );
                    }
                    
                    return available;
                }
            } catch (IOException e) {
                available = false;
                Notifications.Bus.notify(
                    new Notification(
                        "Cool Request",
                        "Connection Error",
                        "Failed to connect to Cool Request API: " + e.getMessage(),
                        NotificationType.ERROR
                    ),
                    project
                );
                return false;
            }
        });
    }
    
    /**
     * Get all controllers from the Grails application
     */
    public CompletableFuture<List<ControllerMetadata>> getControllers(Project project) {
        return CompletableFuture.supplyAsync(() -> {
            List<ControllerMetadata> controllers = new ArrayList<>();
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String url = baseUrl + COOL_REQUEST_PATH + "/controllers";
                HttpGet request = new HttpGet(url);
                request.setHeader("Accept", "application/json");
                
                try (CloseableHttpResponse response = client.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String json = EntityUtils.toString(entity);
                            Type listType = new TypeToken<List<ControllerMetadata>>() {}.getType();
                            controllers = gson.fromJson(json, listType);
                        }
                    }
                }
            } catch (IOException e) {
                Notifications.Bus.notify(
                    new Notification(
                        "Cool Request",
                        "Error Loading Controllers",
                        "Failed to load controllers: " + e.getMessage(),
                        NotificationType.ERROR
                    ),
                    project
                );
            }
            
            return controllers;
        });
    }
    
    /**
     * Execute an HTTP request against the Grails application
     */
    public CompletableFuture<ExecutionResult> executeRequest(
            Project project,
            String method,
            String url,
            String body,
            List<NameValuePair> headers,
            List<NameValuePair> params) {
        
        return CompletableFuture.supplyAsync(() -> {
            ExecutionResult result = new ExecutionResult();
            long startTime = System.currentTimeMillis();
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                // Build URL with query parameters
                StringBuilder fullUrl = new StringBuilder(baseUrl);
                if (!url.startsWith("/")) {
                    fullUrl.append("/");
                }
                fullUrl.append(url);
                
                if (params != null && !params.isEmpty()) {
                    fullUrl.append("?");
                    for (int i = 0; i < params.size(); i++) {
                        if (i > 0) fullUrl.append("&");
                        NameValuePair param = params.get(i);
                        fullUrl.append(param.name).append("=").append(param.value);
                    }
                }
                
                HttpPost request = new HttpPost(fullUrl.toString());
                
                // Set headers
                if (headers != null) {
                    for (NameValuePair header : headers) {
                        request.setHeader(header.name, header.value);
                    }
                }
                
                // Set body if present
                if (body != null && !body.trim().isEmpty()) {
                    StringEntity entity = new StringEntity(body);
                    if (headers == null || headers.stream().noneMatch(h -> h.name.equals("Content-Type"))) {
                        entity.setContentType("application/json");
                    }
                    request.setEntity(entity);
                }
                
                // Override method if not POST
                if (!"POST".equalsIgnoreCase(method)) {
                    request.setHeader("X-HTTP-Method-Override", method);
                }
                
                try (CloseableHttpResponse response = client.execute(request)) {
                    result.setStatus(response.getStatusLine().getStatusCode());
                    result.setStatusText(response.getStatusLine().getReasonPhrase());
                    
                    // Get headers
                    List<NameValuePair> responseHeaders = new ArrayList<>();
                    for (org.apache.http.Header header : response.getAllHeaders()) {
                        responseHeaders.add(new NameValuePair(header.getName(), header.getValue()));
                    }
                    result.setHeaders(responseHeaders);
                    
                    // Get body
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        result.setBody(EntityUtils.toString(entity));
                        result.setContentType(entity.getContentType() != null ? 
                            entity.getContentType().getValue() : "text/plain");
                    }
                    
                    result.setTime(System.currentTimeMillis() - startTime);
                }
                
            } catch (IOException e) {
                result.setError(e.getMessage());
                result.setTime(System.currentTimeMillis() - startTime);
                
                Notifications.Bus.notify(
                    new Notification(
                        "Cool Request",
                        "Request Failed",
                        "Failed to execute request: " + e.getMessage(),
                        NotificationType.ERROR
                    ),
                    project
                );
            }
            
            return result;
        });
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Simple name-value pair class
     */
    public static class NameValuePair {
        public String name;
        public String value;
        
        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
    
    /**
     * Result of request execution
     */
    public static class ExecutionResult {
        private int status;
        private String statusText;
        private String body;
        private String contentType;
        private List<NameValuePair> headers;
        private long time;
        private String error;
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public String getStatusText() {
            return statusText;
        }
        
        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        public List<NameValuePair> getHeaders() {
            return headers;
        }
        
        public void setHeaders(List<NameValuePair> headers) {
            this.headers = headers;
        }
        
        public long getTime() {
            return time;
        }
        
        public void setTime(long time) {
            this.time = time;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
    }
}
