package org.echocat.maven.plugins.hugo;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_RESOURCES;
import static org.echocat.maven.plugins.hugo.model.Config.configOf;
import static org.echocat.maven.plugins.hugo.model.ConfigAndOutput.configAndOutputOf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.SelectorUtils;
import org.echocat.maven.plugins.hugo.model.Config;
import org.echocat.maven.plugins.hugo.model.ConfigAndOutput;

@Mojo(
    name = "build-multi",
    defaultPhase = GENERATE_RESOURCES,
    requiresProject = false
)
public class BuildMultiMojo extends BaseBuildMojo {

    @Parameter(
        name = "configBase",
        property = "hugo.configBase",
        required = true
    )
    private File configBase;

    @Parameter(
        name = "configIncludes"
    )
    private List<String> configIncludes;

    @Parameter(
        name = "configExcludes"
    )
    private List<String> configExcludes;

    @Parameter(
        name = "outputBase",
        property = "hugo.outputBase",
        defaultValue = "${project.build.directory}/generated-resources/hugo",
        required = true
    )
    private File outputBase;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (final ConfigAndOutput configAndOutput : configAndOutputs()) {
            log().info(format("-- build %s --", configAndOutput.config().name()));

            final String targetPath = resourcesTargetPath() + "/" + configAndOutput.config().name();
            execute(configAndOutput, targetPath);
        }
    }

    @Nonnull
    protected List<ConfigAndOutput> configAndOutputs() throws MojoFailureException, MojoExecutionException {
        final List<Config> configs = configs();

        final List<ConfigAndOutput> result = new ArrayList<>(configs.size());
        for (final Config config : configs) {
            result.add(configAndOutputFor(config));
        }
        return unmodifiableList(result);
    }

    @Nonnull
    protected List<Config> configs() throws MojoFailureException, MojoExecutionException {
        final List<Path> paths = configPaths();

        final List<Config> result = new ArrayList<>(paths.size());
        for (final Path path : paths) {
            result.add(configOf(path));
        }

        return unmodifiableList(result);
    }

    @Nonnull
    protected List<Path> configPaths() throws MojoFailureException, MojoExecutionException {
        final Path configBase = configBase();
        try (final Stream<Path> candidates = Files.list(configBase)) {
            return unmodifiableList(candidates
                .filter(this::allowedConfig)
                .collect(Collectors.toList()));
        } catch (IOException e) {
            throw new MojoExecutionException(format("Cannot collect potential config paths from %s", configBase));
        }
    }

    private boolean allowedConfig(@Nonnull Path candidate) {
        final String fileName = candidate.getFileName().toString();
        return matchesOneOf(fileName, configIncludes(), true)
            && !matchesOneOf(fileName, configExcludes(), false);
    }

    private boolean matchesOneOf(@Nonnull String str, @Nonnull Collection<String> patterns, boolean def) {
        if (patterns.isEmpty()) {
            return def;
        }
        for (final String pattern : patterns) {
            if (SelectorUtils.match(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    protected ConfigAndOutput configAndOutputFor(@Nonnull Config config) throws MojoFailureException {
        return configAndOutputOf(
            config,
            outputBase().resolve(config.name())
        );
    }

    @Nonnull
    protected List<String> configIncludes() {
        return ofNullable(configIncludes)
            .map(ArrayList::new)
            .map(Collections::unmodifiableList)
            .orElseGet(Collections::emptyList);
    }

    @Nonnull
    protected List<String> configExcludes() {
        return ofNullable(configExcludes)
            .map(ArrayList::new)
            .map(Collections::unmodifiableList)
            .orElseGet(Collections::emptyList);
    }

    @Nonnull
    protected Path configBase() throws MojoFailureException {
        return ofNullable(configBase)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("configBase property missing."));
    }

    @Nonnull
    protected Path outputBase() throws MojoFailureException {
        return ofNullable(outputBase)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("outputBase property missing."));
    }

}
