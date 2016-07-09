package fr.vuzi.http.impl;

import fr.vuzi.http.request.HttpEncoding;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * IHttpResponse implementation
 */
public class HttpResponse implements IHttpResponse {

    private static Logger logger = Logger.getLogger(HttpResponse.class.getCanonicalName());
    private final Socket socket;

    private IHttpRequest request;
    private boolean headerSend = false;

    private OutputStream outputStream;
    private String protocol = "HTTP/1.1";
    private int status = 200;
    private String textStatus;
    private HttpEncoding encodingType = HttpEncoding.AUTO;

    private Map<String, String> headers = new HashMap<>();

    private byte[] body;
    private InputStream bodyInput;

    /**
     * Default constructor
     * @param request Request, used to automatically determine response elements
     * @param outputStream Thew output stream where to write
     */
    public HttpResponse(IHttpRequest request, OutputStream outputStream, Socket socket) {
        this.request = request;
        this.socket = socket;
        this.outputStream = outputStream;
    }

    /**
     * Empty constructor
     */
    public HttpResponse() {
        this.socket = null;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setTextStatus(String textStatus) {
        this.textStatus = textStatus;
    }

    @Override
    public String getTextStatus() {
        return textStatus;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeader(String key, String value) {
        headers.put(key.toLowerCase(), value);
    }

    @Override
    public void setBody(byte[] value) {
        this.body = value;
    }

    @Override
    public void setBody(String value) {
        this.body = value.getBytes();
    }


    @Override
    public void write() throws IOException {
        if(headerSend)
            throw new IOException("Header already sent");

        // Body compression
        if(encodingType == HttpEncoding.AUTO)
            encodingType = detectEncodingToUse();

        if(encodingType.headerName != null)
            setHeader("Content-Encoding", encodingType.headerName);

        // Body stream
        if(body != null) {
            bodyInput = new ByteArrayInputStream(body);
        }

        // Writer
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        // Response
        bufferedWriter.write( protocol + " " +
                              status   + " " +
                             (textStatus != null ? textStatus : getTextStatusFor(status)) + "\r\n");

        // Headers
        for(Map.Entry<String, String> header : headers.entrySet()) {
            bufferedWriter.write(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();

        headerSend = true;

        // Body
        outputStream = encodingType.encodeOutputStream(outputStream);

        if(bodyInput != null) {
            byte[] buffer = new byte[1024];
            int byteRead;

            while((byteRead = bodyInput.read(buffer)) > 0) {
                outputStream.write(buffer, 0, byteRead);
            }

            outputStream.flush();
            outputStream.close();

            bodyInput.close();
        }

        logger.log(Level.INFO, String.format("(%d) %s -> %s %s",
                getStatus(), request.getMethod(), request.getHostname(), request.getLocation()));
    }

    @Override
    public boolean headerSent() {
        return headerSend;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void setBody(InputStream inputStream) {
        this.bodyInput = inputStream;
    }

    public InputStream getBody() {
        return bodyInput;
    }

    private HttpEncoding detectEncodingToUse() {
        String acceptedEncoding = request.getHeader("Accept-Encoding");

        if(acceptedEncoding == null)
            return HttpEncoding.NONE;

        acceptedEncoding = acceptedEncoding.toLowerCase();

        for(HttpEncoding encodingType : HttpEncoding.values()) {
            if(encodingType.headerName != null && acceptedEncoding.contains(encodingType.headerName))
                return encodingType;
        }

        return HttpEncoding.NONE;
    }

    /**
     * Set the encoding type
     * @param encodingType The new encoding type
     */
    @Override
    public void setEncodingType(HttpEncoding encodingType) {
        this.encodingType = encodingType;
    }

    /**
     * Return the text status for the provided status code
     * @param status The status code
     * @return The text status
     */
    private String getTextStatusFor(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 304:
                return "Not Modified";
            case 404:
                return "Not Found";
            case 400:
                return "Bad Request";
            case 500:
                return "Server Error";
            default:
                return "Error Unknown";
        }
    }


}
