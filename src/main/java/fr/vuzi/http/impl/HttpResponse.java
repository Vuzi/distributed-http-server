package fr.vuzi.http.impl;

import fr.vuzi.http.IHttpRequest;
import fr.vuzi.http.IHttpResponse;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * IHttpResponse implementation
 */
public class HttpResponse implements IHttpResponse {

    private IHttpRequest request;
    private boolean headerSend = false;

    private OutputStream outputStream;
    private String protocol = "HTTP/1.1";
    private int status = 200;
    private String textStatus;
    private EncodingType encodingType = EncodingType.AUTO;

    private Map<String, String> headers = new HashMap<>();

    private byte[] body;
    private InputStream bodyInput;

    /**
     * Default constructor
     * @param request Request, used to automatically determine response elements
     * @param outputStream Thew output stream where to write
     */
    public HttpResponse(IHttpRequest request, OutputStream outputStream) {
        this.request = request;
        this.outputStream = outputStream;
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
        if(encodingType == EncodingType.AUTO)
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

            while((byteRead = bodyInput.read(buffer)) == 1024)
                outputStream.write(buffer);
            outputStream.write(buffer, 0, byteRead);

            bodyInput.close();
            outputStream.flush();
            outputStream.close();
        }
    }

    @Override
    public void setBody(InputStream inputStream) {
        this.bodyInput = inputStream;
    }

    private EncodingType detectEncodingToUse() {
        String acceptedEncoding = request.getHeader("Accept-Encoding");

        if(acceptedEncoding == null)
            return EncodingType.NONE;

        acceptedEncoding = acceptedEncoding.toLowerCase();

        for(EncodingType encodingType : EncodingType.values()) {
            if(encodingType.headerName != null && acceptedEncoding.contains(encodingType.headerName))
                return encodingType;
        }

        return EncodingType.NONE;
    }

    /**
     * Set the encoding type
     * @param encodingType The new encoding type
     */
    public void setEncodingType(EncodingType encodingType) {
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
            case 404:
                return "Not Found";
            case 400:
                return "Bad Request";
            default:
                return "Error Unknown";
        }
    }

    /**
     * Encoding type enumeration
     */
    public enum EncodingType {
        AUTO(null), GZIP("gzip"), NONE(null);

        private String headerName;

        EncodingType(String headerName) {
            this.headerName = headerName;
        }

        /**
         * Wrap the provided stream in an encoded stream
         * @param outputStream The stream to be encoded
         * @return The encoding stream, or the provided stream is no encoding is defined
         * @throws IOException
         */
        public OutputStream encodeOutputStream(OutputStream outputStream) throws IOException {
            switch (this) {
                case GZIP:
                    return new GZIPOutputStream(outputStream);
                case AUTO:
                case NONE:
                    return outputStream;
            }

            return null;
        }
    }

}
