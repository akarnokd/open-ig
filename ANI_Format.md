
# Introduction #

This page describes the file format used for animations in the original Imperium Galactica.

# Details #

The file consists a header and several types of sub-blocks. General rule is that WORDs are stored in little endian format (least significant byte first, most significant byte last). The file can be processed using the `hu.openig.ani.SpidyAniFile` class. The animation file contains 8 bit images.

## Header ##

The header is 19 bytes long and has the following byte structure.

| **Offset (hex)** | **Description** |
|:-----------------|:----------------|
| `00-07` | The string `"SpidyAni"` in ISO-8859-1|
| `08   ` | The file version character. Currently the version `"2"` is accepted by the player|
| `09-0A` | The animation flags (see below)|
| `0B-0C` | Number of full frames in the file. Due to the 64KB limit of the DOS program, a frame must be divided into sub blocks (for example, an 640x480 animation consists of 5 subsequent image blocks)|
| `0D-0E` | The width of the animation |
| `0F-10` | The height of the animation |
| `11-12` | Language: 1-English, 2-Hungarian. |

### The structure of the animation flags ###
(Note: bit 0 is the least significant bit)

| **Offset (hex)** | **Description** |
|:-----------------|:----------------|
|`bit 0` | The image data is compressed (probably). The player requires this to be 1. |
|`bit 2` | The image is encoded using 0: the first [RLE algorithm](Custom_RLE.md); 1: the second RLE algorithm |
|`bit 5` | The encoded image is further encoded by an [LZSS](Custom_LZSS.md) algorithm.|

The meaning of the other bits are unknown, probably reserved.

## Palette block ##
After the header there is usually one palette block which describes the actual RGB colors of varios pixels. The palette is a 6-6-6 bit RGB component. To obtain the usable values, each component value must be multiplied by 4. The block contains 256 palette entries which makes it 4 + 768 bytes long

| **Offset (hex)** | **Description** |
|:-----------------|:----------------|
|`000-003` | The block marker string `"Pal "` (space at the end). |
|`004`| The 0. color's Red component |
|`005`| The 0. color's Green component |
|`006`| The 0. color's Blue component |
|...|...|
|{{{303}}| The 255. color's Blue component |

It is not known whether an ANI file could contain multiple palette blocks or. The player automatically uses the new palette if it encounters one.

## Sound block ##
Sound blocks contain a fixed amount of sound data in a 22050Hz, 8 bit, 1 channel signed PCM format. The sound blocks are 4 + 1270 bytes long.

| **Offset (hex)** | **Description** |
|:-----------------|:----------------|
| `000-003` | The block marker string `"Hang"` (which means sound in hungarian) |
| `004-04F9` | The raw audio data |

## Image data block ##

The image block's data could be decoded in various ways. The header's flags field helps in this. The image data block has its own subheader whose format varies according to the LZSS status flag. After the header is the actual compressed image content. The uncompressed image size can be calculated from `width * height` in bytes.

### If the LZSS status flag is zero (09:5) ###
| **Offset (hex)** | Description |
|:-----------------|:------------|
| `00-03` | The block marker string `"Data"`. |
| `04-05` | Word, the size of the data block |
| `06-07` | Word, the image fragment's width (usually equals to the main width) |
| `08-09` | Word, the image fragment's height (might be less than the main height)|

### If the LZSS status flag is one (09:5) ###
| **Offset (hex)** | Description |
|:-----------------|:------------|
| `00-03` | The block marker string `"Data"`. |
| `04-05` | Word, the buffer size for the LZSS decompressor's output |
| `06-07` | Word, the size of the contents (see notes below) |
| `08-09` | Word, the image fragment's width (usually equals to the main width) |
| `0A-0B` | Word, the image fragment's height (might be less than the main height)|

If the data block size (bytes 06-07) is FFFF, it is an indication, that the block does not need to be LZSS decompressed. In this case, the actual content size is stored in bytes 04-05. Image decompressors should be aware of this kind of change.

### Decompressing the image ###
The decompression of the images could be done in the following steps:

  * If the LZSS animation flag is set and the current block did not contain the special FFFF size marker, decompress the data using the [LZSS](Custom_LZSS.md) algorithm into a temporary buffer and pass it to the following step instead of the original data
  * If the algorithm animation flag is zero, use the first [RLE](Custom_RLE.md) algorithm to decompress the actual image
  * If the algorithm animation flag is one, use the second [RLE](Custom_RLE.md) algorithm to decompress the actual image