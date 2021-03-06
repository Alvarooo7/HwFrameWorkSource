package com.android.org.bouncycastle.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Vector;

public final class Strings {
    private static String LINE_SEPARATOR;

    private static class StringListImpl extends ArrayList<String> implements StringList {
        private StringListImpl() {
        }

        /* synthetic */ StringListImpl(AnonymousClass1 x0) {
            this();
        }

        public /* bridge */ /* synthetic */ String get(int x0) {
            return (String) super.get(x0);
        }

        public boolean add(String s) {
            return super.add(s);
        }

        public String set(int index, String element) {
            return (String) super.set(index, element);
        }

        public void add(int index, String element) {
            super.add(index, element);
        }

        public String[] toStringArray() {
            String[] strs = new String[size()];
            for (int i = 0; i != strs.length; i++) {
                strs[i] = (String) get(i);
            }
            return strs;
        }

        public String[] toStringArray(int from, int to) {
            String[] strs = new String[(to - from)];
            int i = from;
            while (i != size() && i != to) {
                strs[i - from] = (String) get(i);
                i++;
            }
            return strs;
        }
    }

    static {
        try {
            LINE_SEPARATOR = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("line.separator");
                }
            });
        } catch (Exception e) {
            try {
                LINE_SEPARATOR = String.format("%n", new Object[0]);
            } catch (Exception e2) {
                LINE_SEPARATOR = "\n";
            }
        }
    }

    public static String fromUTF8ByteArray(byte[] bytes) {
        int i = 0;
        int length = 0;
        while (i < bytes.length) {
            length++;
            if ((bytes[i] & 240) == 240) {
                length++;
                i += 4;
            } else if ((bytes[i] & 224) == 224) {
                i += 3;
            } else if ((bytes[i] & 192) == 192) {
                i += 2;
            } else {
                i++;
            }
        }
        char[] cs = new char[length];
        i = 0;
        length = 0;
        while (i < bytes.length) {
            int U;
            char ch;
            if ((bytes[i] & 240) == 240) {
                U = (((((bytes[i] & 3) << 18) | ((bytes[i + 1] & 63) << 12)) | ((bytes[i + 2] & 63) << 6)) | (bytes[i + 3] & 63)) - 65536;
                char W2 = (char) (56320 | (U & 1023));
                int length2 = length + 1;
                cs[length] = (char) (55296 | (U >> 10));
                i += 4;
                ch = W2;
                length = length2;
            } else if ((bytes[i] & 224) == 224) {
                ch = (char) ((((bytes[i] & 15) << 12) | ((bytes[i + 1] & 63) << 6)) | (bytes[i + 2] & 63));
                i += 3;
            } else if ((bytes[i] & 208) == 208) {
                ch = (char) (((bytes[i] & 31) << 6) | (bytes[i + 1] & 63));
                i += 2;
            } else if ((bytes[i] & 192) == 192) {
                ch = (char) (((bytes[i] & 31) << 6) | (bytes[i + 1] & 63));
                i += 2;
            } else {
                ch = (char) (bytes[i] & 255);
                i++;
            }
            U = length + 1;
            cs[length] = ch;
            length = U;
        }
        return new String(cs);
    }

    public static byte[] toUTF8ByteArray(String string) {
        return toUTF8ByteArray(string.toCharArray());
    }

    public static byte[] toUTF8ByteArray(char[] string) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            toUTF8ByteArray(string, bOut);
            return bOut.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("cannot encode string to byte array!");
        }
    }

    public static void toUTF8ByteArray(char[] string, OutputStream sOut) throws IOException {
        char[] c = string;
        int i = 0;
        while (i < c.length) {
            char ch = c[i];
            if (ch < 128) {
                sOut.write(ch);
            } else if (ch < 2048) {
                sOut.write(192 | (ch >> 6));
                sOut.write(128 | (ch & 63));
            } else if (ch < 55296 || ch > 57343) {
                sOut.write(224 | (ch >> 12));
                sOut.write(((ch >> 6) & 63) | 128);
                sOut.write(128 | (ch & 63));
            } else if (i + 1 < c.length) {
                char W1 = ch;
                i++;
                char W2 = c[i];
                if (W1 <= 56319) {
                    int codePoint = (((W1 & 1023) << 10) | (W2 & 1023)) + 65536;
                    sOut.write(240 | (codePoint >> 18));
                    sOut.write(((codePoint >> 12) & 63) | 128);
                    sOut.write(((codePoint >> 6) & 63) | 128);
                    sOut.write(128 | (codePoint & 63));
                } else {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
            } else {
                throw new IllegalStateException("invalid UTF-16 codepoint");
            }
            i++;
        }
    }

    public static String toUpperCase(String string) {
        boolean changed = false;
        char[] chars = string.toCharArray();
        for (int i = 0; i != chars.length; i++) {
            char ch = chars[i];
            if ('a' <= ch && 'z' >= ch) {
                changed = true;
                chars[i] = (char) ((ch - 97) + 65);
            }
        }
        if (changed) {
            return new String(chars);
        }
        return string;
    }

    public static String toLowerCase(String string) {
        boolean changed = false;
        char[] chars = string.toCharArray();
        for (int i = 0; i != chars.length; i++) {
            char ch = chars[i];
            if ('A' <= ch && 'Z' >= ch) {
                changed = true;
                chars[i] = (char) ((ch - 65) + 97);
            }
        }
        if (changed) {
            return new String(chars);
        }
        return string;
    }

    public static byte[] toByteArray(char[] chars) {
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    public static byte[] toByteArray(String string) {
        byte[] bytes = new byte[string.length()];
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = (byte) string.charAt(i);
        }
        return bytes;
    }

    public static int toByteArray(String s, byte[] buf, int off) {
        int count = s.length();
        for (int i = 0; i < count; i++) {
            buf[off + i] = (byte) s.charAt(i);
        }
        return count;
    }

    public static String fromByteArray(byte[] bytes) {
        return new String(asCharArray(bytes));
    }

    public static char[] asCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i != chars.length; i++) {
            chars[i] = (char) (bytes[i] & 255);
        }
        return chars;
    }

    public static String[] split(String input, char delimiter) {
        int i;
        Vector v = new Vector();
        boolean moreTokens = true;
        while (true) {
            i = 0;
            if (!moreTokens) {
                break;
            }
            int tokenLocation = input.indexOf(delimiter);
            if (tokenLocation > 0) {
                v.addElement(input.substring(0, tokenLocation));
                input = input.substring(tokenLocation + 1);
            } else {
                moreTokens = false;
                v.addElement(input);
            }
        }
        String[] res = new String[v.size()];
        while (i != res.length) {
            res[i] = (String) v.elementAt(i);
            i++;
        }
        return res;
    }

    public static StringList newList() {
        return new StringListImpl();
    }

    public static String lineSeparator() {
        return LINE_SEPARATOR;
    }
}
