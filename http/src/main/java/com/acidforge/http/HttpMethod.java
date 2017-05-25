/*
 * Copyright (C) 2017 The Acidforge Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.acidforge.http;

/***
 * Represents a Http Method to be parsed by the http client pipeline.
 */
public enum HttpMethod {
    /***
     * Represents the CONNECT http method.
     * For use with a proxy that can dynamically switch to being a tunnel
     */
    CONNECT ("CONNECT"),
    /***
     * Represents the DELETE http method.
     * Sends a command to delete the resource identified by the URI
     */
    DELETE ("DELETE"),
    /***
     * Represents the GET http method.
     * Retrieve any information in form of an http entity.
     */
    GET ("GET"),
    /***
     * Represents the HEAD http method.
     * Although it works the same as GET method, it's intention is only to retrieve server
     * heads like in any ordinary request.
     */
    HEAD ("HEAD"),
    /***
     * Represents the OPTIONS http method
     * Represents a request for information about the communication options available on the request/response chain identified by the Request-URI
     */
    OPTIONS("OPTIONS"),
    /***
     * Represents the PATCH http method
     *
     */
    PATCH ("PATCH"),
    /***
     * Represents the POST http method
     * Is used to send an entity with an enclosing type defined by the request headers.
     */
    POST ("POST"),
    /***
     * Represents the PUT http method
     * It's almost the same as POST method, but semantically differs in the usage, for instance, it's used to place an entity
     * in the defined uri.
     */
    PUT ("PUT"),
    /***
     * Represents the TRACE http method
     * Is used mostly to invoke an ok response based on the request. It cannot have an enclosing entity.
     */
    TRACE ("TRACE");
    private String method;
    HttpMethod(String method) {
        this.method = method;
    }
    @Override
    public String toString(){
        return  method;
    }
}
