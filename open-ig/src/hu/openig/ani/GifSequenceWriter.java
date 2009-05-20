/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ani;

//
//GifSequenceWriter.java
//
//Created by Elliot Kroo on 2009-04-25.
//
//This work is licensed under the Creative Commons Attribution 3.0 Unported
//License. To view a copy of this license, visit
//http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
//Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.

import hu.openig.ani.Framerates.Rates;
import hu.openig.ani.SpidyAniFile.Algorithm;
import hu.openig.ani.SpidyAniFile.Block;
import hu.openig.ani.SpidyAniFile.Data;
import hu.openig.ani.SpidyAniFile.Palette;
import hu.openig.ani.SpidyAniFile.Sound;
import hu.openig.compress.LZSS;
import hu.openig.compress.RLE;
import hu.openig.core.PaletteDecoder;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
 * @author Elliot Kroo
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
	 * @author Elliot Kroo (elliot[at]kroo[dot]net)
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
		} else {
			return iter.next();
		}
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
			ImageOutputStream output = new FileImageOutputStream(new File(
					args[1]));

			final SpidyAniFile saf = new SpidyAniFile();
			FileInputStream rf = new FileInputStream(args[0]);
			saf.open(rf);
			saf.load();

			Framerates fr = new Framerates();
			
			Rates r = fr.getRates(args[0], saf.getLanguageCode());
			double fps = r.fps;
			// create a gif sequence with the type of the first image, 1 second
			// between frames, which loops continuously
			GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, (int)(1000 / fps), false);

			PaletteDecoder palette = null;
			int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
			// clear any previous things from the buffer
			int imageHeight = 0;
			int dst = 0;
			int audioCount = 0;
			Algorithm alg = saf.getAlgorithm();

	   		double starttime = System.currentTimeMillis();  // notice the start time
	   		try {
				// add audio delay
		   		boolean firstframe = true;
		   		while (true) {
					Block b = saf.next();
					if (b instanceof Palette) {
						palette = (Palette)b;
					} else
					if (b instanceof Sound) {
						audioCount++;
					} else
					if (b instanceof Data) {
						if (firstframe) {
							starttime = System.currentTimeMillis();
							firstframe = false;
						}
						Data d = (Data)b;
						imageHeight += d.height;
						// decompress the image
						byte[] rleInput = d.data;
						if (saf.isLZSS() && !d.specialFrame) {
							rleInput = new byte[d.bufferSize];
							LZSS.decompress(d.data, 0, rleInput, 0);
						}
						switch (alg) {
						case RLE_TYPE_1:
							int newDst = RLE.decompress1(rleInput, 0, rawImage, dst, palette);
							dst = newDst;
							break;
						case RLE_TYPE_2:
							newDst = RLE.decompress2(rleInput, 0, rawImage, dst, palette);
							dst = newDst;
							break;
						default:
						}
						// we reached the number of subimages per frame?
						if (imageHeight >= saf.getHeight()) {
							BufferedImage img = new BufferedImage(saf.getWidth(), saf.getHeight(), BufferedImage.TYPE_INT_ARGB);
							img.setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
							writer.writeToSequence(img);
							imageHeight = 0;
							dst = 0;
							starttime += (1000.0 / fps);
//		           			LockSupport.parkNanos((long)(Math.max(0, starttime - System.currentTimeMillis()) * 1000000));
						}
					}
				}
			} catch (EOFException ex) {
			}

			writer.close();
			output.close();
		} else {
			System.out
					.println("Usage: java GifSequenceWriter anim-file-in gif-file-out");
		}
	}
}
