package com.revengemission.plugins.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * maven 代码行数统计插件
 **/
@Mojo(
        name = "rows-count",
        defaultPhase = LifecyclePhase.COMPILE,
        threadSafe = true
)
public class RowsCountMojo extends AbstractMojo {

    /**
     * default includes
     */
    private static final String[] INCLUDES_DEFAULT = {"java", "xml", "properties"};

    @Parameter(property = "project.basedir", readonly = true, required = true)
    private File basedir;

    @Parameter(property = "project.build.sourceDirectory", readonly = true, required = true)
    private File sourceDirectory;

    @Parameter(property = "project.build.testSourceDirectory", readonly = true, required = true)
    private File testSourceDirectory;

    @Parameter(property = "project.build.resources", readonly = true, required = true)
    private List<Resource> resources;

    @Parameter(property = "project.build.testResources", readonly = true, required = true)
    private List<Resource> testResources;

    @Parameter(property = "project.build.sourceEncoding", readonly = true, required = true)
    private String sourceEncoding;

    @Parameter
    private String[] includes;

    /**
     * execute
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (includes == null || includes.length == 0) {
            includes = INCLUDES_DEFAULT;
        }
        try {
            countDir(sourceDirectory);
            countDir(testSourceDirectory);
            Set<String> resourceSet = new HashSet<>();
            for (Resource resource : resources) {
                resourceSet.add(resource.getDirectory());
            }
            if (resourceSet.size() > 0) {
                for (String resource : resourceSet) {
                    countDir(new File(resource));
                }
            }
            Set<String> testResourceSet = new HashSet<>();
            for (Resource testResource : testResources) {
                testResourceSet.add(testResource.getDirectory());
            }
            if (testResourceSet.size() > 0) {
                for (String testResource : testResourceSet) {
                    countDir(new File(testResource));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("count failed:", e);
        }
    }

    /**
     * 统计某个目录下文件的代码行
     *
     * @param dir 目录
     * @throws IOException 文件异常
     */
    private void countDir(File dir) throws IOException {
        String path = dir.getAbsolutePath().substring(basedir.getAbsolutePath().length());
        if (!dir.exists()) {
            getLog().info(path + ": directory not found !");
            return;
        }
        List<File> collected = new ArrayList<>();
        collectFiles(collected, dir);
        int lines = 0;
        for (File sourceFile : collected) {
            lines += countLine(sourceFile);
        }
        getLog().info(path + ": " + lines + " lines of code in " + collected.size() + " files (included file extension: " + Arrays.toString(includes) + ")");
    }

    /**
     * 递归获取文件列表
     *
     * @param collected 文件列表list
     * @param file      文件
     */
    private void collectFiles(List<File> collected, File file) {
        if (file.isFile()) {
            for (String include : includes) {
                if (file.getName().endsWith("." + include)) {
                    collected.add(file);
                    break;
                }
            }
        } else if (file.isDirectory() && file.listFiles() != null) {
            for (File sub : Objects.requireNonNull(file.listFiles())) {
                collectFiles(collected, sub);
            }
        }
    }

    /**
     * 读取文件的行数
     *
     * @param file 文件对象
     * @return line
     * @throws IOException 文件操作异常
     */
    private int countLine(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int line = 0;
        try {
            while (reader.ready()) {
                reader.readLine();
                line++;
            }
        } finally {
            reader.close();
        }
        return line;
    }
}
