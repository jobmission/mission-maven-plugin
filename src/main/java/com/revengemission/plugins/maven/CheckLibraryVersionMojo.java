package com.revengemission.plugins.maven;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * maven lib版本检查
 **/
@Mojo(
    name = "check-library-version",
    defaultPhase = LifecyclePhase.COMPILE,
    threadSafe = true
)
public class CheckLibraryVersionMojo extends AbstractMojo {


    @Parameter(property = "project.basedir", readonly = true, required = true)
    private File basedir;


    @Parameter(defaultValue = "libs-version.json")
    private File rulesUri;

    /**
     * execute
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        if (rulesUri == null || !rulesUri.exists()) {
            getLog().error("config json file not exists!");
            throw new RuntimeException("config json file not exists!");
        }
        try {
            List<LibVersion> libVersionList = objectMapper.readValue(rulesUri, new TypeReference<>() {
            });
            getLog().info("config json file :" + rulesUri.getName());
            if (libVersionList != null && libVersionList.size() > 0) {
                getLog().info("The following libs have newer versions:");
                libVersionList.forEach(libVersion -> {
                    if ("git".equalsIgnoreCase(libVersion.getRepositoryType())) {
                        String url = String.format("https://api.github.com/repos/%s/%s/releases?per_page=10&page=1", libVersion.getRepository(), libVersion.getName());
                        try {
                            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                                .header("Accept", "application/json")
                                .build();

                            String response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString())
                                .body();

                            List<GitReleaseModel> gitReleaseModelList = objectMapper.readValue(response, new TypeReference<>() {
                            });

                            DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(libVersion.getVersion());
                            DefaultArtifactVersion latestVersion = oldVersion;
                            for (int i = 0; i < gitReleaseModelList.size(); i++) {
                                GitReleaseModel gitReleaseModel = gitReleaseModelList.get(i);
                                if (!gitReleaseModel.isDraft() && !gitReleaseModel.isPrerelease()) {
                                    DefaultArtifactVersion gitVersion = new DefaultArtifactVersion(gitReleaseModel.getTag_name());
                                    if (gitVersion.compareTo(latestVersion) > 0) {
                                        latestVersion = gitVersion;
                                    }
                                }
                            }
                            if (latestVersion.compareTo(oldVersion) > 0) {
                                getLog().info(libVersion.getName() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
            }
        } catch (Exception e) {
            throw new MojoExecutionException("execute failed:", e);
        }
    }

}
