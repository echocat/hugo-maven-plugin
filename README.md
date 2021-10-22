# Hugo Maven plugin

Plugin that wraps hugo build processes into Maven executions.

## TOC

1. [Features](#features)
2. [Getting started](#getting-started)
3. [Goals](#goals)
   1. [Generic](#generic) 
   2. [`build`](#build) 
   3. [`build-multi`](#build-multi) 
4. [Contributing](#contributing)
5. [License](#license)

## Features

1. Ensures a Hugo binary is available in the correct version. If required: It will download and install it.
   
2. Can either build single sites or multiple at once.

3. Generated resources can be used as normal sites (at `/public`) or also directly as resources by Java application (as part of the resulting JAR).

4. Easy to configure.

## Getting started

1. Add the repository to your `pom.xml`:
    ```xml
    <pluginRepositories>
        <pluginRepository>
            <id>github</id>
            <url>https://public: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@maven.pkg.github.com/echocat/hugo-maven-plugin</url>
        </pluginRepository>
    </pluginRepositories>
    ```
    > ℹ️ We need this strange approach because (with this PAT) because of a [strange behaviour of the GitHub Maven Repositories](https://github.community/t/download-from-github-package-registry-without-authentication/14407/111). Sorry for that. 🤦

2. Bind the plugin to your build process:
    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.echocat.maven.plugins</groupId>
                <artifactId>hugo-maven-plugin</artifactId>
                <version>LATEST_VERSION</version>
                <executions>
                    <execution>
                        <!-- Give this execution a name,
                             build might be a good one. -->
                        <id>build</id>
                        <goals>
                            <!-- `build` is the default goal.
                                 See below. -->
                            <goal>build</goal>
                        </goals>
                        <configuration>
                            <!-- Configuration to pick up -->
                            <config>config.toml</config>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- [...] -->
        </plugins>
        <!-- [...] -->
    </build>
    ```

3. Now you can run `mvn package`

Now you should have all those resources on the one hand inside `target/generated-resources/hugo/...` but also in your resulting JAR and by this available in your classpath at `public/...`.

Please see [demos](demos) for many easy demonstrations how to configure and what the result will be.

## Goals

1. [Generic](#generic) which applies to all goals.
2. [`build`](#build) to build a single hugo site.
3. [`build-multi`](#build-multi) to build multiple hugo sites at once.

### Generic

This applies to all following goals.

| Name in POM | Property | Type | Description | Default |
| ----------- | -------- | ---- | ----------- | ------- |
| `version` | `hugo.version` | `string` | Version of hugo which should be used. See: [Available releases](https://github.com/gohugoio/hugo/releases). | `0.88.1` |
| `download` | `hugo.download` | `never`,`always`,`onDemand` | Under which conditions hugo should be downloaded. | `onDemand` |
| `workingDirectory` | `hugo.workingDirectory` | `path` | Where hugo should be executed in. | `${project.basedir}` |
| `additionalArguments` | | `[]string` | Additional arguments to pass to hugo executable. | |
| `outputIncludes` | | `[]string` | Glob pattern of resources which should be included into the output (if any). | |
| `outputExcludes` | | `[]string` | Glob pattern of resources which should NOT be included into the output (if any). | |
| `environment` | `hugo.environment` | `string` | Environment to pass to hugo. | |
| `resourcesTargetPath` | | `string` | Target path to store the resources inside the JARs/classpath with. | `public` |

### `build`

This will build a single hugo site.

| Name in POM | Property | Type | Description | Default |
| ----------- | -------- | ---- | ----------- | ------- |
| 🚩 `config` | `hugo.config` | `path` | Configuration file or directory. See: [Hugo configuration](https://gohugo.io/getting-started/configuration/) | |
| `output` | `hugo.output` | `path` | Output directory where to store the generated resources at. | `${project.build.directory}/generated-resources/hugo` |

> 🚩 = required

### `build-multi`

| Name in POM | Property | Type | Description | Default |
| ----------- | -------- | ---- | ----------- | ------- |
| 🚩 `configBase` | `hugo.configBase` | `path` | Base directory where all configuration file or directories are located inside. See: [Hugo configuration](https://gohugo.io/getting-started/configuration/) and [demonstration](demos/build-multi). | |
| `configIncludes` | | `[]string` | Glob pattern for configuration files/directories which should be included (if any). | |
| `configExcludes` | | `[]string` | Glob pattern for configuration files/directories which should NOT be included (if any). | |
| `outputBase` | `hugo.outputBase` | `path` | Base output directory where to store the generated resources at. See: [demonstration](demos/build-multi). | `${project.build.directory}/generated-resources/hugo` |

> 🚩 = required

## Contributing

**hugo-maven-plugin** is an open source project by [echocat](https://echocat.org). So if you want to make this project even better, you can contribute to this project on [Github](https://github.com/echocat/hugo-maven-plugin) by [fork us](https://github.com/echocat/hugo-maven-plugin/fork).

If you commit code to this project, you have to accept that this code will be released under the [license](#license) of this project.

## License

See the [LICENSE](LICENSE) file.
