package org.projectodd.vertx.quartz;

import java.util.concurrent.atomic.AtomicInteger;

import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

public class QuartzSchedulerClient {

    private Vertx vertx;
    private String address;

    private AtomicInteger counter = new AtomicInteger();

    public QuartzSchedulerClient(Vertx vertx) {
        this(vertx, "org.projectodd.quartz");
    }

    public QuartzSchedulerClient(Vertx vertx, String address) {
        this.vertx = vertx;
        this.address = address;
    }

    public String cron(String cron, Handler<Message<JsonObject>> job) {
        return cron(cron, job, new JsonObject());
    }

    public String cron(String cron, Handler<Message<JsonObject>> job, JsonObject payload) {
        String jobAddress = "org.projectodd.quartz.jobs." + this.counter.getAndIncrement();
        this.vertx.eventBus().registerHandler(jobAddress, job);
        this.vertx.eventBus().send(this.address, new JsonObject()
                .putString("cron", cron)
                .putString("address", jobAddress)
                .putString("id", jobAddress)
                .putObject("payload", payload));
        System.err.println("jobId: " + jobAddress);
        return jobAddress;
    }

    public void unschedule(String jobId, Handler<Message<Boolean>> handler) {
        this.vertx.eventBus().send(this.address, new JsonObject().putString("unschedule", jobId), handler);
    }
    
    public boolean unscheduleSync(String jobId) {
        final Future<Boolean> result = new DefaultFutureResult<>();
        this.vertx.eventBus().send(this.address, new JsonObject().putString("unschedule", jobId), new Handler<Message<Boolean>>() {
            @Override
            public void handle(Message<Boolean> event) {
                result.setResult( event.body() );
            }
        } );
        
        return result.result();
    }

}
