package fr.vuzi.http.impl;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.HttpUtils;
import fr.vuzi.http.request.IHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

/**
 * IHttpRequest implementation
 */
public class HttpRequest implements IHttpRequest {
    private InputStream inputStream;

    private String method;
    private String location;
    private String protocol;
    private String hostname;

    private Map<String, String> headers;

    private Map<String, String> parameters;

    private byte[] body;

    /**
     * Default constructor where the provided stream is read and analysed to get all the request information
     * @param in The input stream
     * @throws HttpException
     * @throws IOException
     */
    public HttpRequest(InputStream in) {
        this.inputStream = in;
    }

    @Override
    public void read() throws HttpException, IOException {
        HttpUtils.RequestParser.parse(this, inputStream);

        guessHostname();
    }

    private void guessHostname() {
        hostname = headers.get("host");
        if(hostname != null) {
            int portPos = hostname.indexOf(":");
            if(portPos > 0)
                hostname = hostname.substring(0, portPos);
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Set<String> getHeadersNames() {
        return headers.keySet();
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public Set<String> getParametersNames() {
        return parameters.keySet();
    }

    @Override
    public String getParameter(String parameter) {
        return parameters.get(parameter);
    }

    @Override
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

}
