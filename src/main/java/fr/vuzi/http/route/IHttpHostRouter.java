package fr.vuzi.http.route;


public interface IHttpHostRouter {

    public void addRoute(String hostname, IHttpRouter router);

    public IHttpRouter resolve(String hostname);

}
