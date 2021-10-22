package org.echocat.maven.plugins.hugo;

import static java.util.Optional.ofNullable;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_RESOURCES;
import static org.echocat.maven.plugins.hugo.Platform.platform;

import java.io.File;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(
    name = "build",
    defaultPhase = GENERATE_RESOURCES,
    requiresProject = false
)
public class BuildMojo extends BaseHugoMojo {

    @Parameter(
        name = "config",
        required = true
    )
    private File config;

    @Parameter(
        name = "output",
        defaultValue = "${project.build.directory}/generated-resources/hugo",
        required = true
    )
    private File output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Path output = output();

        hugo().execute(arguments(
            "--configDir", config().toString(),
            "--destination", output.toString()
        ), workingDirectory());

        project().ifPresent(v ->
            v.addResource(toOutputResource(output))
        );
    }

    @Nonnull
    protected Hugo hugo() throws MojoFailureException {
        return hugoBuilder()
            .build();
    }

    @Nonnull
    protected Hugo.Builder hugoBuilder() throws MojoFailureException {
        return super.hugoBuilder()
            .withConfig(config())
            .withOutput(output())
            .withPlatform(platform())
            ;
    }

    @Nonnull
    protected Path config() throws MojoFailureException {
        return ofNullable(config)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("config property missing."));
    }

    @Nonnull
    protected Path output() throws MojoFailureException {
        return ofNullable(output)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("output property missing."));
    }

}
