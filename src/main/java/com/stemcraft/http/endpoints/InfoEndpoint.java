package com.stemcraft.http.endpoints;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.stemcraft.http.HttpEndpoint;

public class InfoEndpoint extends HttpEndpoint {
    @Override
    public void doGET() {
        method = "set";
    }
}
