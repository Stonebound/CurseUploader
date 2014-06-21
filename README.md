# Automatic uploader for curseforge #

This program intended for Kerbal Space Program mod authors to be able to quickly and easily push updates to CurseForge. Currently there is only the command line program, but a GUI version is planned for the future.

*Note: This was intended for the KSP CurseForge site. Theoretically it should work with other CurseForge sites, but it has not been thoroughly tested.*

## Requirements ##

1. CurseForge account - *Created here: https://www.curseforge.com/home/create/*
2. CurseForge API Key - *Obtained here: http://kerbal.curseforge.com/my-api-tokens*
3. An existing mod on CurseForge - *http://kerbal.curseforge.com/ksp-mods/create* (The official CurseForge API does not allow for creation of mods via API)

## Usage ##

### Method One - Command Line Arguments ###

There are two methods to use this tool. The first method is purely command line, and can be executed from the mod's directory directly using all the flags. See below for all program flags:

````
Usage:
  java -jar curse.jar [--help] (-g|--game) <game> (-k|--key) <key> (-m|--mod) <mod>
  [(-c|--changelog) <changelog>] (-t|--type) <release> (-v|--version)
  version1,version2,...,versionN  <file>
  
Example:
  java -jar curse.jar --key 12345678-1234-1234-1234-123456789 --mod 123456
    --changelog "Added more bugs" --version 0.23.5,0.23,0.22 MyMod.zip

Uploads build artifacts to Curseforge

  [--help]
        Prints this help message.

  (-g|--game) <game>
        The Curseforge site to use (default: kerbal)

  (-k|--key) <key>
        Your Curseforge API key. Can be obtained at
        http://kerbal.curseforge.com/my-api-tokens

  (-m|--mod) <mod>
        The ID of your mod on Curseforge. Can be found in the URL:
        http://kerbal.curseforge.com/ksp-mods/MOD_ID_HERE-MOD_NAME_HERE

  [(-c|--changelog) <changelog>]
        Changelog text for this release

  (-t|--type) <release>
        Valid values: release,beta,alpha (default: release)

  (-v|--version) version1,version2,...,versionN 
        Versions of the game that your mod supports (example: 0.23.5,0.23)

  <file>
        The file to release to Curseforge
````

### Method Two - curse.conf settings file ###

If you push a lot of updates or update multiple mods, it might help to keep your settings in a curse.conf file.

It first tries to load options from $HOME/curse.conf and curse.conf in the working directory. If you were to include this tool in your build script you could make a curse.conf in your home directory that looks like this:
````
key=MY_API_KEY
version=0.23.5
````

And then you can use the program like `java -jar curse.jar -m MOD_ID -c CHANGE_LOG modfile.zip`, and it will load your api key and supported KSP versions from the configuration file.

The game, key, mod, changelog, and release options also accept a file to read as a parameter. For example, if you have your changelog stored in changelog.txt you can use `-c "@changelog.txt"` to have it load from there.

You can get your API key at http://kerbal.curseforge.com/my-api-tokens

### Method Three - Jenkins

Just search for Curseforge in your Jenkins addon manager, and install the plugin (Source can be found [here](https://github.com/jenkinsci/curseforge-publisher-plugin)). When configuring your project add the new Post-Build Action "Publish to Curseforge" and fill in your details.

![example](http://i.imgur.com/RzTqejI.png)

Hint: If you use Git you can add the following shell script as a build step to generate your changelog from your commit log:
````
tagfile="../${JOB_NAME}_LAST_COMMIT"
changefile="../${JOB_NAME}_CHANGELOG"
log="git log"
changelog=""
if [ -e "$tagfile" ]; then
  changelog=$($log $(cat "$tagfile")..HEAD)
else
  changelog=$($log)
fi
if [ -n "$changelog" ]; then
  echo "$changelog" > "$changefile"
fi
echo $(git rev-parse HEAD) > "$tagfile"
cat "$changefile" > changelog.txt
````

And then use @changelog.txt as your changelog.
