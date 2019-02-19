package io.vertx.cluster.platform;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {

    public static void main(String[] args) throws Exception {
        DeploymentOptions options = new DeploymentOptions().setWorker(true)
                                                           .setConfig(new JsonObject().put("gitUrlFrom",
                                                                                           "git@github.com:someone328/platform-example-domain.git")
                                                                                      .put("gitUrlTo",
                                                                                           "git@github.com:someone328/platform-domain-generated.git"));
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DomainGeneratorVerticle(),
                             options,
                             res -> {
                                 if (res.succeeded()) {
                                     vertx.setPeriodic(1000,
                                                       id -> {
                                                           System.out.println(vertx.deploymentIDs());
                                                           if (vertx.deploymentIDs()
                                                                    .isEmpty()) {
                                                               vertx.close();
                                                           }
                                                       });
                                 } else {
                                     vertx.close();
                                 }
                             });

    }

}
