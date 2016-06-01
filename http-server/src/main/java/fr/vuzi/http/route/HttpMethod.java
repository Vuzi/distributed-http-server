package fr.vuzi.http.route;

public enum HttpMethod {
    GET("GET"), POST("GET"), PUT("PUT"), DELETE("DELETE"), ALL("*");

    private String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
