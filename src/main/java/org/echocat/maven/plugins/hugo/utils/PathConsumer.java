package org.echocat.maven.plugins.hugo.utils;

import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

@FunctionalInterface
public interface PathConsumer {

    void accept(@Nonnull Path path) throws MojoFailureException, MojoExecutionException;

}
