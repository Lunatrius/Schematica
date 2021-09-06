## Welcome to Schematica!

This is my fork of Schematica, with the goal to make it usable on Anarchy servers, and to improve its overall quality.
Eventually I'd like to update this for newer Minecraft versions, though at the moment I have no clue and no plans on how to do that- I'll save that for when I have most of my planned features implemented.
If you have any bugs or suggestions, feel free to let me know!
https://docs.google.com/forms/d/e/1FAIpQLScpT1qL_RdMT3U1m_95NmT62JIzc5gfbq5jH6PXiiWG-IakLQ/viewform?usp=sf_link

### Compiling

[Setup Schematica](#setup-schematica)

[Compile Schematica](#compile-schematica)

[Updating Your Repository](#updating-your-repository)

#### Setup Schematica
This section assumes that you have Git and JDK installed, and you're using the command-line version of Git.

1. Open up your command line.
2. Navigate to a place where you want to download Schematica's source (eg `C:\Development\Github\Minecraft\`) by executing `cd [folder location]`. This location is known as `mcdev` from now on.
3. Execute `git clone https://github.com/Theyoungster/Schematica.git`. This will download Schematica's source into `mcdev`.
4. Right now, you should have a directory that looks something like:

***
    mcdev
    \-Schematica
        \-Schematica's files (should have build.gradle)
***

#### Compile Schematica
1. Execute `gradlew setupDevWorkspace`. This sets up Forge and downloads the necessary libraries to build Schematica. This might take some time, be patient.
    * You will generally only have to do this once until the Forge version in `gradle.properties` changes.
2. Execute `gradlew build`. If you did everything right, `BUILD SUCCESSFUL` will be displayed after it finishes. This should be relatively quick.
    * If you see `BUILD FAILED`, check the error output (it should be right around `BUILD FAILED`), fix everything (if possible), and try again.
3. Go to `mcdev\Schematica\build\libs`.
    * You should see a `.jar` file named `Schematica-#.#.#-#.#.#.#-universal.jar`.
4. Copy the jar into your Minecraft mods folder, and you are done!

#### Updating Your Repository
In order to get the most up-to-date builds, you'll have to periodically update your local repository.

1. Open up your command line.
2. Navigate to `mcdev` in the console.
3. Make sure you have not made any changes to the local repository, or else there might be issues with Git.
    * If you have, try reverting them to the status that they were when you last updated your repository.
4. Execute `git pull master`. This pulls all commits from the official repository that do not yet exist on your local repository and updates it.

Shamelessly based this README off [pahimar's version](https://github.com/pahimar/Equivalent-Exchange-3).
