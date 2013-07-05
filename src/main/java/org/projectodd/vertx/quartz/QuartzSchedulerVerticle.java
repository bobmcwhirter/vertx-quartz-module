package org.projectodd.vertx.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class QuartzSchedulerVerticle extends Verticle {

	private Scheduler scheduler;

	@Override
	public void start() {
		System.err.println( "start scheduler verticle" );
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			this.scheduler.start();
			vertx.eventBus().registerHandler("org.projectodd.quartz", new Handler<Message<JsonObject>>() {
				@Override
				public void handle(Message<JsonObject> message) {
					handleMessage(message);
				}
			});
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		System.err.println( "scheduler started" );
	}

	@Override
	public void stop() {
		System.err.println( "STOP a" );
		try {
			this.scheduler.shutdown();
			this.scheduler = null;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		System.err.println( "STOP b" );
	}
	
	public void handleMessage(Message<JsonObject> message) {
		System.err.println( "handling message: " + message );
	}

}
