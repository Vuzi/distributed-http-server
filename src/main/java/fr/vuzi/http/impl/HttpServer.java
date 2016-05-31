package fr.vuzi.http.impl;

import fr.vuzi.http.IHttpServer;
import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.error.IHttpErrorHandler;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.response.IHttpResponse;
import fr.vuzi.http.route.HttpMethod;
import fr.vuzi.http.route.IHttpHostRouter;
import fr.vuzi.http.route.IHttpRouter;
import fr.vuzi.http.service.IHttpService;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HTTP server default implementation. Port not working
 */
public class HttpServer implements IHttpServer {

    private static Logger logger = Logger.getLogger(HttpServer.class.getCanonicalName());
    private IHttpErrorHandler defaultErrorHandler = new HttpErrorHandler();
    private IHttpHostRouter hostRouter;

    private int port = 8080;

    @Override
    public void configure(Properties properties) throws Exception {
        hostRouter = new HttpHostRouter();

        try {
            // Get all the virtual host names
            String[] vhostsNames = ((String) properties.getOrDefault("vhosts", "*")).split(",");

            // For each virtual host, get all its information
            for(String vhostName : vhostsNames) {
                IHttpRouter router = new HttpRouter();

                vhostName = vhostName.trim();

                // Host information
                String hostname = (String) properties.getOrDefault(vhostName + ".path", "*");
                int port = Integer.valueOf(((String) properties.getOrDefault(vhostName + ".port", "80")));

                // Services of the virtual host
                String[] servicesNames = ((String) properties.getOrDefault(vhostName + ".services", "*")).split(",");

                // For each service, load the provided class
                for(String serviceName : servicesNames) {
                    String servicePropertyKey = vhostName + ".services." + serviceName.trim();

                    // Get the hostname
                    String route = (String) properties.getOrDefault(servicePropertyKey + ".route", "");

                    String method = (String) properties.getOrDefault(servicePropertyKey + ".method", "");

                    // Get the service class name
                    String className = (String) properties.getOrDefault(servicePropertyKey + ".class", "");

                    if(!className.isEmpty()) {
                        Class<IHttpService> serviceClass = (Class<IHttpService>) Class.forName(className);
                        Constructor<?> constructor = serviceClass.getConstructor(Map.class);

                        Map<String, String> serviceParameters = new HashMap<>();
                        List<?> serviceParameterKeys = Collections.list(properties.propertyNames()).stream().
                                filter(key -> ((String) key).startsWith(servicePropertyKey)).collect(Collectors.toList());

                        // Get all the properties defined for the service, and give it to the service constructor
                        for(Object serviceParameterKey : serviceParameterKeys) {
                            String shortKey = ((String) serviceParameterKey).substring(servicePropertyKey.length() + 1);
                            serviceParameters.put(shortKey, (String) properties.getOrDefault(serviceParameterKey, ""));
                        }

                        router.addRoute(
                                HttpMethod.valueOf(method),  // Method
                                Pattern.compile(route),      // Regex path
                                new String[0],               // Capture groups
                                (IHttpService) constructor.newInstance(serviceParameters)); // Service
                    }
                }

                hostRouter.addRoute(hostname, router);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load configuration file", e);
            throw e;
        }
    }

    @Override
    public void setHostRouter(IHttpHostRouter hostRouter) {
        this.hostRouter = hostRouter;
    }

    @Override
    public void run() {
        try {
            // Open the socket
            ServerSocket serverSocket = new ServerSocket(port);

            // Thread pool
            ExecutorService pool = Executors.newCachedThreadPool();

            // Main loop
            while(true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.submit(() -> handleRequest(clientSocket));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Socket opening failed", e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unhandled error during setup", e);
            System.exit(1);
        }
    }

    /**
     * Handle internally the request, using the server's host router
     * @param clientSocket The client connection
     */
    private void handleRequest(Socket clientSocket) {
        // Timing
        long startTime = System.nanoTime();

        OutputStream outputStream;
        InputStream inputStream;

        try {
            outputStream = clientSocket.getOutputStream();
            inputStream = clientSocket.getInputStream();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Socket error", e);
            return;
        }

        // Response & request creation
        IHttpRequest request = new HttpRequest(inputStream);
        IHttpResponse response = new HttpResponse(request, outputStream);

        try {
            // Parse the request
            request.read();

            // Get host router for the hostname
            IHttpRouter router = hostRouter.resolve(request.getHostname());
            if(router == null) // If no route is found, 404
                throw new HttpException(404, "Host not found");

            // Route the request
            IHttpService service = router.resolve(request);
            if(service == null) // If no route is found, 404
                throw new HttpException(404, "Page not found");

            // Perform the operation
            service.serve(request, response);

            // Write the response
            response.write();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "HTTP error during processing", e);
            defaultErrorHandler.handleError(e, request, response);
        }

        // Performance loading
        long duration = (System.nanoTime() - startTime);
        logger.log(Level.INFO, String.format("Served in %f.4ms", (double)duration/1000000));
    }

    @Override
    public void stop() {
        // TODO
    }
}
