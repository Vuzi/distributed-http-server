package fr.vuzi.http.request;

import fr.vuzi.http.error.HttpException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static final int MAX_LINE_LENGTH = 1048576; // 1Mo

    public static String readLine(InputStream inputStream) throws IOException, HttpException {
        byte[] buffer = new byte[1024];
        int i = 0;

        do {
            int b = inputStream.read();

            if(i >= buffer.length) {
                // Need to resize the buffer
                int size = (int)(buffer.length * 1.5);

                if(size >= MAX_LINE_LENGTH)
                    throw new HttpException(413, "Entity Too Large");

                byte[] newBuffer = new byte[size];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                buffer = newBuffer;
            }

            if(b < 0)
                break; // End of stream

            buffer[i] = (byte) b;
        } while(buffer[i++] != '\n');

        return new String(buffer, 0, i + 1, "UTF-8").trim();
    }

    public static class RequestSender {

        public static void send(IHttpRequest request, OutputStream outputStream) throws HttpException, IOException {
            outputStream.write(String.format("%s %s %s\r\n", request.getMethod(), request.getLocation(), request
                    .getProtocol()).getBytes());
            for(Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                outputStream.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()).getBytes());
            }
            outputStream.write("\r\n".getBytes());
            outputStream.write(request.getBody());
            outputStream.flush();
        }

    }

    public static class ResponseParser {

        public static void parse(IHttpResponse response, InputStream inputStream) throws HttpException, IOException {
            HttpUtils.ResponseParser.parseResponse(response, inputStream);
            HttpUtils.ResponseParser.parseHeaders(response, inputStream);

            response.setEncodingType(HttpEncoding.NONE);
            response.setBody(inputStream);
        }

        public static void parseResponse(IHttpResponse response, InputStream inputStream) throws HttpException, IOException {
            String line = HttpUtils.readLine(inputStream);

            int firstSpace = line.indexOf(" ");
            int secondSpace = line.indexOf(" ", firstSpace + 1);

            if (firstSpace < 0 || secondSpace < 0)
                throw new HttpException(502, "Bad response from server");

            response.setProtocol(line.substring(0, firstSpace));
            response.setStatus(Integer.parseInt(line.substring(firstSpace + 1, secondSpace)));
            response.setTextStatus(line.substring(secondSpace + 1));
        }

        public static void parseHeaders(IHttpResponse response, InputStream inputStream) throws HttpException, IOException {
            String line;

            while (!(line = HttpUtils.readLine(inputStream).trim()).isEmpty()) {
                int i = line.indexOf(':');

                if (i < 1)
                    throw new HttpException(400, "Malformed header at line " + response.getHeaders().size());

                response.getHeaders().put(line.substring(0, i).trim().toLowerCase(),
                        line.length() > i ? line.substring(i + 1).trim() : "");
            }
        }

    }

    public static class RequestParser {

        public static void parse(IHttpRequest request, InputStream inputStream) throws HttpException, IOException {
            HttpUtils.RequestParser.parseRequest(request, inputStream);
            HttpUtils.RequestParser.parseHeaders(request, inputStream);
            HttpUtils.RequestParser.parseBody(request, inputStream);

            HttpUtils.RequestParser.parseCookies(request);
        }

        private static void parseCookies(IHttpRequest request) {
            List<HttpCookie> cookies = new ArrayList<>();

            String rawCookiesHeader = request.getHeader("Cookie");

            if(rawCookiesHeader != null) {
                String[] rawCookies = rawCookiesHeader.split(";");

                for(String rawCookie : rawCookies) {
                    int i = rawCookie.indexOf('=');

                    if(i < 0)
                        cookies.add(new HttpCookie(null, rawCookie.trim()));
                    else
                        cookies.add(new HttpCookie(rawCookie.substring(0, i).trim(), rawCookie.substring(i + 1, 0).trim()));
                }
            }

            request.setCookies(cookies);
        }

        public static void parseRequest(IHttpRequest request, InputStream inputStream) throws HttpException, IOException {
            String requestValues[] = readLine(inputStream).split(" ");
            if(requestValues.length != 3)
                throw new HttpException(405, "Invalid HTTP request method");

            request.setMethod(requestValues[0].trim());
            request.setLocation(requestValues[1].trim());
            request.setProtocol(requestValues[2].trim());
        }

        public static void parseHeaders(IHttpRequest request, InputStream inputStream) throws HttpException, IOException {
            String line;
            HashMap<String, String> headers = new HashMap<>();

            while(!(line = HttpUtils.readLine(inputStream).trim()).isEmpty()) {
                int i = line.indexOf(':');

                if(i < 1)
                    throw new HttpException(400, "Malformed header at line " + headers.size());

                headers.put(line.substring(0, i).trim().toLowerCase(),
                        line.length() > i ? line.substring(i + 1).trim() : "");
            }

            request.setHeaders(headers);
        }
        private static final int MAX_BODY_SIZE = 10485760; // 10Mo

        public static void parseBody(IHttpRequest request, InputStream inputStream) throws HttpException, IOException {
            String contentLength = request.getHeader("content-length");
            boolean hasContentLength = false;
            int bodySize = 1024;

            if(contentLength != null) {
                try {
                    bodySize = Integer.valueOf(contentLength);
                    hasContentLength = true;
                } catch(NumberFormatException e) {
                    bodySize = -1;
                }

                if(bodySize < 0)
                    throw new HttpException(400, "Malformed content-length header: " + request.getHeader("content-length"));
            }

            // If the stream if empty, set an empty body
            if(inputStream.available() <= 0) {
                request.setBody(new byte[0]);
                return;
            }

            byte[] body = new byte[bodySize];
            int i = 0;

            do {
                int b = inputStream.read();

                if (b < 0)
                    break; // End of stream

                if (i >= body.length) {
                    // Need to resize the buffer
                    int size = (int) (body.length * 1.5);

                    if (size >= MAX_BODY_SIZE)
                        throw new HttpException(413, "Body too large");

                    byte[] newBuffer = new byte[size];
                    System.arraycopy(body, 0, newBuffer, 0, body.length);
                    body = newBuffer;
                }

                body[i++] = (byte) b;

                if(hasContentLength && i >= bodySize) {
                    break; // If content length is specified
                }

            } while (true);

            i--;

            if(i != (body.length - 1)) {
                byte[] resizeBody = new byte[i + 1];
                System.arraycopy(body, 0, resizeBody, 0, i + 1);
                body = resizeBody;
            }
            request.setBody(body);
        }

    }

}
