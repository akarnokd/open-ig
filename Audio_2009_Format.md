# Introduction #

This page describes the modernized audio format.


# Details #

It is basically a RAW 22050Hz 8bit unsigned mono file, but differential coded (the numerical difference between the amplitudes are stored). This lossless scheme allows the file to be compressed down to ~40% of its original size, whereas a normal WAV+ZIP would result in ~50% size.

In the scheme, the minus first byte is considered to be (e.g the zeroth byte is the original byte).

The format can be decompressed and decoded in a streaming way.