# Schematica

Schematica is an open-source (CC BY-NC-SA 3.0) modification for [Minecraft](http://www.minecraft.net/).

## Getting started

The project requires a parent [MinecraftForge](https://github.com/MinecraftForge/MinecraftForge) project to successfuly compile.

## Building the jar file

To build a jar file, that can be installed alongside MinecraftForge, you'll have to create the file build.properties with the following properties (the default values may be changed to your liking):

```
dir.project=${basedir}
dir.workspace=${dir.project}/..
dir.mcp=${dir.workspace}/mcp
dir.release=${dir.project}/release
```

## Localization contributions

For a list of all contributors after the change in c202e5db333660ac619291da782f439af8a72897 check out the project at [crowdin.net](http://crowdin.net/project/schematica).
Any contributions prior the commit can be found in the git logs.
