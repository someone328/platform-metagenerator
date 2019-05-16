package io.vertx.cluster.platform;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

public class Main {

  public static void main(String[] args) throws Exception {

    var config =
        new JsonObject()
            .put("gitUrlFrom", "git@github.com:someone328/platform-example-domain.git")
            .put("gitUrlTo", "git@github.com:someone328/platform-domain-generated.git");
    var options = new DeploymentOptions().setWorker(true).setConfig(config);
    var vertx = Vertx.vertx();

    RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
    RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
    RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

    vertx
        .rxDeployVerticle(new DomainGeneratorVerticle(), options)
        .flatMapObservable(s -> Observable.interval(5, TimeUnit.SECONDS, Schedulers.io()))
        .subscribe(
            success -> {
              System.out.println(vertx.deploymentIDs());
              if (vertx.deploymentIDs().isEmpty()) {
                vertx.close();
              }
            },
            error -> vertx.close());
  }
}
