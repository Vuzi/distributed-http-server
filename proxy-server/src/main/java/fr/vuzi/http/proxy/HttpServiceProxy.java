package fr.vuzi.http.proxy;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.HttpCookie;
import fr.vuzi.http.request.HttpUtils;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proxy implementation
 */
public class HttpServiceProxy implements IHttpService {

    private static Logger logger = Logger.getLogger(HttpServiceProxy.class.getCanonicalName());

    AtomicInteger index = new AtomicInteger(0);
    List<ProxyDestination> destinations = new ArrayList<>();

    public HttpServiceProxy(Map<String, String> parameters) throws Exception {
        String pool = parameters.get("pool");

        if(pool == null || pool.isEmpty())
            throw new Exception("No server pool specified");

        for(String server : pool.split(",")) {
            server = server.trim();
            int portIndex = server.indexOf(':');

            if(portIndex < 0) {
                destinations.add(new ProxyDestination(server, 80));
            } else {
                destinations.add(
                        new ProxyDestination(
                                server.substring(0, portIndex),
                                Integer.parseInt(server.substring(portIndex + 1))));
            }
        }
    }

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        int i = index.getAndIncrement();
        ProxyDestination destination = destinations.get(i % destinations.size());

        logger.info(String.format("Proxy request %d to %s %s -> %s",
                i, request.getHostname(), request.getLocation(), destination.host));

        //HttpCookie stikyCookie = request.getCookie("ID");

        try {
            Socket socket = new Socket(destination.host, destination.port);

            // Update request headers
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if(forwardedFor != null && !forwardedFor.isEmpty())
                request.getHeaders().put("X-Forwarded-For", forwardedFor + ", " + request.getClientAddress().toString());
            else
                request.getHeaders().put("X-Forwarded-For",  request.getClientAddress().toString());

            // Send to the server
            HttpUtils.RequestSender.send(request, socket.getOutputStream());

            // Read the response
            HttpUtils.ResponseParser.parse(response, socket.getInputStream());

            logger.info(String.format("Response for request %d from %s -> %d",
                    i, destination.host, response.getStatus()));

        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Error during proxy request %d processing", i), e);
            throw new HttpException(500, "Error during proxy request processing");
        }
    }

    private class ProxyDestination {
        String host;
        int port;

        public ProxyDestination(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
