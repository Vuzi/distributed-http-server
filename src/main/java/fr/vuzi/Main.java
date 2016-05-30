package fr.vuzi;

import fr.vuzi.http.IHttpRequest;
import fr.vuzi.http.IHttpResponse;
import fr.vuzi.http.IHttpService;
import fr.vuzi.http.impl.HttpRequest;
import fr.vuzi.http.impl.HttpResponse;
import fr.vuzi.http.impl.HttpServiceStaticFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());

    public static void main(String[] args) {
        logger.info("Server launched");
        logger.info("Work directory -> " + System.getProperty("user.dir"));

        try {
            // Open the socket
            ServerSocket serverSocket = new ServerSocket(8080);

            // Create the service
            IHttpService service = new HttpServiceStaticFile("../public");

            while(true) {
                try {
                    handleRequest(serverSocket.accept(), service);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Socket opening failed", e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unhandled error during setup", e);
            System.exit(1);
        }
    }

    public static void handleRequest(Socket clientSocket, IHttpService service) {
        try {
            long startTime = System.nanoTime();
            OutputStream out = clientSocket.getOutputStream();
            InputStream in = clientSocket.getInputStream();

            IHttpRequest request = new HttpRequest(in);
            logger.log(Level.INFO, String.format("%s -> %s", request.getMethod(), request.getLocation()));

            IHttpResponse response = new HttpResponse(request, out);

            service.serve(request, response);
            long duration = (System.nanoTime() - startTime);

            logger.log(Level.INFO, String.format("Served in %f.4ms", (double)duration/1000000));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "HTTP request processing failed", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Socket closing failed", e);
            }
        }
    }

}
