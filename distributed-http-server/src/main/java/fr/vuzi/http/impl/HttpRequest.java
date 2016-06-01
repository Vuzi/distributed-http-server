package fr.vuzi.http.impl;

import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.error.HttpException;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IHttpRequest implementation
 */
public class HttpRequest implements IHttpRequest {
    private BufferedReader bufferedReader;
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
        this.bufferedReader = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void read() throws HttpException, IOException {
        readRequest();
        readHeaders();
        readParameters();
        readHostname();
        readBody();
    }

    private void readParameters() throws UnsupportedEncodingException {
        parameters = new HashMap<>();
        int paramStart = location.indexOf('?');
        if(paramStart > 0 && paramStart < location.length()) {

            String[] keyValues = URLDecoder.decode(location.substring(paramStart + 1), "UTF-8").split("&");

            for(String keyValue : keyValues) {
                String[] keyAndValue = keyValue.split("=");
                if(keyAndValue.length == 2)
                    parameters.put(keyAndValue[0], keyAndValue[1]);
            }
        }
    }

    private void readRequest() throws HttpException, IOException {
        String requestValues[] = bufferedReader.readLine().split(" ");
        if(requestValues.length != 3)
            throw new HttpException(405, "Invalid HTTP request method");

        method = requestValues[0].trim();
        location = requestValues[1].trim();
        protocol = requestValues[2].trim();
    }

    private void readHeaders() throws HttpException, IOException {
        String line;
        headers = new HashMap<>();

        while(!(line = bufferedReader.readLine().trim()).isEmpty()) {
            int i = line.indexOf(':');

            if(i < 1)
                throw new HttpException(400, "Malformed header at line " + headers.size());

            headers.put(line.substring(0, i).trim().toLowerCase(),
                    line.length() > i ? line.substring(i + 1).trim() : "");
        }
    }

    private void readHostname() {
        hostname = headers.get("host");
        if(hostname != null) {
            int portPos = hostname.indexOf(":");
            if(portPos > 0)
                hostname = hostname.substring(0, portPos);
        }
    }

    private void readBody() throws HttpException, IOException {
        String contentLength = headers.get("content-length");
        if(contentLength != null) {
            int size = Integer.valueOf(contentLength);
            if(size < 0)
                throw new HttpException(400, "Malformed content-length header: " + headers.get("content-length"));

            body = new byte[size];
            int i = 0;

            while(i < size) {
                body[i++] = (byte)bufferedReader.read();
            }
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getProtocol() {
        return protocol;
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
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
