# Introduction #

I created a special image format to achieve better lossless compression on the Imperium Galactica files than a PNG or PCX type compression would.

The idea is simple: try different encoding and compression method and keep the best one:

  * Palette or without palette: replace colors with index, and use a common palette. The palette should contain only the colors for the used indexes, not the entire possible colors.
  * compute effective bit per pixel ratio (e.g 1 BPP, 12 BPP) and store the indexed images on bits
  * for <9 BPP, try keeping the index size on 8BPP to allow a further compression to better capture patterns
  * try storing the image az raw 24bit (normal or transparent) or 32bit (= alpha channel) as  for small images, the palette might take up too much space in respect to the image data
  * try keeping it uncompressed (=no compression format overhead), GZIP-ped or ZIPped (maximum compression)

The preliminary results show, that recompressing 2089 PNG images results:

Uncompressed total image size: 149,639,816 bytes<br>
PNG compressed (mixed 8BPP and 32BPP): 13,724,378 bytes (9.17%)<br>
IMG_2009 compressed: 10,361,145 bytes(6.92%, 75.49% relative to the PNG)<br>
<br>
However, the format itself is rather slower to compress and decompress, as the IO operation is bit sized. It is possible to speed it up when everything is 8, 16, 24 or 32 BPP.<br>
<br>
<h1>Format specification</h1>

The first whole byte (as a character) is an indicator for compression:<br>
<br>
<table><thead><th> <b>Value</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> 'R' </td><td> The image is in raw format - no compression </td></tr>
<tr><td> 'G' </td><td> The image is GZIP compressed </td></tr>
<tr><td> 'Z' </td><td> The image is ZIP compressed. The ZIP contains only a raw.img file entry. </td></tr></tbody></table>

The following bytes need to be interpreted according to this (e.g. start reading the file as a compressed stream)<br>
<br>
The bit operations use least significant to most significant order, e.g. bit 0 of the input goes to bit 0 of the output byte. For multibyte data, this is equivalent to the little endian format.<br>
<br>
<table><thead><th> <b>Bit Offset</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> 0 </td><td> Transparency indicator </td></tr>
<tr><td> 1 </td><td> Alpha channel indicator: the image contains not only transparency, but more alpha values than 0 or 0xFF. If this bit is set </td></tr>
<tr><td> 2 </td><td> The image contains palette </td></tr>
<tr><td> 3 - 7 </td><td> BPP: The bits per pixel - 1 (+ end of first byte) </td></tr>
<tr><td> 8 - 19 </td><td> W: The image width </td></tr>
<tr><td> 20 - 31 </td><td> H: The image height (+ end of 4th byte) </td></tr></tbody></table>

Based on the indicators, the following bytes can be different<br>
<br>
<h2>No palette is used</h2>
<h3>Transparent but no other alpha</h3>
If the image is transparent, but has no other alpha values, the following 24 bits contain the RGB color, that is used to indicate a transparent pixel.<br>
<br>
After that, W x H number of 24 bits of RGB color bytes are stored as the pixels.<br>
<br>
<h3>No transparency</h3>
If the alpha bit is zero, then W x Hnumber of 24 bits of RGB colors follow. If the alpha bit is one, then W x H number of 32 bits of ARGB colors follow.<br>
<br>
<h2>Palette is used</h2>
Remark: if the image has transparency and no other alpha, then the transparent index is the number of palette entries plus one and is never stored in the palette.<br>
<br>
The following BPP bits contain the number of palette entries (PE) - 1.<br>
<br>
If the image has more alpha, the following PE number of 32 bit entries contains the ARGB color.<br>
If the image has no other alpha, the following PE number of 24 bit entries contains the RGB color.<br>
<br>
After the palette comes the image bits in BPP sized chunks. If there only transparency, the transparent index equals PE.<br>
<br>
<h1>Effectiveness</h1>

I tested the compression scheme on the current generic+hungarian images of 35MB of PNG (about 250MB of uncompressed RGBA). The scheme was able to reduce the image sizes to 28MB (~78% of original size). As there are smaller images than audio or video, I still haven't decided to transcode every image, as it might make hard to alter the resources further on - a price high for a 7 MB less download.