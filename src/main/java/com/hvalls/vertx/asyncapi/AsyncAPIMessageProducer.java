package com.hvalls.vertx.asyncapi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

public class AsyncAPIMessageProducer implements MessageProducer<JsonObject> {

    private final MessageProducer<JsonObject> delegate;
    private final Schema messageSchema;

    AsyncAPIMessageProducer(MessageProducer<JsonObject> delegate, JSONObject messageSchema) {
        this.delegate = delegate;
        this.messageSchema = SchemaLoader.load(messageSchema);
    }

    @Override
    public MessageProducer<JsonObject> send(JsonObject message) {
        validateMessage(message);
        return delegate.send(message);
    }

    @Override
    public <R> MessageProducer<JsonObject> send(JsonObject message,
        Handler<AsyncResult<Message<R>>> replyHandler) {
        validateMessage(message);
        return delegate.send(message, replyHandler);
    }

    @Override
    public MessageProducer<JsonObject> exceptionHandler(Handler<Throwable> handler) {
        return delegate.exceptionHandler(handler);
    }

    @Override
    public MessageProducer<JsonObject> write(JsonObject data) {
        validateMessage(data);
        return delegate.write(data);
    }

    @Override
    public MessageProducer<JsonObject> write(JsonObject data, Handler<AsyncResult<Void>> handler) {
        try {
            validateMessage(data);
        } catch (ValidationException e) {
            notifyException(handler, e);
            return this;
        }
        return delegate.write(data, handler);
    }

    @Override
    public MessageProducer<JsonObject> setWriteQueueMaxSize(int maxSize) {
        return delegate.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public boolean writeQueueFull() {
        return delegate.writeQueueFull();
    }

    @Override
    public MessageProducer<JsonObject> drainHandler(Handler<Void> handler) {
        return delegate.drainHandler(handler);
    }

    @Override
    public MessageProducer<JsonObject> deliveryOptions(DeliveryOptions options) {
        return delegate.deliveryOptions(options);
    }

    @Override
    public String address() {
        return delegate.address();
    }

    @Override
    public void end() {
        delegate.end();
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        delegate.end(handler);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        delegate.close(handler);
    }

    private void validateMessage(JsonObject message) {
        if (message != null) {
            messageSchema.validate(new JSONObject(message.toString()));
        }
    }

    private <R> void notifyException(Handler<AsyncResult<Void>> replyHandler,
        ValidationException e) {
        replyHandler.handle(new AsyncResult<>() {
            @Override
            public Void result() {
                return null;
            }

            @Override
            public Throwable cause() {
                return e;
            }

            @Override
            public boolean succeeded() {
                return false;
            }

            @Override
            public boolean failed() {
                return true;
            }
        });
    }

}