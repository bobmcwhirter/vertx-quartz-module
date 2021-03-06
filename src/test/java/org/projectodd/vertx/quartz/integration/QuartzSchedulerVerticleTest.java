package org.projectodd.vertx.quartz.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

public class QuartzSchedulerVerticleTest extends TestVerticle {

    private int counter;
    private String jobId;

    @Override
    public void start() {
        container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                startTests();
            }
        });
        super.start();
    }

    @Test
    public void testScheduler() throws InterruptedException {
        JsonObject request = new JsonObject();
        request.putString("address", "my.job.1");
        request.putString("cron", "*/2 * * * * ?");
        request.putObject("payload", new JsonObject().putString( "cheese", "cheddar" ) );
        vertx.eventBus().registerHandler("my.job.1", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                VertxAssert.assertEquals( "cheese should be cheddar", "cheddar", event.body().getString( "cheese" ) );
                fired();
            }
        });
        vertx.eventBus().send("org.projectodd.quartz", request, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                jobId = event.body().getString("job");
            }

        });
    }

    protected void fired() {
        ++this.counter;
        if (this.counter > 3) {
            vertx.eventBus().send( "org.projectodd.quartz", new JsonObject().putString("unschedule", jobId ), new Handler<Message<Boolean>>() {
                @Override
                public void handle(Message<Boolean> event) {
                    VertxAssert.assertEquals("expected true", true, event.body());
                    VertxAssert.testComplete();
                }
            });
        }
    }

}
