package com.example.pixelgame.lwjgl3;

import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ServerAddressTest {
    @Test void acceptsLocalAndHttpsTunnelAddresses() {
        assertEquals("http://127.0.0.1:8080",ServerAddress.normalize(" http://127.0.0.1:8080/ "));
        assertEquals("https://test.trycloudflare.com",ServerAddress.normalize("HTTPS://test.trycloudflare.com///"));
        assertEquals("http://[::1]:8080",ServerAddress.normalize("http://[::1]:8080"));
    }
    @Test void rejectsAddressesWithCredentialsPathsOrUnsupportedSchemes() {
        for(String input:new String[]{"","localhost:8080","file:///C:/test","https://user:secret@test.example","https://test.example/api/health","https://test.example?token=test","https://test.example#fragment","http://localhost:0","http://localhost:65536"})
            assertThrows(IllegalArgumentException.class,()->ServerAddress.normalize(input),input);
    }
    @Test void acceptsOnlyTheDeadlightHealthEndpoint() throws Exception {
        assertDoesNotThrow(()->health(200,"{\"status\":\"ok\",\"service\":\"deadlight-records\"}"));
        assertThrows(IOException.class,()->health(200,"{\"status\":\"ok\",\"mode\":\"gametest\"}"));
        assertThrows(IOException.class,()->health(200,"{\"status\":\"ok\"}"));
        assertThrows(IOException.class,()->health(200,"<!doctype html><html>loading</html>"));
        assertThrows(IOException.class,()->health(503,"unavailable"));
    }
    private static void health(int status,String body) throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/health",exchange->{
            byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {ServerAddress.check("http://127.0.0.1:"+server.getAddress().getPort());}
        finally {server.stop(0);}
    }
}
