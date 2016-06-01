package fr.vuzi.http.request;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.util.Map;

/**
 * Interface for an HTTP response
 */
public interface IHttpResponse {

    /**
     * Set the protocol. Should be HTTP/1.1
     * @param protocol The protocol
     */
    void setProtocol(String protocol);

    /**
     * Get the protocol. Should be HTTP/1.1
     * @return The protocol
     */
    String getProtocol();

    /**
     * Set the request status code
     * @param status The request status code
     */
    void setStatus(int status);

    /**
     * Return the request status code
     * @return The request status code
     */
    int getStatus();

    /**
     * Set a text status. If null, a default text status will be used
     * @param textStatus The text status
     */
    void setTextStatus(String textStatus);

    /**
     * Return the actual text status
     * @return The text status
     */
    String getTextStatus();

    /**
     * Return the headers set for the response
     * @return The response headers
     */
    Map<String, String> getHeaders();

    /**
     * Set a header
     * @param key The header key
     * @param value The header value
     */
    void setHeader(String key, String value);

    /**
     * Set the body value
     * @param value The body value
     */
    void setBody(byte[] value);

    /**
     * Set the body value
     * @param value The body value
     */
    void setBody(String value);

    /**
     * Set the body stream
     * @param inputStream The new body stream
     */
    void setBody(InputStream inputStream);

    /**
     * Write the headers, the body and then close the connection
     * @throws IOException
     */
    void write() throws IOException;

    /**
     * Return true if the headers have already been sent
     * @return True if the headers have already been sent
     */
    boolean headerSent();

    OutputStream getOutputStream();

    void setEncodingType(HttpEncoding encodingType);
}
