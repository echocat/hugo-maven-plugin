# Build multiple Hugo Sites demo

This demonstrates how to build multiple sites using [Hugo](https://gohugo.io/) from either configuration files ([`config/foo.toml`](config/foo.toml)) and directories ([`config/bar`](config/bar)).

## How it works

Run `mvn package`.

Now you should have all those resources on the one hand inside `target/generated-resources/hugo/...` but also in your resulting JAR and by this available in your classpath at `public/...`.

| Config | Output | in JAR |
| ------ | ------ | ------ |
| [`config/foo.toml`](config/foo.toml) | `target/generated-resources/hugo/foo/` | `public/foo/` | 
| [`config/bar`](config/bar) | `target/generated-resources/hugo/bar/` | `public/bar/` | 

Please see [`pom.xml`](pom.xml) for details how this is being configured.
