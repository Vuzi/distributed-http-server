package fr.vuzi.http.impl;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.error.IHttpErrorHandler;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpErrorHandler implements IHttpErrorHandler {

    private static Logger logger = Logger.getLogger(HttpErrorHandler.class.getCanonicalName());

    @Override
    public void handleError(HttpException error, IHttpRequest request, IHttpResponse response) {

        try {
            if(response.headerSent()) {
                // Headers are already sent, can't do anything beside logging the error
                logger.log(Level.WARNING, "Error after headers sent", error);
                response.getOutputStream().close();
                return;
            }

            response.setStatus(error.getErrorCode());
            response.setBody(
                    "<html>" +
                            "<head>" +
                            "<title>Error occurred</title>" +
                            "</head>" +
                            "<body>" +
                            "<h1>Error " + error.getErrorCode() + "</h1>" +
                            "<p>" +
                            (error.getCause() != null ? error.getCause().getMessage() : error.getMessage()) +
                            "</p>" +
                            "</body>" +
                            "</html>");
            response.write();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during the error handling", e);
        } finally {
            try {
                response.getOutputStream().close();
            } catch (IOException e) {}
        }
    }

    @Override
    public void handleError(Exception e, IHttpRequest request, IHttpResponse response) {
        handleError(
                e instanceof HttpException ? (HttpException)e : new HttpException(500, "Error : " + e
                        .getLocalizedMessage(), e),
                request,
                response);
    }
}
