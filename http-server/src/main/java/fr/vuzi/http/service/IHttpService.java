package fr.vuzi.http.service;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;

/**
 * Interface for an HTTP service
 */
public interface IHttpService {

    /**
     * Service serving point
     * @param request The request to fulfill
     * @param response The response where to write
     */
    void serve(IHttpRequest request, IHttpResponse response) throws HttpException;
}