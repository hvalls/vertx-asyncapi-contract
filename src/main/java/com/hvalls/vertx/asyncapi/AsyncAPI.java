package com.hvalls.vertx.asyncapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class AsyncAPI {

    /**
     * Creates a new AsyncAPIMessageProducer
     *
     * @param vertx
     * @param url     local path of your AsyncAPI spec
     * @param handler
     */
    public static void createProducer(Vertx vertx, String url, String channel,
        Handler<AsyncResult<AsyncAPIMessageProducer>> handler) {
        vertx.executeBlocking((Promise<AsyncAPIMessageProducer> promise) -> {
            try {
                JSONObject spec = readSpec(url);
                if (spec == null) {
                    promise.fail(new RuntimeException("error deserializing AsyncAPI spec"));
                    return;
                }
                JSONObject messageSchema = readMessageSchema(channel, spec);
                MessageProducer<JsonObject> delegate = vertx.eventBus().publisher(channel);
                promise.complete(new AsyncAPIMessageProducer(delegate, messageSchema));
            } catch (Throwable t) {
                promise.fail(t);
            }
        }, handler);
    }

    private static JSONObject readMessageSchema(String channel, JSONObject spec) {
        return spec.getJSONObject("channels")
            .getJSONObject(channel)
            .getJSONObject("publish")
            .getJSONObject("message")
            .getJSONObject("payload");
    }

    private static JSONObject readSpec(String url) {
        try {
            String location = url.replace("\\\\", "/");

            Path path;
            if (location.toLowerCase().startsWith("file:")) {
                path = Paths.get(URI.create(location));
            } else {
                path = Paths.get(location);
            }

            String data;
            if (Files.exists(path)) {
                data = FileUtils.readFileToString(path.toFile(), "utf-8");
            } else {
                data = ClasspathHelper.loadFileFromClasspath(location);
            }

            return new JSONObject(
                getMapper(data).readTree(data).toString()
            );
        } catch (Throwable t) {
            return null;
        }
    }

    private static ObjectMapper getMapper(String data) {
        if (data.trim().startsWith("{")) {
            return ObjectMapperFactory.createJson();
        } else {
            return ObjectMapperFactory.createYaml();
        }
    }

}