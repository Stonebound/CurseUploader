#Automatic uploader for curseforge

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

It first tries to load options from $HOME/curse.conf and curse.conf in the working directory. If you were to include this tool in your build script you could make a curse.conf in your home directory that looks like this:
````
key=MY_API_KEY
version=0.23.5
````

And then you can use the program like `java -jar curse.jar -m MOD_ID -c CHANGE_LOG modfile.zip`, and it will load your api key and supported KSP versions from the configuration file.

The game, key, mod, changelog, and release options also accept a file to read as a parameter. For example, if you have your changelog stored in changelog.txt you can use `-c "@changelog.txt"` to have it load from there.

You can get your API key at http://kerbal.curseforge.com/my-api-tokens
