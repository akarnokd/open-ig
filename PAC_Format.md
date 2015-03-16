# Introduction #

This page describes the package file format used by the original Imperium Galactica. The `hu.openig.utils.PACFile` class contains the routines to process a `.PAC` file.

# Details #

The file starts with a word, which specifies the number of entries (N) in the file.

Following N x 20 bytes contains the directory listing for each internal file, size and offsets within the file:

| **Offset** | **Description** |
|:-----------|:----------------|
| 00 | The length of the following file name, up to 13 bytes |
| 01 - 0D | The file name, unused bytes contain the dot (0x2E) characters |
| 0E - 0F | The length of the file |
| 10 - 13 | The absolute start offset within the file |

Unfortunately, this file format does not allow storing files larger than 64KB. Therefore, lot of the building images are stored splitted into two separated files within a package.
The file format allows the file data to be non-contignous or not in the same order as the directory entries. The file is in little endian format.