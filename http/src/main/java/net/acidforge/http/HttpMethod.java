package net.acidforge.http;

/***
 * Represents a Http Method to be parsed by the http client pipeline.
 */
public enum HttpMethod {
    /***
     * Represents a DELETE http method
     */
    DELETE ("DELETE"),
    /***
     * Represents a GET http method
     */
    GET ("GET"),
    /***
     * Represents a PATCH http method
     */
    PATCH ("PATCH"),
    /***
     * Represents a POST http method
     */
    POST ("POST"),
    /***
     * Represents a PUT http method
     */
    PUT ("PUT");
    private String method;
    HttpMethod(String method) {
        this.method = method;
    }
    @Override
    public String toString(){
        return  method;
    }
}
