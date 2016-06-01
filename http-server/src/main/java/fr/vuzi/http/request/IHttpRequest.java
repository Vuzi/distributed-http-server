package fr.vuzi.http.request;

import fr.vuzi.http.error.HttpException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

/**
 * Interface for an HTTP request
 */
public interface IHttpRequest {

    /**
     * Return the request method (i.e. GET or POST)
     * @return The request method
     */
    String getMethod();

    void setMethod(String method);

    /**
     * Return the URI requested
     * @return The requested URI location
     */
    String getLocation();

    void setLocation(String location);

    /**
     * Return the protocol. Should always be HTTP/1.1
     * @return The protocol used
     */
    String getProtocol();

    void setProtocol(String protocol);

    /**
     * Return the requested header, or null if not found
     * @param name The header name
     * @return The header value
     */
    String getHeader(String name);

    /**
     * Return the map of all the headers
     * @return All the headers
     */
    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);

    /**
     * Return all the headers names
     * @return The headers names
     */
    Set<String> getHeadersNames();

    Map<String, String> getParameters();

    Set<String> getParametersNames();

    String getParameter(String parameter);

    void setParameters(Map<String, String> parameters);

    /**
     * Return the body of the request
     * @return The body of the request
     */
    byte[] getBody();

    void setBody(byte[] body);

    String getHostname();

    void read() throws HttpException, IOException;

    InetAddress getClientAddress();

    void setClientAddress(InetAddress clientAddress);
}
