package fr.vuzi.http.impl;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.HttpCookie;
import fr.vuzi.http.request.HttpUtils;
import fr.vuzi.http.request.IHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IHttpRequest implementation
 */
public class HttpRequest implements IHttpRequest {
    private InputStream inputStream;

    private String method;
    private String location;
    private String protocol = "HTTP/1.1";
    private String hostname;
    private InetAddress clientAddress;

    private Map<String, String> headers;
    private List<HttpCookie> cookies;

    private Map<String, String> parameters;

    private byte[] body;

    /**
     * Default constructor where the provided stream is read and analysed to get all the request information
     * @param in The input stream
     */
    public HttpRequest(InputStream in) {
        this.inputStream = in;
    }

    /**
     * Empty constructor
     */
    public HttpRequest() {
        headers = new HashMap<>();
        cookies = new ArrayList<>();
        parameters = new HashMap<>();
    }

    @Override
    public void read() throws HttpException, IOException {
        HttpUtils.RequestParser.parse(this, inputStream);

        parameters = new HashMap<>();

        guessHostname();
    }

    /**
     * Guess the hostname from the "host" header
     */
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
    public Map<String, String> getParameters() {
        return parameters;
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

    @Override
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    @Override
    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    @Override
    public List<HttpCookie> getCookies() {
        return cookies;
    }

    @Override
    public void setCookies(List<HttpCookie> cookies) {
        this.cookies = cookies;
    }
}
