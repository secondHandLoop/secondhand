package com.secondhand.note.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
 * Downscales and re-encodes images to compact JPEGs. Used both to shrink stored
 * listing photos (so they load fast from object storage) and to minimize the
 * pixels sent to the vision model.
 *
 * <p>Re-encoding drops the source EXIF metadata, so the original orientation tag
 * would be lost and portrait phone photos would display sideways. This reads the
 * JPEG EXIF orientation and bakes the rotation/flip into the pixels before writing,
 * so the output needs no orientation tag to render correctly.
 *
 * <p>Every failure path returns the original bytes untouched, so a decode/encode
 * problem never breaks an upload — it just stores the image uncompressed. Callers
 * can also pass a size budget so already-small images (e.g. ones a client already
 * compressed) pass through without a needless second lossy re-encode.
 */
@Component
public class ImageCompressor {

    private static final Logger log = LoggerFactory.getLogger(ImageCompressor.class);

    /**
     * The outcome of a compression attempt: either freshly encoded JPEG bytes, or
     * the original bytes when the image could not be processed.
     */
    public static final class Result {
        private final byte[] bytes;
        private final boolean recompressed;

        private Result(byte[] bytes, boolean recompressed) {
            this.bytes = bytes;
            this.recompressed = recompressed;
        }

        /** @return the image bytes to store or send */
        public byte[] bytes() {
            return bytes;
        }

        /** @return true if {@link #bytes()} is a freshly encoded JPEG, false if it is the untouched original */
        public boolean recompressed() {
            return recompressed;
        }
    }

    /**
     * Downscales an image to at most {@code maxDim} on its longest side, applies its
     * EXIF orientation, and re-encodes it as a JPEG at the given quality.
     *
     * @param input            the original image bytes
     * @param maxDim           the maximum length, in pixels, of the longest side
     * @param quality          the JPEG quality, 0.0 (smallest) to 1.0 (best)
     * @param skipIfUnderBytes if positive, images already within {@code maxDim} and no
     *                         larger than this many bytes are returned untouched; pass 0
     *                         to always re-encode
     * @return the compressed JPEG, or the original bytes if the image cannot be processed
     */
    public Result compressToJpeg(byte[] input, int maxDim, float quality, long skipIfUnderBytes) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
            if (src == null) {
                return new Result(input, false);
            }
            // Guardrail no-op: an already-small image (e.g. compressed by the client)
            // is stored as-is rather than re-encoded a second time.
            if (skipIfUnderBytes > 0
                    && Math.max(src.getWidth(), src.getHeight()) <= maxDim
                    && input.length <= skipIfUnderBytes) {
                return new Result(input, false);
            }
            int orientation = readExifOrientation(input);
            BufferedImage scaled = scale(src, maxDim);
            BufferedImage oriented = applyOrientation(scaled, orientation);
            byte[] jpeg = writeJpeg(oriented, quality);
            return jpeg.length > 0 ? new Result(jpeg, true) : new Result(input, false);
        } catch (Exception ex) {
            log.warn("Image compress failed, using original: {}", ex.getMessage());
            return new Result(input, false);
        }
    }

    /** Draws the source onto an opaque RGB canvas scaled to fit {@code maxDim} on its longest side. */
    private BufferedImage scale(BufferedImage src, int maxDim) {
        int w = src.getWidth();
        int h = src.getHeight();
        int longest = Math.max(w, h);
        double factor = longest > maxDim ? (double) maxDim / longest : 1.0;
        int nw = Math.max(1, (int) Math.round(w * factor));
        int nh = Math.max(1, (int) Math.round(h * factor));
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, Color.WHITE, null);
        g.dispose();
        return dst;
    }

    /**
     * Bakes the EXIF orientation into the pixels, swapping width/height for the four
     * quarter-turn cases. Returns the input unchanged for the identity orientation.
     *
     * @param img         the (already scaled, opaque RGB) image
     * @param orientation the EXIF orientation value, 1-8
     * @return an upright image
     */
    private BufferedImage applyOrientation(BufferedImage img, int orientation) {
        if (orientation <= 1) {
            return img;
        }
        int w = img.getWidth();
        int h = img.getHeight();
        boolean swap = orientation >= 5;
        BufferedImage dst = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        AffineTransform tf = new AffineTransform();
        switch (orientation) {
            case 2: // mirror horizontal
                tf.translate(w, 0);
                tf.scale(-1, 1);
                break;
            case 3: // rotate 180
                tf.translate(w, h);
                tf.rotate(Math.PI);
                break;
            case 4: // mirror vertical
                tf.translate(0, h);
                tf.scale(1, -1);
                break;
            case 5: // transpose (reflect across main diagonal)
                tf = new AffineTransform(0, 1, 1, 0, 0, 0);
                break;
            case 6: // rotate 90 CW
                tf.translate(h, 0);
                tf.rotate(Math.PI / 2);
                break;
            case 7: // transverse (reflect across anti-diagonal)
                tf = new AffineTransform(0, -1, -1, 0, h, w);
                break;
            case 8: // rotate 90 CCW
                tf.translate(0, w);
                tf.rotate(-Math.PI / 2);
                break;
            default:
                g.dispose();
                return img;
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, tf, null);
        g.dispose();
        return dst;
    }

    /** Encodes an opaque image as JPEG at the given quality. */
    private byte[] writeJpeg(BufferedImage img, float quality) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            return new byte[0];
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Math.max(0f, Math.min(1f, quality)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    /**
     * Reads the EXIF orientation tag (0x0112) from JPEG bytes.
     *
     * @param data the image bytes
     * @return the orientation value 1-8, or 1 when absent or the image is not a parseable JPEG
     */
    private int readExifOrientation(byte[] data) {
        try {
            if (data == null || data.length < 4 || (data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
                return 1;
            }
            int offset = 2;
            while (offset + 4 <= data.length) {
                if ((data[offset] & 0xFF) != 0xFF) {
                    offset++;
                    continue;
                }
                int marker = data[offset + 1] & 0xFF;
                // Standalone markers (SOI/EOI/RSTn) carry no length field.
                if (marker == 0xD8 || marker == 0xD9 || (marker >= 0xD0 && marker <= 0xD7)) {
                    offset += 2;
                    continue;
                }
                int segLen = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
                if (segLen < 2) {
                    return 1;
                }
                int segStart = offset + 4;
                // APP1 segment beginning with "Exif\0\0" holds the TIFF header + IFD.
                if (marker == 0xE1 && segStart + 6 <= data.length
                        && data[segStart] == 'E' && data[segStart + 1] == 'x'
                        && data[segStart + 2] == 'i' && data[segStart + 3] == 'f'
                        && data[segStart + 4] == 0 && data[segStart + 5] == 0) {
                    return parseExifOrientation(data, segStart + 6, Math.min(data.length, offset + 2 + segLen));
                }
                if (marker == 0xDA) {
                    // Start of scan — image data follows, no more metadata.
                    return 1;
                }
                offset += 2 + segLen;
            }
        } catch (Exception ignore) {
            // Any malformed structure just means "no known orientation".
        }
        return 1;
    }

    /** Parses the orientation tag out of a TIFF/EXIF block starting at {@code tiffStart}. */
    private int parseExifOrientation(byte[] d, int tiffStart, int end) {
        if (tiffStart + 8 > end) {
            return 1;
        }
        int b0 = d[tiffStart] & 0xFF;
        int b1 = d[tiffStart + 1] & 0xFF;
        boolean little;
        if (b0 == 0x49 && b1 == 0x49) {
            little = true;
        } else if (b0 == 0x4D && b1 == 0x4D) {
            little = false;
        } else {
            return 1;
        }
        int ifdOffset = readInt(d, tiffStart + 4, little);
        int ifd = tiffStart + ifdOffset;
        if (ifd + 2 > end) {
            return 1;
        }
        int count = readShort(d, ifd, little);
        int entry = ifd + 2;
        for (int i = 0; i < count; i++, entry += 12) {
            if (entry + 12 > end) {
                break;
            }
            int tag = readShort(d, entry, little);
            if (tag == 0x0112) {
                int value = readShort(d, entry + 8, little);
                return (value >= 1 && value <= 8) ? value : 1;
            }
        }
        return 1;
    }

    private int readShort(byte[] d, int at, boolean little) {
        int a = d[at] & 0xFF;
        int b = d[at + 1] & 0xFF;
        return little ? (b << 8) | a : (a << 8) | b;
    }

    private int readInt(byte[] d, int at, boolean little) {
        int a = d[at] & 0xFF;
        int b = d[at + 1] & 0xFF;
        int c = d[at + 2] & 0xFF;
        int e = d[at + 3] & 0xFF;
        return little
                ? (e << 24) | (c << 16) | (b << 8) | a
                : (a << 24) | (b << 16) | (c << 8) | e;
    }
}
