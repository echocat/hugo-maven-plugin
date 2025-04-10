package org.echocat.maven.plugins.hugo;

import static java.util.Optional.ofNullable;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_RESOURCES;
import static org.echocat.maven.plugins.hugo.model.Config.configOf;
import static org.echocat.maven.plugins.hugo.model.ConfigAndOutput.configAndOutputOf;

import java.io.File;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.echocat.maven.plugins.hugo.model.Config;
import org.echocat.maven.plugins.hugo.model.ConfigAndOutput;
import org.echocat.maven.plugins.hugo.utils.FailureException;

@Mojo(
    name = "build",
    defaultPhase = GENERATE_RESOURCES,
    requiresProject = false
)
public class BuildMojo extends BaseBuildMojo {

    @Parameter(
        name = "config",
        property = "hugo.config",
        required = true
    )
    private File config;

    @Parameter(
        name = "output",
        property = "hugo.output",
        defaultValue = "${project.build.directory}/generated-resources/hugo",
        required = true
    )
    private File output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        execute(configAndOutput(), resourcesTargetPath());
    }

    @Nonnull
    protected ConfigAndOutput configAndOutput() throws FailureException {
        return configAndOutputOf(
            config(),
            output()
        );
    }

    @Nonnull
    protected Config config() throws FailureException {
        final Path path = ofNullable(config)
            .map(File::toPath)
            .orElseThrow(() -> new FailureException("config property missing."));
        return configOf(path);
    }

    @Nonnull
    protected Path output() throws FailureException {
        return ofNullable(output)
            .map(File::toPath)
            .orElseThrow(() -> new FailureException("output property missing."));
    }

}
