# Build Hugo Site with config directory demo

This demonstrates how to build a single page using [Hugo](https://gohugo.io/) from the given configuration directory [`config`](config).

## How it works

Run `mvn package`.

Now you should have all those resources on the one hand inside `target/generated-resources/hugo/...` but also in your resulting JAR and by this available in your classpath at `public/...`.

| Config | Output | in JAR |
| ------ | ------ | ------ |
| [`config`](config) | `target/generated-resources/hugo/` | `public/` | 

Please see [`pom.xml`](pom.xml) for details how this is being configured.
