package org.projectodd.vertx.quartz;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class QuartzSchedulerVerticle extends Verticle {

    private Scheduler scheduler;
    private AtomicInteger counter = new AtomicInteger();

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
    }

    @Override
    public void stop() {
        try {
            this.scheduler.shutdown();
            this.scheduler = null;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Message<JsonObject> message) {

        if (message.body().getString("cron") != null) {
            String id = message.body().getString("id");
            
            if ( id == null ) {
                id = "job-" + this.counter.getAndIncrement();
            }
            
            String triggerAddress = message.body().getString("address");

            try {
                JobDetailImpl jobDetail = new JobDetailImpl();
                jobDetail.setJobClass(VertxJob.class);
                JobKey jobKey = new JobKey(id);
                jobDetail.setKey(jobKey);
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("eventBus", vertx.eventBus());
                jobDataMap.put("address", triggerAddress);
                jobDataMap.put("payload", message.body().getObject("payload", new JsonObject()));
                jobDetail.setJobDataMap(jobDataMap);
                
                System.err.println( "scheduling: " + id + " to " + triggerAddress );

                CronTriggerImpl trigger = new CronTriggerImpl();
                trigger.setName(id);
                trigger.setCronExpression(message.body().getString("cron"));
                this.scheduler.scheduleJob(jobDetail, trigger);
                message.reply(new JsonObject().putString("job", id));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } else if (message.body().getString("unschedule") != null) {
            String id = message.body().getString("unschedule");
            try {
                this.scheduler.deleteJob(new JobKey(id));
                message.reply(true);
            } catch (SchedulerException e) {
                e.printStackTrace();
                message.reply(false);
            }

        }
    }

}
