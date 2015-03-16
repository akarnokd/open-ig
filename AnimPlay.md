# Introduction #

My Open Imperium Galactica project started with the test whether I can come up with a decoder to play the original game's <a href='http://code.google.com/p/open-ig/wiki/ANI_Format'>ANI files</a>. I succeded, with the great help from TCH.

# Details #

When you start the application, it opens an empty playback window along with a playlist filled in. In the main window, you can use `File > Open` or `A` to open an animation file, `S` to stop the current playback and `D` to replay the current stopped playback.

The program allows saving the video as Animated GIF, series of PNG images and a WAV file. (Unfortunately, I had no time to implement a direct AVI output, but they tend to be very large: the BLOCK23.ANI decodes into a 1GB RAW AVI file!)

In the new View menu, you can now set the scaling type to

  * NONE: plays on the original size
  * FIT WINDOW: scales to the current player window
  * KEEP ASPECT: scales to the current player window, but the picture retains its aspect ratio

<img src='http://karnokd.uw.hu/images/open-ig-animplay.png'>

With version 0.5 I added a playlist window too, which can search any ANI files from the given directory. Note when you start the application, this window opens and starts scanning the files in the current directory and below. If you put the app into a root directory, it could take very long time before it finishes, and probably you can only kill the application (my mistake: haven't implemented a stop functionality to that.)<br>
<br>
You can use the <code>SPACE</code> or <code>ENTER</code> keys to start playing the selected video (note, that <code>ENTER</code> behaves a bit excellish as it moves the selection to the next line and that entry will be played). You can use <code>S</code> to stop the current playback or <code>D</code> to replay the last playback.<br>
<br>
This playlist window allows the user to select multiple entries and start a combined PNG &amp; WAV extraction - this way you can convert videos in batch mode. You can specify the output target directory and whether the files should be placed in their respectively named subdirectories.<br>
<br>
<img src='http://karnokd.uw.hu/images/open-ig-animplaylist.png'>