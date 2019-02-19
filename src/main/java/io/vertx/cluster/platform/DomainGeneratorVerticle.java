package io.vertx.cluster.platform;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import com.sun.codemodel.JCodeModel;

import io.vertx.cluster.platform.util.GitCloneWrapper;
import io.vertx.core.AbstractVerticle;

public class DomainGeneratorVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        String gitUrlFrom = Optional.of(config().getString("gitUrlFrom"))
                                    .get();
        String gitUrlTo = Optional.of(config().getString("gitUrlTo"))
                                  .get();
        GitCloneWrapper.cloneUrl(gitUrlFrom);
        GitCloneWrapper.cloneUrl(gitUrlTo);
        generateDomain(GitCloneWrapper.calculateFolderNameFrom(gitUrlFrom), GitCloneWrapper.calculateFolderNameFrom(gitUrlTo));
        GitCloneWrapper.pushToUrl(gitUrlTo);

        vertx.setTimer(500,
                       handler -> vertx.undeploy(deploymentID()));
    }

    private void generateDomain(String gitFolder, String generatedDomainGitUrl) throws Exception {
        JCodeModel codeModel = new JCodeModel();
        GenerationConfig config = createConfig();
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config,
                                                               new Jackson2Annotator(config),
                                                               new SchemaStore()),
                                               new SchemaGenerator());

        Files.find(Paths.get(gitFolder),
                   999,
                   (path, attr) -> path.toString()
                                       .endsWith(".json"))
             .forEach(path -> generate(codeModel,
                                       mapper,
                                       path));

        codeModel.build(Files.createDirectories(Paths.get(generatedDomainGitUrl +"/src"))
                             .toFile());

    }

    private GenerationConfig createConfig() {
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() {
                return true;
            }

            @Override
            public boolean isIncludeJsr303Annotations() {
                return true;
            }

            @Override
            public boolean isUseTitleAsClassname() {
                return true;
            }

            @Override
            public boolean isIncludeAdditionalProperties() {
                return false;
            }

            @Override
            public String getDateTimeType() {
                return "java.time.LocalDateTime";
            }

            @Override
            public String getDateType() {
                return "java.time.LocalDate";
            }

        };
        return config;
    }

    private void generate(JCodeModel codeModel, SchemaMapper mapper, Path path) {
        try {
            mapper.generate(codeModel,
                            "ClassName",
                            "io.vertx.cluster.platform.domain",
                            path.toUri()
                                .toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Domain generating done. Verticle will be undeployed.");
    }

}
