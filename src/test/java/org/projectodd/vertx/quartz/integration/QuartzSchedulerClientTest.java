package org.projectodd.vertx.quartz.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.projectodd.vertx.quartz.QuartzSchedulerClient;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

public class QuartzSchedulerClientTest extends TestVerticle {

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
        QuartzSchedulerClient client = new QuartzSchedulerClient( vertx );
        
        jobId = client.cron("*/2 * * * * ?", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                System.err.println( "fired" );
                VertxAssert.assertEquals( "cheese should be swiss", "swiss", event.body().getString( "cheese" ) );
                fired();
            }
        }, new JsonObject().putString( "cheese", "swiss" ) );
    }

    protected void fired() {
        ++this.counter;
        if (this.counter > 3) {
            
            QuartzSchedulerClient client = new QuartzSchedulerClient( vertx );
            client.unschedule( jobId, new Handler<Message<Boolean>>() {
                @Override
                public void handle(Message<Boolean> event) {
                    VertxAssert.assertEquals("expected true", true, event.body());
                    VertxAssert.testComplete();
                }
            });
        }
    }

}
