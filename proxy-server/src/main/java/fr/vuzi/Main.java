package fr.vuzi;

import fr.vuzi.http.IHttpServer;
import fr.vuzi.http.impl.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());

    public static void main(String[] args) {
        logger.info("Server launched");
        logger.info("Work directory -> " + System.getProperty("user.dir"));

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File("resources/main/conf.properties")));

            IHttpServer server = new HttpServer();
            server.configure(properties);
            server.run(8080);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load or use configuration file", e);
            System.exit(1);
        }
    }
}
