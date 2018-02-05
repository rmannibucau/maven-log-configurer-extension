package com.github.rmannibucau.maven.common;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;

import java.io.File;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(defaultPhase = INITIALIZE, name = "frontend-filter")
public class FrontendFilterResourceMojo extends BaseFilterMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        Stream.of(new File(project.getBasedir(), "src/main/frontend/package-template.json"),
                new File(project.getBasedir(), "package-template.json")).filter(File::exists)
                .forEach(template -> doFilter(template, new File(template, template.getName().replace("-template", ""))));
    }
}
