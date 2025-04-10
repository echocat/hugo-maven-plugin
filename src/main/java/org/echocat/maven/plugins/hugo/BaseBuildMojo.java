package org.echocat.maven.plugins.hugo;

import static java.util.Optional.ofNullable;

import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.echocat.maven.plugins.hugo.model.Config;
import org.echocat.maven.plugins.hugo.model.ConfigAndOutput;
import org.echocat.maven.plugins.hugo.utils.FailureException;

public abstract class BaseBuildMojo extends BaseMojo {

    @Parameter(
        name = "resourcesTargetPath",
        defaultValue = "public"
    )
    private String resourcesTargetPath;

    protected void execute(
        @Nonnull ConfigAndOutput configAndOutput,
        @Nonnull String targetPath
    ) throws MojoExecutionException, MojoFailureException {
        try {
            final Config config = configAndOutput.config();
            final Path output = configAndOutput.output();

            hugo().execute(arguments(
                config.parameterName(), config.path().toString(),
                "--destination", output.toString()
            ), workingDirectory());

            log().info(""); // empty finish line

            project().ifPresent(v ->
                v.addResource(toOutputResource(output, targetPath))
            );
        } catch (FailureException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected String resourcesTargetPath() {
        return ofNullable(resourcesTargetPath)
            .orElse("target");
    }

}
