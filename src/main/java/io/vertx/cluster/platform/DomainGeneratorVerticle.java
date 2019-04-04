package io.vertx.cluster.platform;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.swagger.codegen.v3.cli.SwaggerCodegen;
import io.vertx.cluster.platform.util.FileRcursiveEraiser;
import io.vertx.cluster.platform.util.GitCloneWrapper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;

public class DomainGeneratorVerticle extends AbstractVerticle {

    @Override
    public void start() {
        try {
            String gitUrlFrom = config().getString("gitUrlFrom");
            String gitUrlTo = config().getString("gitUrlTo");
            GitCloneWrapper.cloneUrl(gitUrlFrom);
            GitCloneWrapper.cloneUrl(gitUrlTo);
            String outputGitFolderName = GitCloneWrapper.calculateFolderNameFrom(gitUrlTo);
            generateDomain(GitCloneWrapper.calculateFolderNameFrom(gitUrlFrom),
                           outputGitFolderName);

            wait4GenerationEndAndStopVerticle(gitUrlTo,
                                              outputGitFolderName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void wait4GenerationEndAndStopVerticle(String gitUrlTo, String outputGitFolderName) {
        Scheduler scheduler = RxHelper.scheduler(vertx);
        Flowable<Long> ticker = Flowable.interval(200,
                                                  TimeUnit.MILLISECONDS,
                                                  scheduler);
        ticker.map(tickId -> vertx.fileSystem()
                                  .rxExists(outputGitFolderName + "/.swagger-codegen/VERSION")
                                  .toFlowable())
              .flatMap(b -> b)
              .takeUntil(Boolean::booleanValue)
              .doOnComplete(() -> pushGeneratedAndStopCurrentVerticle(gitUrlTo))
              .subscribe();
    }

    private void pushGeneratedAndStopCurrentVerticle(String gitUrlTo) {
        GitCloneWrapper.pushToUrl(gitUrlTo);
        vertx.setTimer(500,
                       handler -> vertx.undeploy(deploymentID()));
    }

    private void generateDomain(String gitFolder, String generatedDomainGitUrl) throws Exception {
        FileRcursiveEraiser.deleteRecursive(generatedDomainGitUrl,
                                            true);

        String[] args2 = new String[12];
        args2[0] = "generate";
        args2[1] = "-l";
        args2[2] = "java-vertx";
        args2[3] = "-i";
        args2[4] = gitFolder + "/swagger.yaml";
        args2[5] = "-o";
        args2[6] = generatedDomainGitUrl;
        args2[7] = "--group-id";
        args2[8] = "io.vertx.cluster.platform";
        args2[9] = "--artifact-id";
        args2[10] = "domain-rx";
        args2[11] = "-DrxInterface=true,apiImplGeneration=true";
        SwaggerCodegen.main(args2);

    }

    @Override
    public void stop() throws Exception {
        System.out.println("Domain generating done. Verticle will be undeployed.");
    }

}
