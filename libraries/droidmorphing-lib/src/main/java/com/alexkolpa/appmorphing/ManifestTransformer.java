package com.alexkolpa.appmorphing;

import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.SAXException;

/**
 * Solution of decompressing by: http://stackoverflow.com/a/4761689 Created by
 * @version 2-4-2014
 * @author Alex Kolpa
 * @version 21-4-2014
 * @author Joris
 */
public class ManifestTransformer {
    private static int endDocTag = 0x00100101;
    private static int startTag = 0x00100102;
    private static int endTag = 0x00100103;

    public static void writeLabel(File manifest, String label) throws
            IOException,
            ParserConfigurationException,
            SAXException {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        byte[] xml = null;
        try {
            input = new BufferedInputStream(new FileInputStream(manifest));
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            xml = output.toByteArray();
        }
        finally {
            if(input != null) {
                input.close();
            }
            if(output != null) {
                output.close();
            }
        }

        if(xml == null || xml.length == 0) {
            throw new IOException("Unable to read manifest file!");
        }

        int numbStrings = LEW(xml, 4 * 4);

        int sitOff = 0x24;
        int stOff = sitOff + numbStrings * 4;

        int xmlTagOff = LEW(xml, 3 * 4);

        for (int ii = xmlTagOff; ii < xml.length - 4; ii += 4) {
            if (LEW(xml, ii) == startTag) {
                xmlTagOff = ii;
                break;
            }
        }

        int off = xmlTagOff;
        while (off < xml.length) {
            int tag0 = LEW(xml, off);
            int nameSi = LEW(xml, off + 5 * 4);

            if (tag0 == startTag) {
                int numbAttrs = LEW(xml, off + 7 * 4);
                off += 9 * 4;
                String name = compXmlString(xml, sitOff, stOff, nameSi);

                // Look for the Attributes
                for (int ii = 0; ii < numbAttrs; ii++) {
                    int attrNameSi = LEW(xml, off + 1 * 4);
                    int attrValueSi = LEW(xml, off + 2 * 4);
                    off += 5 * 4;

                    String attrName = compXmlString(xml, sitOff, stOff,
                            attrNameSi);

                    if (name.equals("application") && attrName.equals("label")) {
                        insertXmlString(label, xml, sitOff, stOff, attrValueSi);
                    }
                }

            } else if (tag0 == endTag) {
                off += 6 * 4;
                String name = compXmlString(xml, sitOff, stOff, nameSi);
            } else if (tag0 == endDocTag) {
                break;

            } else {
                break;
            }
        }

        OutputStream fOutput = null;
        try {
            fOutput = new FileOutputStream(manifest);
            fOutput.write(xml);
        }
        finally {
            if(fOutput != null) {
                fOutput.close();
            }
        }
    }

    private static String compXmlString(byte[] xml, int sitOff, int stOff, int strInd) {
        if (strInd < 0) {
            return null;
        }
        int strOff = stOff + LEW(xml, sitOff + strInd * 4);
        return compXmlStringAt(xml, strOff);
    }

    // compXmlStringAt -- Return the string stored in StringTable format at
    // offset strOff. This offset points to the 16 bit string length, which
    // is followed by that number of 16 bit (Unicode) chars.
    private static String compXmlStringAt(byte[] arr, int strOff) {
        int strLen = arr[strOff + 1] << 8 & 0xff00 | arr[strOff] & 0xff;
        byte[] chars = new byte[strLen];
        for (int ii = 0; ii < strLen; ii++) {
            chars[ii] = arr[strOff + 2 + ii * 2];
        }
        return new String(chars); // Hack, just use 8 byte chars
    } // end of compXmlStringAt

    private static void insertXmlString(String input, byte[] xml, int sitOff, int stOff, int strInd)
            throws UnsupportedEncodingException {
        if (strInd < 0) {
            return;
        }
        int strOff = stOff + LEW(xml, sitOff + strInd * 4);
        insertXmlString(input, xml, strOff);
    }

    //Makes use of compXmlStringAt's code to find the right location in the byte array and insert the new value.
    private static void insertXmlString(String input, byte[] arr, int strOff)
            throws UnsupportedEncodingException {
        int strLen = arr[strOff + 1] << 8 & 0xff00 | arr[strOff] & 0xff;
        if (input.length() < strLen) {
            int add = strLen - input.length();
            for (int j = 0; j < add; j++) {
                input += " ";
            }
        }

        byte[] inputChars = new byte[0];

        inputChars = input.getBytes("ISO-8859-1");
        for (int ii = 0; ii < strLen; ii++) {
            arr[strOff + 2 + ii * 2] = inputChars[ii];
        }

    } // end of compXmlStringAt

    // LEW -- Return value of a Little Endian 32 bit word from the byte array
    // at offset off.
    private static int LEW(byte[] arr, int off) {
        return arr[off + 3] << 24 & 0xff000000 | arr[off + 2] << 16 & 0xff0000
                | arr[off + 1] << 8 & 0xff00 | arr[off] & 0xFF;
    } // end of LEW
}
