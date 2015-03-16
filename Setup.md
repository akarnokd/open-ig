# Introduction #

This page describes the new setup program used for Open Imperium Galactica.


# Details #

## Language page ##

![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-1.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-1.png)

This page allows the user to switch between the supported languages. In version 0.8, these options are hard coded to Hungarian and English.

## Files page ##
![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-2.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-2.png)

The most important setup page is the Files page. Here must the resource files or directories be selected:

  * Image directories or image ZIP file(s), named open-ig-images-`*`.zip
  * Audio directories or audio ZIP file(s), named open-ig-audio-`*`.zip
  * Video directories or video ZIP file(s), named open-ig-video-`*`.zip
  * Data directories or data ZIP file(s), named open-ig-data-`*`.zip

To allow patching/changing the resources without re-downloading everything, the system supports ordering of these files: put newer or override files in front.

There is an auto locate option to look for these files in the active directory.

## Graphics page ##
![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-3.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-3.png)

On this page, the user can set the game window's position and size. This is useful when a predefined sized screenshot or video recording needs to be done.

The troubleshooting options lets the user disable some acceleration properties if performance or rendering anomalies occur.

## Audio page ##
![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-4.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-4.png)

The page allows setting some active/default volume and audio filtering options, then test the settings using the welcome.wav.

The number of audio channels allows the user to limit the number of parallel audio played  back simultaneously, as some systems might not accept an arbitrary amount of parallel sound.

The music will be played back on a higher quality as most original effect and video sound, which are a bit noisy. The audio playback is fitted with a moving average filter, which reduces the noise (and the normal sound) level by averaging neighboring audio samples. The step count can be adjusted and listened to. In general, step size of 2-4 hears nicely. Note, that the averaging reduces the overall amplitude of the sound as well.

## Cheats page ##
![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-5.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-5.png)

This page will allow the player to change money for players and adjust planetary population. Later on, several other cheat options will be available (as testing requires them).

## Logs page ##
![http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-6.png](http://darksideunderflow.com/images/resources.php?id=open-ig-setup-0.8-6.png)

This is a log viewer page, where any run time events and problems can be listed. Used for diagnosing bugs during the development phase.