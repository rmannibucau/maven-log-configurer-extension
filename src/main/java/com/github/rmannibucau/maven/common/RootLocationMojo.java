package com.github.rmannibucau.maven.common;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(defaultPhase = INITIALIZE, name = "rootlocation")
public class RootLocationMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "rootlocation", property = "rmannibucau.common.rootlocation.name")
    private String name;

    @Override
    public void execute() {
        MavenProject rootProject = session.getCurrentProject();
        while (rootProject.getParent() != null) {
            rootProject = rootProject.getParent();
        }
        project.getProperties().setProperty(name, rootProject.getBasedir().getAbsolutePath());
    }
}
