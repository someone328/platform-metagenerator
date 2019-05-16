package io.vertx.cluster.platform.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileRcursiveEraiser {
  public static void deleteRecursive(String folderToDelete, boolean skipGitServiceFolder)
      throws Exception {
    System.out.println("DELETE");
    Path path = Paths.get(folderToDelete);
    if (path.toFile().exists()) {
      Files.walk(path)
          .filter(
              currPath ->
                  skipGitServiceFolder ? !currPath.startsWith(folderToDelete + "/.git") : true)
          .peek(System.out::println)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }
}
