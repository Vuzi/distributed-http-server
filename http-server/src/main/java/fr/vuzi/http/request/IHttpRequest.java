package fr.vuzi.http.request;

import fr.vuzi.http.error.HttpException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for an HTTP request
 */
public interface IHttpRequest {

    /**
     * Initialize the response
     * @throws HttpException Thrown if the HTTP request is not valid
     * @throws IOException Thrown if an IO exception occurred on the socket
     */
    void read() throws HttpException, IOException;

    /**
     * Return the request method (i.e. GET or POST)
     * @return The request method
     */
    String getMethod();

    /**
     * Set the method used by the request (i.e. GET or POST)
     * @param method The request method
     */
    void setMethod(String method);

    /**
     * Return the URI requested
     * @return The requested URI location
     */
    String getLocation();

    /**
     * Set the requested URI
     * @param location The requested URI location
     */
    void setLocation(String location);

    /**
     * Return the protocol. Should always be HTTP/1.1
     * @return The protocol used
     */
    String getProtocol();

    /**
     * Set the protocol. Should always be HTTP/1.1
     * @param protocol The protocol used
     */
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

    /**
     * Set the request headers
     * @param headers The headers
     */
    void setHeaders(Map<String, String> headers);

    /**
     * Return the URI parameters
     * @return The URI parameters
     */
    Map<String, String> getParameters();

    /**
     * Return the parameter by its name, or null if not found
     * @param parameter The parameter name
     * @return The parameter by its name, or null if not found
     */
    String getParameter(String parameter);

    /**
     * Set the request parameters
     * @param parameters The parameters
     */
    void setParameters(Map<String, String> parameters);

    /**
     * Return the body of the request
     * @return The body of the request
     */
    byte[] getBody();

    /**
     * Set the body to the provided byte array
     * @param body The request body
     */
    void setBody(byte[] body);

    /**
     * Return the inet address of the client
     * @return The client address
     */
    InetAddress getClientAddress();

    /**
     * Set the request address
     * @param clientAddress The client address
     */
    void setClientAddress(InetAddress clientAddress);

    List<HttpCookie> getCookies();

    /**
     * Return a cookie by its name. Use null for cookie without a name
     * @param cookieName The cookie name
     * @return The cookie value, or null if not found
     */
    default HttpCookie getCookie(String cookieName)  {

        for(HttpCookie cookie : getCookies()) {
            if(cookieName == null) {
                if(cookie.getKey() == null)
                    return cookie;
            } else if (cookie.getKey().equals(cookieName))
                return cookie;
        }

        return null;
    }

    void setCookies(List<HttpCookie> cookies);

    /**
     * Return the hostname requested by the request
     * @return The request hostname
     */
    String getHostname();
}
