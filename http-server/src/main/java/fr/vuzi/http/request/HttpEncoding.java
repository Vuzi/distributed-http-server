package fr.vuzi.http.request;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Encoding type enumeration
 */
public enum HttpEncoding {
    AUTO(null), GZIP("gzip"), NONE(null);

    public final String headerName;

    HttpEncoding(String headerName) {
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

