### What is this?
`AsyncAPIMessageProducer` provides a Vert.x EventBus `MessageProducer` validation layer from an AsyncAPI doc.
Given an AsyncAPI doc, it will check all the published messages match the expected schema.

### Example  

Put in `src/main/resources` a file called `asyncapi.yaml` with content below:

```yaml
asyncapi: 2.0.0
info:
  title: Catalog Service
  version: '1.0.0'

channels:
  book/registered:
    description: Book Registered Topic
    publish:
      message:
        name: BookRegistered
        contentType: application/json
        payload:
          type: object
          properties:
            bookId:
              type: string
            title:
              type: string
            author:
              type: string
          required: ["bookId", "title", "author"]
```

Code below shows how you can create an instance of `AsyncAPIMessageProducerFactory` and start sending messages. 

```java
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;

public class Main {

    public static String CHANNEL = "book/registered";
    public static String ASYNCAPI_FILE = "src/main/resources/asyncapi.yaml";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.eventBus().consumer(CHANNEL, (Handler<Message<JsonObject>>) event ->
            System.out.println("Message received: " + event.body().encodePrettily()));

        AsyncAPI.createProducer(vertx, ASYNCAPI_FILE, CHANNEL, result -> {
            if (result.succeeded()) {
                MessageProducer<JsonObject> messageProducer = result.result();

                JsonObject message = new JsonObject()
                    .put("bookId", "1")
                    .put("title", "Hamlet");

                messageProducer.write(message, sendingResult -> {
                    if (sendingResult.succeeded()) {
                        System.out.println("Message sent successfuly");
                    } else {
                        System.out.println(
                            "Error when sending message: " + sendingResult.cause().getMessage())
                        ;
                    }
                });
            }
        });
    }

}
```

Code above fails since the message doesn't contain `author` field and, according AsyncAPI doc, it is required. 