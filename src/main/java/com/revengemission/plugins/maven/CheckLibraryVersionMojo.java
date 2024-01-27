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
                        String releasesUrl = String.format("https://api.github.com/repos/%s/%s/releases?per_page=10&page=1", libVersion.getOwner(), libVersion.getRepository());
                        try {
                            HttpRequest request = HttpRequest.newBuilder(new URI(releasesUrl))
                                .header("Accept", "application/json")
                                .build();

                            String response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString())
                                .body();

                            List<GitReleaseModel> gitReleaseModelList = objectMapper.readValue(response, new TypeReference<>() {
                            });

                            if (gitReleaseModelList != null && !gitReleaseModelList.isEmpty()) {
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
                                    getLog().info(libVersion.getRepository() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                                }
                            } else {
                                String tagsUrl = String.format("https://api.github.com/repos/%s/%s/tags?per_page=10&page=1", libVersion.getOwner(), libVersion.getRepository());
                                HttpRequest requestTags = HttpRequest.newBuilder(new URI(tagsUrl))
                                    .header("Accept", "application/json")
                                    .build();

                                String responseTags = HttpClient.newHttpClient()
                                    .send(requestTags, HttpResponse.BodyHandlers.ofString())
                                    .body();

                                List<GitReleaseModel> gitReleaseModelListTags = objectMapper.readValue(responseTags, new TypeReference<>() {
                                });

                                if (!gitReleaseModelListTags.isEmpty()) {
                                    DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(libVersion.getVersion());
                                    DefaultArtifactVersion latestVersion = oldVersion;
                                    for (int i = 0; i < gitReleaseModelListTags.size(); i++) {
                                        GitReleaseModel gitReleaseModel = gitReleaseModelListTags.get(i);
                                        DefaultArtifactVersion gitVersion = new DefaultArtifactVersion(gitReleaseModel.getName());
                                        if (gitVersion.compareTo(latestVersion) > 0) {
                                            latestVersion = gitVersion;
                                        }
                                    }
                                    if (latestVersion.compareTo(oldVersion) > 0) {
                                        getLog().info(libVersion.getRepository() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                                    }
                                } else {
                                    getLog().warn(libVersion.getRepository() + "(" + libVersion.getRepositoryType() + ") 未获取到版本记录 ");
                                }
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(releasesUrl, e);
                        }
                    } else if ("maven".equalsIgnoreCase(libVersion.getRepositoryType())) {
                        String url = String.format("https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&rows=20&wt=json", libVersion.getOwner(), libVersion.getRepository());
                        try {
                            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                                .header("Accept", "application/json")
                                .build();

                            String response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString())
                                .body();

                            MavenReleaseModel mavenReleaseModel = objectMapper.readValue(response, MavenReleaseModel.class);

                            if (mavenReleaseModel != null && mavenReleaseModel.getResponse() != null && mavenReleaseModel.getResponse().getDocs() != null) {
                                DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(libVersion.getVersion());
                                DefaultArtifactVersion latestVersion = oldVersion;
                                for (int i = 0; i < mavenReleaseModel.getResponse().getDocs().size(); i++) {
                                    MavenDocModel mavenDocModel = mavenReleaseModel.getResponse().getDocs().get(i);
                                    DefaultArtifactVersion mavenVersion = new DefaultArtifactVersion(mavenDocModel.getV());
                                    if (mavenVersion.compareTo(latestVersion) > 0) {
                                        latestVersion = mavenVersion;
                                    }
                                }
                                if (latestVersion.compareTo(oldVersion) > 0) {
                                    getLog().info(libVersion.getRepository() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                                }
                            } else {
                                getLog().warn(libVersion.getRepository() + "(" + libVersion.getRepositoryType() + ") 未获取到版本记录 ");
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(url, e);
                        }
                    } else if ("npm".equalsIgnoreCase(libVersion.getRepositoryType())) {
                        /// String url = String.format("https://registry.npmjs.org/-/v1/search?text=%s&size=5", libVersion.getRepository());
                        String url = String.format("https://registry.npmmirror.com/-/v1/search?text=%s&size=5", libVersion.getRepository());
                        try {
                            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                                .header("Accept", "application/json")
                                .build();

                            String response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString())
                                .body();

                            NpmReleaseModel npmReleaseModel = objectMapper.readValue(response, NpmReleaseModel.class);

                            if (npmReleaseModel != null && npmReleaseModel.getObjects() != null && npmReleaseModel.getObjects().size() > 0) {
                                DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(libVersion.getVersion());
                                DefaultArtifactVersion latestVersion = oldVersion;
                                for (int i = 0; i < npmReleaseModel.getObjects().size(); i++) {
                                    NpmPackageModel npmPackageModel = npmReleaseModel.getObjects().get(i);
                                    DefaultArtifactVersion npmVersion = new DefaultArtifactVersion(npmPackageModel.getPackageDetailModel().getVersion());
                                    if (libVersion.getRepository().equalsIgnoreCase(npmPackageModel.getPackageDetailModel().getName()) && npmVersion.compareTo(latestVersion) > 0) {
                                        latestVersion = npmVersion;
                                    }
                                }
                                if (latestVersion.compareTo(oldVersion) > 0) {
                                    getLog().info(libVersion.getRepository() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                                }
                            } else {
                                getLog().warn(libVersion.getRepository() + "(" + libVersion.getRepositoryType() + ") 未获取到版本记录 ");
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(url, e);
                        }
                    } else if ("docker".equalsIgnoreCase(libVersion.getRepositoryType())) {
                        String url = String.format("https://hub.docker.com/v2/repositories/library/%s/tags?page_size=10", libVersion.getRepository());
                        try {
                            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                                .header("Accept", "application/json")
                                .build();

                            String response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString())
                                .body();

                            DockerReleaseModel dockerReleaseModel = objectMapper.readValue(response, DockerReleaseModel.class);

                            if (dockerReleaseModel != null && dockerReleaseModel.getResults() != null && dockerReleaseModel.getResults().size() > 0) {
                                DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(libVersion.getVersion());
                                DefaultArtifactVersion latestVersion = oldVersion;
                                for (int i = 0; i < dockerReleaseModel.getResults().size(); i++) {
                                    DockerItemModel dockerItemModel = dockerReleaseModel.getResults().get(i);
                                    DefaultArtifactVersion dockerVersion = new DefaultArtifactVersion(dockerItemModel.getName());
                                    if (!"latest".equalsIgnoreCase(dockerItemModel.getName()) && dockerVersion.compareTo(latestVersion) > 0) {
                                        latestVersion = dockerVersion;
                                    }
                                }
                                if (latestVersion.compareTo(oldVersion) > 0) {
                                    getLog().info(libVersion.getRepository() + " has newer version ... " + libVersion.getVersion() + " -> " + latestVersion);
                                }
                            } else {
                                getLog().warn(libVersion.getRepository() + "(" + libVersion.getRepositoryType() + ") 未获取到版本记录 ");
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(url, e);
                        }
                    }

                });
            }
        } catch (Exception e) {
            throw new MojoExecutionException("execute failed:", e);
        }
    }

}
