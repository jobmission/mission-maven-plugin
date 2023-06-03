package com.revengemission.plugins.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class YourMojoTest extends AbstractMojoTestCase {
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    /**
     * @throws Exception
     */
    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/plugin-pom.xml");

        CheckLibraryVersionMojo mojo = (CheckLibraryVersionMojo) lookupMojo("check-library-version", testPom);
        assertNotNull(mojo);
        mojo.execute();
    }
}
