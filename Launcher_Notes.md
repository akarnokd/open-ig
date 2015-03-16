

See the [Launcher](Launcher.md) page about the application itself.

# Version 0.37 - Language pack management #
> [Blog](http://open-ig-dev.blogspot.hu/2012/08/launcher-037-released.html)

# Version 0.35 - Additional launch options #
  * Added JVM and Game parameters to the run settings dialog ([Issue #591](https://code.google.com/p/open-ig/issues/detail?id=591))
  * Instantly start from the last save button. ([Issue #618](https://code.google.com/p/open-ig/issues/detail?id=618))
  * First-time launch options fixed. ([Issue #636](https://code.google.com/p/open-ig/issues/detail?id=636))


# Version 0.34 - Detect bad install #
  * Added code to the Launcher and Game to detect missing resources and start the verification automatically. ([Issue #606](http://code.google.com/p/open-ig/issues/detail?id=606))

# Version 0.33 - Minor improvements #
  * Downloading and installing the game under Firefox should work properly now ([Issue #597](https://code.google.com/p/open-ig/issues/detail?id=597))
  * Added first time start graphics options ([Issue #539](https://code.google.com/p/open-ig/issues/detail?id=539))
  * Fixed uninstall error ([Issue #538](https://code.google.com/p/open-ig/issues/detail?id=538))

# Version 0.24 - Bug fixes #
  * Fixed crash when existing files had zero size
  * Fixed issue [#240](http://code.google.com/p/open-ig/issues/detail?id=240): memory field number formatting error

# Version 0.22 - Bug fixes #

  * Fixed Launcher JDK 6 incompatibility ([Issue #228](http://code.google.com/p/open-ig/issues/detail?id=228))
  * Fixed detection of installed game version ([Issue #236](http://code.google.com/p/open-ig/issues/detail?id=236))

# Version 0.20 - Improvements #

  * More details on the download progress
  * More IG-like look
  * Downloads a single game Jar file, as all other tools are already included
  * Ability to install the game anywhere, the launcher then will move itself to the new location as well.

# Version 0.13 - Bugfix release #

  * Fixed issue causing the game to hang when run from the Launcher.

# Version 0.12 - Bugfix release #

  * Fixed a related bug where the application restarts itself infinitely, typically on Linux machines.
  * Removed the Verify button - which did nothing.

# Version 0.11 - Bugfix release #

  * Fixed a bug when the user quit the launcher by using the **Exit** button, the Launcher forgot what modules where installed. Fortunately, clicking on the Install buttons, the launcher recognized that the files are already there and intact, therefore, no unnecessary network bandwith was required.