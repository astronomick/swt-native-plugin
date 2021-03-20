/*
 * Copyright © 2021, Michael Barbeaux
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.astronomick;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Mojo(name = "swt-native-compile", defaultPhase = LifecyclePhase.PACKAGE)
public class SwtNativeCompileMojo extends AbstractSwtSourcesMojo {

    @Parameter(property = "mainClass", required = true)
    private String mainClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String osClassifier = getOsClassifier();

        try {
            // Fetch GraalVM svm artifact.
            final Artifact graalSvmArtifact = new DefaultArtifact("org.graalvm.nativeimage", "svm", null, "jar", "21.0.0.2");
            final ArtifactResult graalSvmArtifactResult = artifactResolver.resolveArtifact(repoSession, new ArtifactRequest(graalSvmArtifact, projectRepos, ""));

            Path temp = Files.createTempDirectory("build");
            temp.toFile().deleteOnExit();
            Files.copy(getClass().getClassLoader().getResourceAsStream("graalvm/org/eclipse/swt/internal/Target_Library.java"), temp.resolve("Target_Library.java"));
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(System.in, System.out, System.err, "-classpath",
                    graalSvmArtifactResult.getArtifact().getFile().getAbsolutePath(), "-d",
                    Path.of(targetDirectory.getAbsolutePath(), "native", osClassifier, "additional-classes").toFile().getAbsolutePath(),
                    temp.resolve("Target_Library.java").toFile().getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Path temp = Files.createTempDirectory("build");
            temp.toFile().deleteOnExit();
            Files.copy(getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/jni-config.json"), temp.resolve("jni-config.json"));
            Files.copy(getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/proxy-config.json"), temp.resolve("proxy-config.json"));
            Files.copy(getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/reflect-config.json"), temp.resolve("reflect-config.json"));
            Files.copy(getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/resource-config.json"), temp.resolve("resource-config.json"));
            Files.copy(getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/serialization-config.json"), temp.resolve("serialization-config.json"));

            final Path nativePath = Path.of(targetDirectory.getAbsolutePath(), "native", osClassifier, "graalvm", "include");
            Files.createDirectories(nativePath);
            final ProcessBuilder processBuilder = new ProcessBuilder("native-image" + (SystemUtils.IS_OS_WINDOWS ? ".cmd" : ""),
                    "-cp", project.getBuild().getFinalName() + ".jar" + File.pathSeparator +
                    Path.of(targetDirectory.getAbsolutePath(), "native", osClassifier, "additional-classes").toFile().getAbsolutePath(),
                    "-H:Name=" + project.getArtifactId(),
                    "-H:Class=" + mainClass,
                    "-H:Path=" + nativePath.toFile().getAbsolutePath(),
                    "-H:JNIConfigurationFiles=" + temp.resolve("jni-config.json").toFile().getAbsolutePath(),
                    "-H:DynamicProxyConfigurationFiles=" + temp.resolve("proxy-config.json").toFile().getAbsolutePath(),
                    "-H:ReflectionConfigurationFiles=" + temp.resolve("reflect-config.json").toFile().getAbsolutePath(),
                    "-H:ResourceConfigurationFiles=" + temp.resolve("resource-config.json").toFile().getAbsolutePath(),
                    "-H:SerializationConfigurationFiles=" + temp.resolve("serialization-config.json").toFile().getAbsolutePath(),
                    "-H:TempDirectory=" + nativePath.getParent().toFile().getAbsolutePath(),
                    "-H:+ExitAfterRelocatableImageWrite",
                    "-H:+SharedLibrary");
            processBuilder.directory(targetDirectory);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            if (process.waitFor() != 0) {
                throw new MojoExecutionException("Execution of native-image returned non-zero result");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        final Path swtResourcesPath = Path.of(targetDirectory.getAbsolutePath(), "native", osClassifier, "swt");

        extractFromSwtSources("glob:library/**.*", swtResourcesPath);

        if (SystemUtils.IS_OS_MAC_OSX) {
            try {
                final ProcessBuilder processBuilder = new ProcessBuilder("make", "-f", "make_macosx.mak");
                processBuilder.directory(swtResourcesPath.resolve("library").toFile());
                String javaHome = System.getenv("JAVA_HOME");
                processBuilder.environment().put("CFLAGS_JAVA_VM", String.format("-I %1$s%2$sinclude -I %1$s%2$sinclude%2$sdarwin", javaHome, File.separator));
                processBuilder.environment().put("MACOSX_DEPLOYMENT_TARGET", "10.10");
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                if (process.waitFor() != 0) {
                    throw new MojoExecutionException("Execution of make returned non-zero result");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            try {
                final ProcessBuilder processBuilder = new ProcessBuilder("bash", "build.sh", "-gtk3");
                processBuilder.directory(swtResourcesPath.resolve("library").toFile());
                String javaHome = System.getenv("JAVA_HOME");
                processBuilder.environment().put("JAVA_HOME", javaHome);
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                if (process.waitFor() != 0) {
                    throw new MojoExecutionException("Execution of make returned non-zero result");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/launcher.c");
             InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Files.createDirectories(swtResourcesPath.getParent().resolve("build"));
            final Mustache m = mustacheFactory.compile(ir, "launcher.c");

            try (Writer w1 = new FileWriter(swtResourcesPath.getParent().resolve("build").resolve("launcher.c").toFile());
                 Writer w2 = m.execute(w1, Map.of("artifactId", project.getArtifactId()))) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("native/" + osClassifier + "/make_static.mak");
             InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Files.createDirectories(swtResourcesPath.getParent().resolve("build"));

            final Mustache m = mustacheFactory.compile(ir, "make_static.mak");
            final Path svmPath = Files.find(Path.of(targetDirectory.getAbsolutePath(), "native", osClassifier, "graalvm"),
                    1, (path, attributes) -> attributes.isDirectory() && path.getFileName().toString().startsWith("SVM"))
                    .findFirst().get();

            try (Writer w1 = new FileWriter(swtResourcesPath.getParent().resolve("build").resolve("make_static.mak").toFile());
                 Writer w2 = m.execute(w1, Map.of("artifactId", project.getArtifactId(),
                         "javaHome", System.getenv("JAVA_HOME"),
                         "svmDir", svmPath,
                         "swtLibDir", swtResourcesPath.resolve("library").toFile().getAbsolutePath()))) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("make", "-f", "make_static.mak", "all");
            processBuilder.directory(swtResourcesPath.getParent().resolve("build").toFile());
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            if (process.waitFor() != 0) {
                throw new MojoExecutionException("Execution of make returned non-zero result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
