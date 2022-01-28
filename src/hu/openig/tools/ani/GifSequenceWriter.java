/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.tools.ani;

//
//GifSequenceWriter.java
//
//Created by Elliot Kroo on 2009-04-25.
//
//This work is licensed under the Creative Commons Attribution 3.0 Unported
//License. To view a copy of this license, visit
//http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
//Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.

import hu.openig.tools.ani.SpidyAniDecoder.SpidyAniCallback;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * This class will generate an animated GIF from a sequence of individual images.
 * @author Elliot Kroo (elliot[at]kroo[dot]net)
 */
public class GifSequenceWriter {
    /** The actual image writer. */
    protected ImageWriter gifWriter;
    /** The parameters. */
    protected ImageWriteParam imageWriteParam;
    /** The metadata. */
    protected IIOMetadata imageMetaData;

    /**
     * Creates a new GifSequenceWriter.
     *

     * @param outputStream
     *            the ImageOutputStream to be written to
     * @param imageType
     *            one of the imageTypes specified in BufferedImage
     * @param timeBetweenFramesMS
     *            the time between frames in miliseconds
     * @param loopContinuously
     *            wether the gif should loop repeatedly
     * @throws IOException if the underlying stream throws it or if no gif ImageWriters are found
     */
    public GifSequenceWriter(ImageOutputStream outputStream, int imageType,
            int timeBetweenFramesMS, boolean loopContinuously)
            throws IOException {
        // my method to create a writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier
                .createFromBufferedImageType(imageType);

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) imageMetaData
                .getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root,
                "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag",
                "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer
                .toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");

        IIOMetadataNode appEntensionsNode = getNode(root,
                "ApplicationExtensions");

        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = loopContinuously ? 0 : 1;

        child.setUserObject(new byte[] { 0x1, (byte) (loop & 0xFF),
                (byte) ((loop >> 8) & 0xFF) });
        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        gifWriter.setOutput(outputStream);

        gifWriter.prepareWriteSequence(null);
    }
    /**
     * Write the image sequence.
     * @param img the image to write
     * @throws IOException in an I/O error occurs
     */
    public void writeToSequence(RenderedImage img) throws IOException {
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData),
                imageWriteParam);
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    /**
     * Returns the first available GIF ImageWriter using
     * ImageIO.getImageWritersBySuffix("gif").
     *

     * @return a GIF ImageWriter object
     * @throws IIOException
     *             if no GIF image writers are returned
     */
    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        }
        return iter.next();
    }

    /**
     * Returns an existing child node, or creates and returns a new child node
     * (if the requested node does not exist).
     *

     * @param rootNode
     *            the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName
     *            the name of the child node.
     *

     * @return the child node, if found or a new node created with the given
     *         name.
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode,
            String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }
    /**
     * Sample program.
     * @param args arguments last argument the target filename
     * @throws Exception ex
     */

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            // create a new BufferedOutputStream with the last argument
            final String filename = args[0];
            final String outfile = args[1];
            transcodeToGif(filename, outfile, null);
        } else {
            System.out
                    .println("Usage: java GifSequenceWriter anim-file-in gif-file-out");
        }
    }
    /**
     * Transcodes the given input ANI file into the given output GIF file.
     * @param filename the input filename
     * @param outfile the output filename
     * @param progress the progress callback or null
     */
    public static void transcodeToGif(final String filename, final String outfile, final ProgressCallback progress) {
        SpidyAniCallback callback = new SpidyAniCallback() {
            /** The output image. */
            private FileImageOutputStream output;
            /** The gif sequencer. */
            private GifSequenceWriter writer;
            /** The image width. */
            private int width;
            /** The image height. */
            private int height;
            /** The working frame buffer. */
            private BufferedImage img;
            /** The current frame index. */
            private int frameCount;
            /** The maximum frame count. */
            private int maxFrameCount;
            @Override
            public void audioData(byte[] data) {
                // ignored
            }

            @Override
            public void fatal(Throwable t) {
                t.printStackTrace();
                stopped();
            }

            @Override
            public void finished() {
                try {
                    writer.close();
                    output.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public String getFileName() {
                return filename;
            }

            @Override
            public InputStream getNewInputStream() {
                try {
                    return new FileInputStream(filename);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void imageData(int[] image) {
                if (progress != null) {
                    progress.progress(frameCount, maxFrameCount);
                }
                img.setRGB(0, 0, width, height, image, 0, width);
                frameCount++;
                try {
                    writer.writeToSequence(img);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void initialize(int width, int height, int frames,
                    int languageCode, double fps, int audioDelay) {
                this.width = width;
                this.height = height;
                this.maxFrameCount = frames;
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                try {
                    output = new FileImageOutputStream(new File(outfile));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, (int)(1000 / fps), false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public boolean isPaused() {
                return false;
            }

            @Override
            public boolean isStopped() {
                return progress != null && progress.cancel();
            }

            @Override
            public void stopped() {
                finished();
            }

        };
        SpidyAniDecoder.decodeLoop(callback);
    }
}
