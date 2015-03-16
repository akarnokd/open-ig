# Introduction #

Similar to the [ANI\_Format](ANI_Format.md) described, but has a slightly different format along with a different compression scheme, resulting in a 10-20% smaller video files. It was designed to cope with the properties of the original video files: less than 256 colors per image.

# Details #

The new file format is as follows:

| **Offset** | **Length** | **Description** |
|:-----------|:-----------|:----------------|
| 0x0000 | int32 | The image width (W). Little endian. |
| 0x0004 | int32 | The image height (H). Little endian. |
| 0x0008 | int32 | Number of frames (N). Little endian. |
| 0x000C | int32 | Frames per second `*` 1000. Little endian. |

Starting from offset `0x0010` multiple sections are possible. Each section starts with an indicator byte, then a section specific data and length

  * `P` indicates a palette section: following byte (M) indicates the number of byte sized R, G, B triplets following.
  * `A` indicates an audio section: the following int32 little endian indicates the number of raw bytes of the sound, possible format is 22050/8/1 unsigned
  * `I` image section, contains W `*` H bytes of 256 color image.
  * `X` end of file indicator, no more bytes

The format is not final, as there are considerations going on to compress the audio data separately using OGG or simple differential scheme.

As it is obvious, the format does not contain any compression - the audio and video data is fully uncompressed. However, the image data is specially encoded:

If a pixel in the current frame is the same as the pixel in the previous frame, the color index of 255 is used, instead of the concrete color index. This creates a huge patch of 255s on each frame, which can be nicely compressed by any means. If the original image contains 256 different colors, it might be possible to change the palette a bit or find a close replacement color, therefore index 255 can be used for transparency indication.

The raw data can be then freely GZIP-ped or ZIP-ped with great efficiency: around 7% of the uncompressed video data and about 10% smaller than the original ANI file.