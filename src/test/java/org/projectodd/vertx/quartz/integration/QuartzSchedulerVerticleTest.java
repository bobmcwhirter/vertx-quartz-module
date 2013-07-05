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
		System.err.println( "testing scheduler");
		JsonObject request = new JsonObject();
		request.putString( "action", "schedule" );
		request.putString("address", "my.job.1" );
		request.putString( "cron", "*/2 * * * * ?");
		vertx.eventBus().registerHandler("my.job.1", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.err.println( "JOB FIRED!" );
				System.err.println( event.body() );
		        VertxAssert.testComplete();
			}
		} );
		vertx.eventBus().send("org.projectodd.quartz", request );
	}

}
