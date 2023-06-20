# ProtoSky
ProtoSky skyblock mod.

## Requirements (Mod users)
- Fabric Installer : https://fabricmc.net/use/

## Compiling
- Clone this repo.
- Run `gradlew genSources idea` for IntelliJ and `gradlew genSources eclipse` for Eclipse
  You should be ready to develop.
- Run `gradlew build` if you want to create mod jars.

## Notes
ChunkStatusMixin.java is where all the action happens. It's also where I commented everything, so it will be way easier to figure out what's happening.