package fr.vuzi.http;

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

    /**
     * Return the URI requested
     * @return The requested URI location
     */
    String getLocation();

    /**
     * Return the protocol. Should always be HTTP/1.1
     * @return The protocol used
     */
    String getProtocol();

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

    /**
     * Return all the headers names
     * @return The headers names
     */
    Set<String> getHeadersNames();

    /**
     * Return the body of the request
     * @return The body of the request
     */
    byte[] getBody();
}
