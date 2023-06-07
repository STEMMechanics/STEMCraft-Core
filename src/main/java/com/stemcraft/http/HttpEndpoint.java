package com.stemcraft.http;

public class HttpEndpoint {
    protected String method;

    public void doGET() {
        method = "get";
    }
}
