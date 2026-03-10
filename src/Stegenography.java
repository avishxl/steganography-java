// Stegenography.java
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Stegenography {

    private static final int MAX_INT_LEN = 4;
    private static final int DATA_SIZE = 8;   // number of image bytes required to store one stego byte

    static byte[] imBytes;
    static int msgLen;
    static String fnm;
    static String msg;

    public static boolean hide(String textFnm, String imFnm) {
        String inputText = readTextFile(textFnm);
        if ((inputText == null) || (inputText.length() == 0))
            return false;

        byte[] stego = buildStego(inputText);

        // access the image's data as a byte array
        BufferedImage im = loadImage(imFnm);
        if (im == null)
            return false;
        byte imBytesArr[] = accessBytes(im);

        if (!singleHide(imBytesArr, stego))
            return false;

        // store the modified image in <fnm>Msg.png
        String outBase = getFileName(imFnm);
        return writeImageToFile(outBase + "Msg.png", im);
    }  // end of hide()

    private static String readTextFile(String fnm) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            br = new BufferedReader(new FileReader(new File(fnm)));
            String text = null;
            while ((text = br.readLine()) != null)
                sb.append(text).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not completely read " + fnm);
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Problem closing " + fnm);
                return null;
            }
        }
        System.out.println("Read in " + fnm);
        return sb.toString();
    }  // end of readTextFile()

    private static byte[] buildStego(String inputText) {
        byte[] msgBytes = inputText.getBytes();
        byte[] lenBs = intToBytes(msgBytes.length);

        int totalLen = lenBs.length + msgBytes.length;
        byte[] stego = new byte[totalLen];

        System.arraycopy(lenBs, 0, stego, 0, lenBs.length);
        System.arraycopy(msgBytes, 0, stego, lenBs.length, msgBytes.length);

        return stego;
    }  // end of buildStego()

    private static byte[] intToBytes(int i) {
        byte[] integerBs = new byte[MAX_INT_LEN];
        integerBs[0] = (byte) ((i >>> 24) & 0xFF);
        integerBs[1] = (byte) ((i >>> 16) & 0xFF);
        integerBs[2] = (byte) ((i >>> 8) & 0xFF);
        integerBs[3] = (byte) (i & 0xFF);
        return integerBs;
    }  // end of intToBytes()

    private static BufferedImage loadImage(String imFnm) {
        BufferedImage im = null;
        try {
            im = ImageIO.read(new File(imFnm));
            System.out.println("Read " + imFnm);
            // Ensure image uses a byte-based raster (TYPE_3BYTE_BGR) so DataBufferByte cast works
            if (im.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                BufferedImage convertedImg = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                convertedImg.getGraphics().drawImage(im, 0, 0, null);
                im = convertedImg;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not read image from " + imFnm);
        }
        return im;
    }   // end of loadImage()

    private static byte[] accessBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        java.awt.image.DataBuffer buffer = raster.getDataBuffer();
        if (!(buffer instanceof DataBufferByte)) {
            // This should not happen because loadImage converts to TYPE_3BYTE_BGR,
            // but keep this safe-guard to avoid ClassCastException.
            BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(image, 0, 0, null);
            raster = convertedImg.getRaster();
            buffer = raster.getDataBuffer();
        }
        DataBufferByte dbb = (DataBufferByte) buffer;
        return dbb.getData();
    }  // end of accessBytes()

    private static boolean singleHide(byte[] imBytesArr, byte[] stego) {
        int imLen = imBytesArr.length;
        System.out.println("Byte length of image: " + imLen);

        int totalLen = stego.length;
        System.out.println("Total byte length of message (including length field): " + totalLen);

        if ((totalLen * DATA_SIZE) > imLen) {
            System.out.println("Image not big enough for message");
            return false;
        }

        hideStego(imBytesArr, stego, 0);
        return true;
    }  // end of singleHide()

    private static void hideStego(byte[] imBytesArr, byte[] stego, int offset) {
        for (int i = 0; i < stego.length; i++) {
            int byteVal = stego[i] & 0xFF;
            for (int j = 7; j >= 0; j--) {
                int bitVal = (byteVal >>> j) & 1;
                imBytesArr[offset] = (byte) ((imBytesArr[offset] & 0xFE) | bitVal);
                offset++;
            }
        }
    }  // end of hideStego()

    private static String getFileName(String fnm) {
        int extPosn = fnm.lastIndexOf('.');
        if (extPosn == -1) {
            System.out.println("No extension found for " + fnm);
            return fnm;
        }
        return fnm.substring(0, extPosn);
    }  // end of getFileName()

    private static boolean writeImageToFile(String outFnm, BufferedImage im) {
        try {
            ImageIO.write(im, "png", new File(outFnm));
            System.out.println("Image written to PNG file: " + outFnm);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not write image to " + outFnm);
            return false;
        }
    } // end of writeImageToFile();

    // --------------------------- reveal a message -----------------------------------

    public static String reveal(String imFnm) {
        BufferedImage im = loadImage(imFnm);
        if (im == null)
            return "Image inappropriate!!";
        imBytes = accessBytes(im);
        System.out.println("Byte length of image: " + imBytes.length);

        msgLen = getMsgLength(imBytes, 0);
        if (msgLen == -1)
            return "No Message Found";
        System.out.println("Byte length of message: " + msgLen);

        msg = getMessage(imBytes, msgLen, MAX_INT_LEN * DATA_SIZE);
        if (msg != null) {
            fnm = getFileName(imFnm);
            return msg;
        } else {
            System.out.println("No message found");
            return "No Message Found";
        }
    }  // end of reveal()

    public String save() {
        if (fnm == null || msg == null) {
            return "No message to save. Run reveal() first.";
        }
        return writeStringToFile(fnm + ".txt");
    }

    private static int getMsgLength(byte[] imBytesArr, int offset) {
        byte[] lenBytes = extractHiddenBytes(imBytesArr, MAX_INT_LEN, offset);
        if (lenBytes == null)
            return -1;

        int msgLenLocal = ((lenBytes[0] & 0xff) << 24) |
                ((lenBytes[1] & 0xff) << 16) |
                ((lenBytes[2] & 0xff) << 8) |
                (lenBytes[3] & 0xff);

        // validate that the message fits in remaining image bytes
        long required = (long) MAX_INT_LEN * DATA_SIZE + (long) msgLenLocal * DATA_SIZE;
        if ((msgLenLocal <= 0) || (required > imBytesArr.length)) {
            System.out.println("Incorrect message length or not enough space: msgLen=" + msgLenLocal + ", required=" + required + ", available=" + imBytesArr.length);
            return -1;
        }

        return msgLenLocal;
    }  // end of getMsgLength()

    private static String getMessage(byte[] imBytesArr, int msgLenLocal, int offset) {
        byte[] msgBytes = extractHiddenBytes(imBytesArr, msgLenLocal, offset);
        if (msgBytes == null)
            return null;

        String msgLocal = new String(msgBytes);
        if (isPrintable(msgLocal)) {
            return msgLocal;
        } else
            return null;
    }  // end of getMessage()

    private static byte[] extractHiddenBytes(byte[] imBytesArr, int size, int offset) {
        int finalPosn = offset + (size * DATA_SIZE);
        if (finalPosn > imBytesArr.length) {
            System.out.println("End of image reached");
            return null;
        }

        byte[] hiddenBytes = new byte[size];

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < DATA_SIZE; i++) {
                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 1) | (imBytesArr[offset] & 1));
                offset++;
            }
        }
        return hiddenBytes;
    }  // end of extractHiddenBytes()

    private static boolean isPrintable(String str) {
        for (int i = 0; i < str.length(); i++)
            if (!isPrintable(str.charAt(i))) {
                System.out.println("Unprintable character found");
                return false;
            }
        return true;
    }  // end of isPrintable()

    private static boolean isPrintable(int ch) {
        if (Character.isWhitespace(ch) && (ch < 127))
            return true;
        else if ((ch > 32) && (ch < 127))
            return true;
        return false;
    }  // end of isPrintable()

    private static String writeStringToFile(String outFnm) {
        try {
            try (FileWriter out = new FileWriter(new File(outFnm))) {
                out.write(msg);
            }
            return outFnm;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not write message to " + outFnm);
        }
        return "Something went wrong!!";
    }  // end of writeStringToFile()

}  // end of Stegenography class
