# Introduction #

This page details the manual installation instructions for Open Imperium Galactica.

**I strongly recommend using the Launcher application which will take care about all the file downloads and the proper startup parametrization of the game.**

<a href='https://open-ig.googlecode.com/files/open-ig-launcher.jar'><img src='http://www.gstatic.com/codesite/ph/images/dl_arrow.gif' />  Installer</a>**(version 0.44)**

# Details #

1) Download the current upgrade pack and main game JAR file from the downloads page:

[Upgrade 2014-12-17-a-2](https://googledrive.com/host/0B4T7ZW3brESKeU1jWnpqMGVnZ0k/open-ig-upgrade-20141217a2.zip)

[open-ig-0.95.203.jar](https://googledrive.com/host/0B4T7ZW3brESKeU1jWnpqMGVnZ0k/open-ig-0.95.203.jar)

2) Download the game resource files (1.3 GB!):


[Audio 1](http://open-ig.googlecode.com/files/open-ig-audio-hu-20120420A.zip), [Audio 2](http://open-ig.googlecode.com/files/open-ig-audio-en-20120623A.zip), [Audio 3](http://open-ig.googlecode.com/files/open-ig-audio-de-20120420A.zip), [Audio 4](http://open-ig.googlecode.com/files/open-ig-audio-generic-20121121A.zip), [Audio 5](http://open-ig.googlecode.com/files/open-ig-audio-fr-20121022.zip),
[Audio 6](http://open-ig.googlecode.com/files/open-ig-audio-ru-20121022.zip)
<br><br>
<a href='http://open-ig.googlecode.com/svn/trunk/open-ig/open-ig-splash.png'>Splash</a>, <a href='https://googledrive.com/host/0B4T7ZW3brESKeU1jWnpqMGVnZ0k/open-ig-images-20140910a.zip'>Images</a><br><br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-01.zip'>Video 1</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-02.zip'>Video 2</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-03-0.8.zip'>Video 3</a>, <br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-04-0.8.zip'>Video 4</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-05-0.8.zip'>Video 5</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-06-0.8.zip'>Video 6</a>,<br>
<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-07-0.8.zip'>Video 7</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-08-0.8.zip'>Video 8</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-09-0.8.zip'>Video 9</a>, <br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-10-0.8.zip'>Video 10</a>,<a href='http://open-ig.googlecode.com/files/open-ig-video-hu-0.8.zip'>Video 11</a>,  <a href='http://open-ig.googlecode.com/files/open-ig-video-en-20120423A.zip'>Video 12</a>
<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-de-20120420A.zip'>Video 13</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-fr-20121022.zip'>Video 14</a>,<br>
<a href='http://open-ig.googlecode.com/files/open-ig-video-ru-20121022.zip'>Video 15</a>


3) Place all these files into the same directory<br>
<br>
4) Run the game:<br>
<br>
<ul><li>double click on <code>open-ig-0.95.203.jar</code>, or<br>
</li><li>run the following command line:</li></ul>

<pre><code>java -Xmx832M -jar open-ig-0.95.203.jar -memonce<br>
</code></pre>

<ul><li>create a shell script/batch file and put in:</li></ul>

<pre><code>javaw -Xmx832M -jar open-ig-0.95.203.jar -memonce<br>
</code></pre>