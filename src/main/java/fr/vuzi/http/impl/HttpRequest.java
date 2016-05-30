package fr.vuzi.http.impl;

import fr.vuzi.http.IHttpRequest;
import fr.vuzi.http.HttpException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IHttpRequest implementation
 */
public class HttpRequest implements IHttpRequest {
    private String method;
    private String location;
    private String protocol;

    private Map<String, String> headers;

    private byte[] body;

    /**
     * Default constructor where the provided stream is read and analysed to get all the request information
     * @param in The input stream
     * @throws HttpException
     * @throws IOException
     */
    public HttpRequest(InputStream in) throws HttpException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line = bufferedReader.readLine();

        // Request line
        String requestValues[] = line.split(" ");
        if(requestValues.length != 3)
            throw new HttpException(405, "Invalid HTTP request method");

        method = requestValues[0].trim();
        location = requestValues[1].trim();
        protocol = requestValues[2].trim();

        // headers
        headers = new HashMap<>();

        while(!(line = bufferedReader.readLine().trim()).isEmpty()) {
            int i = line.indexOf(':');

            if(i < 1)
                throw new HttpException(400, "Malformed header at line " + headers.size());

            headers.put(line.substring(0, i).trim().toLowerCase(),
                        line.length() > i ? line.substring(i + 1).trim() : "");
        }

        // Body
        String contentLength = headers.get("content-length");
        if(contentLength != null) {
            int size = Integer.valueOf(contentLength);
            if(size < 0)
                throw new HttpException(400, "Malformed content-length header:" + headers.get("content-length"));

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
    public byte[] getBody() {
        return body;
    }
}
