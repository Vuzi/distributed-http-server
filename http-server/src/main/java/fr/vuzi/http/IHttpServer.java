package fr.vuzi.http;

import fr.vuzi.http.route.IHttpHostRouter;

import java.util.Properties;

public interface IHttpServer {

    /**
     * Configure the server using the provided properties file
     * @param properties The properties to use
     * @throws Exception If the configuration is invalid or impossible (missing classe, etc..)
     */
    void configure(Properties properties) throws Exception;

    /**
     * Set manually the host router for the server
     * @param hostRouter the new host router
     */
    void setHostRouter(IHttpHostRouter hostRouter);

    /**
     * Start the server on the specified port
     */
    void run(int port);

    /**
     * Stop the server
     */
    void stop();
}
