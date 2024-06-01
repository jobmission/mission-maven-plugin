package com.revengemission.plugins.maven;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.codehaus.plexus.PlexusTestCase.getBasedir;

public class CheckLibraryVersionMojoTest {
    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Ignore
    @Test
    public void testCheckLibraryVersionMojoGoal() throws Exception {

        File pom = new File(getBasedir(), "src/test/resources/mission-test-pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest configuration = executionRequest.getProjectBuildingRequest()
            .setRepositorySession(new DefaultRepositorySystemSession());
        MavenProject project = rule.lookup(ProjectBuilder.class).build(pom, configuration).getProject();

        CheckLibraryVersionMojo mojo = (CheckLibraryVersionMojo) rule.lookupConfiguredMojo(project, "check-library-version");
        Assert.assertNotNull(mojo);
        mojo.execute();
    }
}
