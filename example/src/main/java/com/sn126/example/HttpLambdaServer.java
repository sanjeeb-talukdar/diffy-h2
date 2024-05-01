package com.sn126.example;

import java.util.function.Function;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class HttpLambdaServer {
    private final Logger log = LoggerFactory.getLogger(HttpLambdaServer.class);

    private final DisposableServer server;

    public HttpLambdaServer(int port, Function<String, String> lambda) {
        server = HttpServer.create()
                .port(port)
                .handle((req, res) -> {
                    log.info("Received traffic on port {}", port);
                    return Mono.fromFuture(
                            req.receive().aggregate().asString().toFuture().thenApply(lambda))
                            .flatMap(responseBody -> res.header("UPPERCASE_TO_LOWERCASE", "must_convert")
                                    .sendString(Mono.justOrEmpty(responseBody))
                                    .then());
                }).bindNow();
    }

    public static void main(String[] args) throws Exception {
        HttpLambdaServer primary = new HttpLambdaServer(Integer.parseInt(args[0]), s -> s);
        HttpLambdaServer secondary = new HttpLambdaServer(Integer.parseInt(args[1]), s -> s);
        HttpLambdaServer candidate = new HttpLambdaServer(Integer.parseInt(args[2]), (s)-> {
            try{JSONObject object = new JSONObject(s);
            object.append("experiment", "value");
            return object.toString();}catch(Exception e){return s;}
        });

        primary.server.onDispose().block();
        secondary.server.onDispose().block();
        candidate.server.onDispose().block();
    }
}
