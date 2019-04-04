package io.vertx.cluster.platform;

import io.swagger.codegen.v3.cli.SwaggerCodegen;

public class SwaggerMain {

    public static void main(String[] args) {
        /*
         * System.setProperty("rxInterface", "true");
         * System.setProperty("apiImplGeneration", "true");
         * System.setProperty("mainVerticleGeneration", "false");
         * 
         * String[] argsLocal = { "generate", "-ljava-vertx",
         * "-isrc/main/resources/test.yaml", //"-oplatform-domasin-generated", "-oxz",
         * //"--group-id your.group.id", //"--artifact-id your.artifact.id",
         * "-DrxInterface=true", "-DapiImplGeneration=true",
         * "-DmainVerticleGeneration=false" };
         * 
         * String version = Version.readVersionFromResources();
         * 
         * @SuppressWarnings("unchecked") Cli.CliBuilder<Runnable> builder =
         * Cli.<Runnable>builder("swagger-codegen-cli") .withDescription(String.
         * format("Swagger code generator CLI (version %s). More info on swagger.io",
         * version)) .withDefaultCommand(Langs.class) .withCommands(Generate.class,
         * Meta.class, Langs.class, Help.class, ConfigHelp.class, Validate.class,
         * Version.class);
         * 
         * builder.build() .parse(argsLocal) .run();
         */
        
        String[] args2 = new String[12];
        args2[0] = "generate";
        args2[1] = "-l";
        args2[2] = "java-vertx";
        args2[3] = "-i";
        args2[4] = "platform-example-domain/swagger.yaml";
        args2[5] = "-o";
        args2[6] = "xz";
        args2[7] = "--group-id";
        args2[8] = "io.vertx.cluster.platform";
        args2[9] = "--artifact-id";
        args2[10] = "domain-rx";
        args2[11] = "-DrxInterface=true";
        SwaggerCodegen.main(args2);
    }

}
