package fr.vuzi.http.request;

import fr.vuzi.http.error.HttpException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static class RequestSender {

        public static void send(IHttpRequest request, OutputStream outputStream) throws HttpException, IOException {
            outputStream.write(String.format("%s %s %s\r\n", request.getMethod(), request.getLocation(), request
                    .getProtocol()).getBytes());
            for(Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                outputStream.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()).getBytes());
            }
            outputStream.write("\r\n".getBytes());
            outputStream.write(request.getBody());
        }

    }

    public static class ResponseParser {

        public static void parse(IHttpResponse response, InputStream inputStream) throws HttpException, IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            HttpUtils.ResponseParser.parseResponse(response, reader);
            HttpUtils.ResponseParser.parseHeaders(response, reader);

            response.setEncodingType(HttpEncoding.NONE);
            response.setBody(inputStream);
        }

        public static void parseResponse(IHttpResponse response, BufferedReader reader) throws HttpException, IOException {
            String line = reader.readLine();

            int firstSpace = line.indexOf(" ");
            int secondSpace = line.indexOf(" ", firstSpace + 1);

            if (firstSpace < 0 || secondSpace < 0)
                throw new HttpException(502, "Bad response from server");

            response.setProtocol(line.substring(0, firstSpace));
            response.setStatus(Integer.parseInt(line.substring(firstSpace + 1, secondSpace)));
            response.setTextStatus(line.substring(secondSpace + 1));
        }

        public static void parseHeaders(IHttpResponse response, BufferedReader reader) throws HttpException, IOException {
            String line;

            while (!(line = reader.readLine().trim()).isEmpty()) {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            HttpUtils.RequestParser.parseRequest(request, reader);
            HttpUtils.RequestParser.parseHeaders(request, reader);
            HttpUtils.RequestParser.parseBody(request, reader);
        }

        public static void parseRequest(IHttpRequest request, BufferedReader reader) throws HttpException, IOException {
            String requestValues[] = reader.readLine().split(" ");
            if(requestValues.length != 3)
                throw new HttpException(405, "Invalid HTTP request method");

            request.setMethod(requestValues[0].trim());
            request.setLocation(requestValues[1].trim());
            request.setProtocol(requestValues[2].trim());
        }

        public static void parseHeaders(IHttpRequest request, BufferedReader reader) throws HttpException, IOException {
            String line;
            HashMap<String, String> headers = new HashMap<>();

            while(!(line = reader.readLine().trim()).isEmpty()) {
                int i = line.indexOf(':');

                if(i < 1)
                    throw new HttpException(400, "Malformed header at line " + headers.size());

                headers.put(line.substring(0, i).trim().toLowerCase(),
                        line.length() > i ? line.substring(i + 1).trim() : "");
            }

            request.setHeaders(headers);
        }

        public static void parseBody(IHttpRequest request, BufferedReader reader) throws HttpException, IOException {
            String contentLength = request.getHeader("content-length");
            byte[] body;

            if(contentLength != null) {
                int size = Integer.valueOf(contentLength);
                if(size < 0)
                    throw new HttpException(400, "Malformed content-length header: " + request.getHeader("content-length"));

                body = new byte[size];
                int i = 0;

                while(i < size) {
                    body[i++] = (byte)reader.read();
                }
            } else
                body = new byte[0];

            request.setBody(body);
        }

    }

}
