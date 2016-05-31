package fr.vuzi.http.error;

import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.response.IHttpResponse;

public interface IHttpErrorHandler {

    void handleError(HttpException e, IHttpRequest request, IHttpResponse response);

    void handleError(Exception e, IHttpRequest request, IHttpResponse response);
}
