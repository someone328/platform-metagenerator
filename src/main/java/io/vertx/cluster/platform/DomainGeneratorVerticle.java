package io.vertx.cluster.platform;

import java.util.Optional;

import io.swagger.codegen.v3.cli.SwaggerCodegen;
import io.vertx.cluster.platform.util.FileRcursiveEraiser;
import io.vertx.cluster.platform.util.GitCloneWrapper;
import io.vertx.core.AbstractVerticle;

public class DomainGeneratorVerticle extends AbstractVerticle {

    @Override
    public void start() {
        try {
            String gitUrlFrom = Optional.of(config().getString("gitUrlFrom"))
                                        .get();
            String gitUrlTo = Optional.of(config().getString("gitUrlTo"))
                                      .get();
            GitCloneWrapper.cloneUrl(gitUrlFrom);
            GitCloneWrapper.cloneUrl(gitUrlTo);
            String outputGitFolderName = GitCloneWrapper.calculateFolderNameFrom(gitUrlTo);
            generateDomain(GitCloneWrapper.calculateFolderNameFrom(gitUrlFrom),
                           outputGitFolderName);
            vertx.setPeriodic(200,
                              i -> vertx.fileSystem()
                                        .exists(outputGitFolderName + "/.swagger-codegen/VERSION",
                                                h -> {
                                                    System.out.println("XXXXXXXXXXXXXXXXX "+ h);
                                                    if(!h.result()) {
                                                        return;
                                                    }
                                                    GitCloneWrapper.pushToUrl(gitUrlTo);
                                                    vertx.setTimer(500,
                                                                   handler -> vertx.undeploy(deploymentID()));
                                                    vertx.cancelTimer(i);
                                                }));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void generateDomain(String gitFolder, String generatedDomainGitUrl) throws Exception {
        FileRcursiveEraiser.deleteRecursive(generatedDomainGitUrl, true);

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

    /*
     * private void generateDomain(String gitFolder, String generatedDomainGitUrl)
     * throws Exception { FileRcursiveEraiser.deleteRecursive(generatedDomainGitUrl
     * +"/src");
     * 
     * JCodeModel codeModel = new JCodeModel(); GenerationConfig config =
     * createConfig(); SchemaMapper mapper = new SchemaMapper(new
     * VertxPlatformRuleFactory(config, new Jackson2Annotator(config), new
     * SchemaStore()), new SchemaGenerator());
     * 
     * Files.find(Paths.get(gitFolder), 999, (path, attr) -> path.toString()
     * .endsWith(".json")) .forEach(path -> generate(codeModel, mapper, path));
     * 
     * codeModel.build(Files.createDirectories(Paths.get(generatedDomainGitUrl
     * +"/src")) .toFile());
     * 
     * }
     * 
     * private GenerationConfig createConfig() { GenerationConfig config = new
     * DefaultGenerationConfig() {
     * 
     * @Override public boolean isGenerateBuilders() { return true; }
     * 
     * @Override public boolean isIncludeJsr303Annotations() { return true; }
     * 
     * @Override public boolean isUseTitleAsClassname() { return true; }
     * 
     * @Override public boolean isIncludeAdditionalProperties() { return false; }
     * 
     * @Override public String getDateTimeType() { return "java.time.LocalDateTime";
     * }
     * 
     * @Override public String getDateType() { return "java.time.LocalDate"; }
     * 
     * }; return config; }
     * 
     * private void generate(JCodeModel codeModel, SchemaMapper mapper, Path path) {
     * try { mapper.generate(codeModel, "ClassName",
     * "io.vertx.cluster.platform.domain", path.toUri() .toURL()); } catch
     * (MalformedURLException e) { throw new RuntimeException(e); } }
     */

    @Override
    public void stop() throws Exception {
        System.out.println("Domain generating done. Verticle will be undeployed.");
    }

}
