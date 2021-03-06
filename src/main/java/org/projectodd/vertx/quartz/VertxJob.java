package org.projectodd.vertx.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

public class VertxJob implements Job {

	public VertxJob() {
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		EventBus eventBus = (EventBus) context.getMergedJobDataMap().get( "eventBus" );
		String address = (String) context.getMergedJobDataMap().get( "address" );
		JsonObject payload = (JsonObject) context.getMergedJobDataMap().get( "payload" );
		eventBus.send(address, payload );
	}

}
