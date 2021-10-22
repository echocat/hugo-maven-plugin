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
    protected ConfigAndOutput configAndOutput() throws MojoFailureException {
        return configAndOutputOf(
            config(),
            output()
        );
    }

    @Nonnull
    protected Config config() throws MojoFailureException {
        final Path path = ofNullable(config)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("config property missing."));
        return configOf(path);
    }

    @Nonnull
    protected Path output() throws MojoFailureException {
        return ofNullable(output)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("output property missing."));
    }

}
