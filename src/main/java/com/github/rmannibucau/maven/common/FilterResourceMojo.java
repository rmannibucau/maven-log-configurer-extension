package com.github.rmannibucau.maven.common;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(defaultPhase = INITIALIZE, name = "filter")
public class FilterResourceMojo extends BaseFilterMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = true, property = "rmannibucau.common.filter.from")
    private File from;

    @Parameter(required = true, property = "rmannibucau.common.filter.to")
    private File to;

    @Override
    public void execute() throws MojoExecutionException {
        if (!from.exists()) {
            throw new IllegalArgumentException(from + " doesn't exist");
        }
        doFilter(from, to);
    }
}
