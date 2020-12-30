package com.github.rmannibucau.maven.log.configurer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "rmannibucau-log-configurer")
public class LogConfigurer extends AbstractMavenLifecycleParticipant {

    @Override
    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        if (session.getCurrentProject() == null) {
            return;
        }

        final MavenProject currentProject = session.getCurrentProject();
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = thread.getContextClassLoader();
        final ClassLoader plexusCore;
        try {
            plexusCore = ClassRealm.class.cast(Thread.currentThread().getContextClassLoader()).getWorld().getRealm("plexus.core");
            thread.setContextClassLoader(plexusCore);
        } catch (final NoSuchRealmException e) {
            throw new IllegalStateException(e);
        }
        try {
            final Class<?> simpleLogger = plexusCore.loadClass("org.slf4j.impl.SimpleLogger");
            try {
                final Field props = simpleLogger.getDeclaredField("SIMPLE_LOGGER_PROPS");
                props.setAccessible(true);
                configure(currentProject, Properties.class.cast(props.get(null)));

                try { // now we are ready for the config, relaunch the config init
                    final Field initialized = simpleLogger.getDeclaredField("INITIALIZED");
                    initialized.setAccessible(true);
                    initialized.set(null, false);
                } catch (final Exception e) {
                    throw new MavenExecutionException(e.getMessage(), e);
                }

                plexusCore.loadClass("org.slf4j.impl.SimpleLoggerFactory").getConstructor().newInstance(); // triggers the init
            } catch (final NoSuchFieldException nsfe) { // 3.5.3?
                try {
                    final Field configParams = simpleLogger.getDeclaredField("CONFIG_PARAMS");
                    configParams.setAccessible(true);
                    final Object config = configParams.get(null);
                    final Field props = config.getClass().getDeclaredField("properties");
                    props.setAccessible(true);
                    configure(currentProject, Properties.class.cast(props.get(config)));
                    final Method init = config.getClass().getDeclaredMethod("init");
                    init.setAccessible(true);
                    thread.setContextClassLoader(loader); // empty simplelogger.properties to not override what we did
                    init.invoke(config);
                } catch (final Exception e) {
                    throw new MavenExecutionException(e.getMessage(), e);
                }
            } catch (final Exception e) {
                throw new MavenExecutionException(e.getMessage(), e);
            }
        } catch (final ClassNotFoundException cnfe) {
            // no-op (mvnd)
        } catch (final Exception e) {
            throw new MavenExecutionException(e.getMessage(), e);
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    private void configure(final MavenProject project, final Properties properties) {
        if (project == null) {
            return;
        }
        properties.putAll(project.getProperties());
        properties.putAll(System.getProperties()); // potential overrides so keep doing it
        configure(project.getParent(), properties);
    }
}
