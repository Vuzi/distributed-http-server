package fr.vuzi.http.impl;

import fr.vuzi.http.route.IHttpHostRouter;
import fr.vuzi.http.route.IHttpRouter;

import java.util.HashMap;
import java.util.Map;

public class HttpHostRouter implements IHttpHostRouter {

    private Map<String, IHttpRouter> routes = new HashMap<>();

    @Override
    public void addRoute(String hostname, IHttpRouter router) {
        routes.put(hostname, router);
    }

    @Override
    public IHttpRouter resolve(String hostname) {
        IHttpRouter router = routes.get(hostname);

        if(router == null)
            router = routes.get("*"); // Try default fallback

        return router;
    }
}
