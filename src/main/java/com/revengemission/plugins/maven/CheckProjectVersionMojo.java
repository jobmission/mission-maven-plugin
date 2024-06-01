package com.revengemission.plugins.maven;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * maven lib版本检查
 **/
@Mojo(
    name = "check-project-version",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    threadSafe = true
)
public class CheckProjectVersionMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(property = "project.basedir", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "libs-version.json")
    private File rulesUri;

    @Parameter(defaultValue = "true")
    private boolean verbose;

    @Parameter(defaultValue = "https://hub.docker.com/v2")
    private String dockerApiEndpoint;

    @Parameter(defaultValue = ".*[-_\\.](alpha|Alpha|ALPHA|b|beta|Beta|BETA|rc|RC|M|EA)[-_\\.]?[0-9]*")
    private String draftReleaseRegex;

    @Parameter(defaultValue = "https://repo1.maven.org/maven2")
    private String mavenApiEndpoint;

    @Parameter(defaultValue = "https://api.github.com")
    private String githubApiEndpoint;

    @Parameter(defaultValue = "https://registry.npmjs.org/-/v1")
    private String npmApiEndpoint;

    DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    DateTimeFormatter yyyyMMddHHmmssHumanized = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    /**
     * execute
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        StringBuilder newVersionsStringBuilder = new StringBuilder();
        this.project.getDependencies().forEach(dependency -> {
            try {
                // maven也可以拼接这个地址
                // https://maven.aliyun.com/repository/public
                // https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml
                // String url = String.format("https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&rows=20&wt=json", libVersion.getOwner(), libVersion.getRepository());
                String url = String.format("%s/%s/%s/maven-metadata.xml", mavenApiEndpoint, dependency.getGroupId().replaceAll("\\.", "/"), dependency.getArtifactId());
                HttpRequest request = HttpRequest.newBuilder(new URI(url)).build();

                HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                String responseBody = httpResponse.body();
                MavenMetaData mavenMetaData = xmlMapper.readValue(responseBody, MavenMetaData.class);


                if (mavenMetaData != null && mavenMetaData.getVersioning() != null) {
                    DefaultArtifactVersion oldVersion = new DefaultArtifactVersion(dependency.getVersion());
                    String latestVersionStr = isNotEmpty(mavenMetaData.getVersioning().getLatest()) ? mavenMetaData.getVersioning().getLatest() : mavenMetaData.getVersioning().getRelease();
                    LocalDateTime lastUpdatedTime = LocalDateTime.parse(mavenMetaData.getVersioning().getLastUpdated(), yyyyMMddHHmmss);
                    long daysDiff = LocalDateTime.now().toLocalDate().toEpochDay() - lastUpdatedTime.toLocalDate().toEpochDay();
                    if (latestVersionStr.matches(draftReleaseRegex)) {
                        getLog().info(dependency.getArtifactId() + " last draft released at " + lastUpdatedTime.format(yyyyMMddHHmmssHumanized) + ", " + daysDiff + " days have passed .");
                    } else {
                        DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestVersionStr);
                        if (latestVersion.compareTo(oldVersion) > 0) {
                            newVersionsStringBuilder.append(dependency.getArtifactId()).append(" has newer version ... ").append(dependency.getVersion()).append(" -> ").append(latestVersion).append(" last released at ").append(lastUpdatedTime.format(yyyyMMddHHmmssHumanized)).append(", ").append(daysDiff).append(" days have passed !\n");
                        } else if (verbose) {
                            if (daysDiff > 365) {
                                getLog().warn(dependency.getArtifactId() + " last released at " + lastUpdatedTime.format(yyyyMMddHHmmssHumanized) + ", " + daysDiff + " days have passed !");
                            } else {
                                getLog().info(dependency.getArtifactId() + " last released at " + lastUpdatedTime.format(yyyyMMddHHmmssHumanized) + ", " + daysDiff + " days have passed .");
                            }
                        }
                    }
                } else {
                    getLog().warn(dependency.getArtifactId() + " 未获取到版本记录 ");
                }
            } catch (Exception e) {
                getLog().error("execute failed:", e);
            }
        });

        if (newVersionsStringBuilder.length() > 0) {
            getLog().info("The following libs has newer versions:\n" + newVersionsStringBuilder);
        } else {
            getLog().info("The project libs has no newer versions.");
        }
    }

    boolean isEmpty(String cs) {
        return cs == null || cs.isEmpty();
    }

    boolean isNotEmpty(String cs) {
        return !isEmpty(cs);
    }

}
