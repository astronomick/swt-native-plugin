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

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class AbstractSwtSourcesMojo extends AbstractMojo {

    private static final Map<String, String> SWT_ARTIFACT_ID_BY_OS_CLASSIFIER = Map.of(
            "osx-x86_64", "org.eclipse.swt.cocoa.macosx.x86_64",
            "linux-x86_64", "org.eclipse.swt.gtk.linux.x86_64",
            "windows-x86_64", "org.eclipse.swt.win32.win32.x86_64");

    @Parameter(property = "swtVersion", required = true)
    private String swtVersion;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> projectRepos;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected File targetDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Component
    protected ArtifactResolver artifactResolver;

    @SneakyThrows // FIXME
    protected void extractFromSwtSources(final String pathPattern, final Path outputDirectory) {
        final String swtArtifactId = SWT_ARTIFACT_ID_BY_OS_CLASSIFIER.get(getOsClassifier());

        // Fetch SWT sources artifact.
        final Artifact swtSourcesArtifact = new DefaultArtifact("org.eclipse.platform", swtArtifactId, "sources", "jar", swtVersion);
        final ArtifactResult swtSourcesArtifactResult = artifactResolver.resolveArtifact(repoSession, new ArtifactRequest(swtSourcesArtifact, projectRepos, ""));

        // Unzip SWT java classes to target/generated-sources/swt
        final PathMatcher javaClassMatcher = FileSystems.getDefault().getPathMatcher(pathPattern);

        try (final ZipFile zipFile = new ZipFile(swtSourcesArtifactResult.getArtifact().getFile(), ZipFile.OPEN_READ, StandardCharsets.UTF_8)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (javaClassMatcher.matches(Path.of(entry.getName()))) {
                    final Path entryPath = outputDirectory.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        try (final InputStream in = zipFile.getInputStream(entry);
                             final OutputStream out = new FileOutputStream(entryPath.toFile())) {
                            IOUtils.copy(in, out);
                        }
                    }
                }
            }
        }
    }

    protected String getOsClassifier() {
        if (!SystemUtils.OS_ARCH.contains("64")) {
            throw new RuntimeException("64bits support only");
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return "osx-x86_64";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux-x86_64";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return "windows-x86_64";
        } else {
            throw new RuntimeException("Unknown OS classifier");
        }
    }

}
