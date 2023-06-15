package com.stemcraft.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class SMApiResponse {
    private Integer status;
    private String data;

    public SMApiResponse() {}

    public SMApiResponse(Integer status, String data) {
        setStatus(status);
        setData(data);
    }

    public SMApiResponse(Integer status, Object data) {
        setStatus(status);
        setData(data);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setData(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.data = objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            this.data = "";
        }
    }

    public Integer getDataLength() {
        return this.data.length();
    }

    public void send(HttpExchange exchange) throws IOException {
        byte[] responseBytes = getData().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(getStatus(), responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }
}
