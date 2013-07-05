package org.projectodd.vertx.quartz;

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class QuartzSchedulerVerticle extends Verticle {

	private Scheduler scheduler;

	@Override
	public void start() {
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			this.scheduler.start();

			JsonObject conf = container.config();
			String address = conf.getString("address", "org.projectodd.quartz");

			vertx.eventBus().registerHandler(address,
					new Handler<Message<JsonObject>>() {
						@Override
						public void handle(Message<JsonObject> message) {
							handleMessage(message);
						}
					});
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		System.err.println("scheduler started");
	}

	@Override
	public void stop() {
		System.err.println("STOP a");
		try {
			this.scheduler.shutdown();
			this.scheduler = null;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		System.err.println("STOP b");
	}

	public void handleMessage(Message<JsonObject> message) {
		String action = message.body().getString("action");

		if (action.equals("schedule")) {
			String triggerAddress = message.body().getString("address");
			try {
				JobDetailImpl jobDetail = new JobDetailImpl();
				jobDetail.setJobClass(VertxJob.class);
				JobKey jobKey = new JobKey( triggerAddress );
				jobDetail.setKey( jobKey );
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put("eventBus", vertx.eventBus());
				jobDataMap.put("address", triggerAddress);
				jobDetail.setJobDataMap(jobDataMap);

				CronTriggerImpl trigger = new CronTriggerImpl();
				trigger.setName( triggerAddress );
				trigger.setCronExpression(message.body().getString("cron"));
				this.scheduler.scheduleJob(jobDetail, trigger);
				System.err.println( "scheduled to: " + triggerAddress );
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		} else if (action.equals("unschedule")) {

		}
	}

}
