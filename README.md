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
