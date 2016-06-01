package fr.vuzi.http.impl;

import fr.vuzi.http.route.HttpMethod;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.route.IHttpRouter;
import fr.vuzi.http.service.IHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRouter implements IHttpRouter {

    private List<HttpRoute> routes = new ArrayList<>();

    @Override
    public void addRoute(HttpMethod method, Pattern pattern, IHttpService service) {
        routes.add(new HttpRoute(method, pattern, new String[0], service));
    }

    @Override
    public void addRoute(HttpMethod method, Pattern pattern, String[] values, IHttpService service) {
        routes.add(new HttpRoute(method, pattern, values, service));
    }

    @Override
    public IHttpService resolve(IHttpRequest request) {
        for(HttpRoute route : routes) {
            if(!(route.method == HttpMethod.ALL) && !request.getMethod().equals(route.method.getMethod()))
                continue;

            Matcher m = route.pattern.matcher(request.getLocation());

            if(m.find()) {
                int count = m.groupCount();

                for(int i = 1; i <= count && i <= route.values.length; i++) {
                    request.getParameters().put(route.values[i - 1], m.group(i));
                }

                return route.service;
            }
        }

        return null; // Not found
    }

    private class HttpRoute {
        public HttpMethod method;
        public Pattern pattern;
        public IHttpService service;
        public String[] values;

        public HttpRoute(HttpMethod method, Pattern pattern, String[] values, IHttpService service) {
            this.method = method;
            this.pattern = pattern;
            this.values = values;
            this.service = service;
        }
    }
}
