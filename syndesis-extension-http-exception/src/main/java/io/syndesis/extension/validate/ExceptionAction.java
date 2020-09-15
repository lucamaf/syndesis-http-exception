package io.syndesis.extension.validate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import io.syndesis.extension.api.Step;
import io.syndesis.extension.api.annotations.Action;
import io.syndesis.extension.api.annotations.ConfigurationProperty;
import io.syndesis.extension.api.annotations.ConfigurationProperties;
import io.syndesis.extension.api.annotations.ConfigurationProperty.PropertyEnum;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;

@Action(id = "Exception handling", name = "Exception", description = "Add a http exception catch step", tags = { "exception", "extension"})
@ConfigurationProperties({
    @ConfigurationProperty(
        name = "endpoint",
        displayName = "Valid HTTP endpoint",
        description = "The HTTP endpoint called",
        type = "String" ,
        required = true),
    @ConfigurationProperty(
        name = "method",
        displayName = "Valid HTTP method",
        description = "The HTTP method called",
        type = "String" ,
        required = true),
    @ConfigurationProperty(
        name = "body",
        displayName = "Body to be sent with the request",
        description = "JSON Body to be forwarded to the endpoint",
        type = "String" ,
        required = false)
})
public class ExceptionAction implements Step {

   
    private String endpoint;
    public String getEndpoint(){
        return endpoint;
    }
    public void setEndpoint(String endpoint){
        this.endpoint = endpoint;
    }

   
    private String method;
    public String getMethod(){
        return method;
    }
    public void setMethod(String method){
        this.method = method;
    }

    
    private String body;
    public String getBody(){
        return body;
    }
    public void setBody(String body){
        this.body = body;
    }

	@Override
    public Optional<ProcessorDefinition<?>> configure(CamelContext context, ProcessorDefinition<?> route, Map<String, Object> parameters) {
        ObjectHelper.notNull(route, "route");
        ObjectHelper.notNull(endpoint, "endpoint");
        ObjectHelper.notNull(method, "method");
        
        return Optional.of(route.process((Exchange exchange) -> {
            // my code
            URL url = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toUpperCase());
            
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            //ApiCallResult apiCallResult = callRestAPI(endpoint);

            if(method.toUpperCase().equals("POST")){
                con.setDoOutput(true);
                try(OutputStream os = con.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);			
                }
            }
            int statusCode = con.getResponseCode();
            //String payload = con.getResponseMessage();
            BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer payload = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                payload.append(inputLine);
            }
            in.close();

            exchange.getIn().setBody(payload);
            exchange.getIn().setHeader("API_CALL_STATUS_CODE", statusCode);
        }));
    }
}
