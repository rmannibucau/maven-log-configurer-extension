package com.github.rmannibucau.maven.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;

public abstract class BaseFilterMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    protected void doFilter(final File from, final File to) {
        try {
            final String content = FileUtils.fileRead(from);
            FileUtils.forceMkdir(to.getParentFile());
            final StringSearchInterpolator interpolator = new StringSearchInterpolator();
            interpolator.addValueSource(new PropertiesBasedValueSource(project.getProperties()));
            interpolator.addValueSource(new PropertiesBasedValueSource(session.getSystemProperties()));
            interpolator.addValueSource(new PropertiesBasedValueSource(session.getUserProperties()));
            final String filtered = interpolator.interpolate(content);
            try (final Writer writer = new BufferedWriter(new FileWriter(to))) {
                writer.write(filtered);
            }
        } catch (final IOException | InterpolationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
