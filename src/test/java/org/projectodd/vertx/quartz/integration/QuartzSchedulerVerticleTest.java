package org.projectodd.vertx.quartz.integration;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import static org.junit.Assert.*;

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
		vertx.eventBus().send("org.projectodd.quartz", new JsonObject() );
		Thread.sleep( 2000 );
		VertxAssert.testComplete();
	}

}
