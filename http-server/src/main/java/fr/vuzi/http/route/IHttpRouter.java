package fr.vuzi.http.route;

import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.service.IHttpService;

import java.util.regex.Pattern;

public interface IHttpRouter {

    void addRoute(HttpMethod method, Pattern pattern, IHttpService service);

    void addRoute(HttpMethod method, Pattern pattern, String[] values, IHttpService service);

    IHttpService resolve(IHttpRequest request);
}
