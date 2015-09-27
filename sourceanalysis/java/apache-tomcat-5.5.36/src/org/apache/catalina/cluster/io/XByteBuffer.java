/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.cluster.io;

import org.apache.catalina.cluster.tcp.ClusterData;

/**
 * The XByteBuffer provides a dual functionality.
 * One, it stores message bytes and automatically extends the byte buffer if needed.<BR>
 * Two, it can encode and decode packages so that they can be defined and identified
 * as they come in on a socket.
 * <br/>
 * Transfer package:
 * <ul>
 * <li><b>START_DATA/b> - 7 bytes - <i>FLT2002</i></li>
 * <li><b>COMPRESS</b>  - 4 bytes - is message compressed flag</li>
 * <li><b>SIZE</b>      - 4 bytes - size of the data package</li>
 * <li><b>DATA</b>      - should be as many bytes as the prev SIZE</li>
 * <li><b>END_DATA</b>  - 7 bytes - <i>TLF2003</i></lI>
 * </ul>
 * FIXME: Why we not use a list of byte buffers?
 * FIXME: Used a pool of buffers instead, every time new generation
 *
 * @author Filip Hanik
 * @author Peter Rossbach
 * @version $Id: XByteBuffer.java 939539 2010-04-30 01:31:33Z kkolinko $
 */
public class XByteBuffer
{
    
    public static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( XByteBuffer.class );
    
    /**
     * This is a package header, 7 bytes (FLT2002)
     */
    public static final byte[] START_DATA = {70,76,84,50,48,48,50};
    
    /**
     * This is the package footer, 7 bytes (TLF2003)
     */
    public static final byte[] END_DATA = {84,76,70,50,48,48,51};
 
    /**
     * Default size on the initial byte buffer
     */
    static final int DEF_SIZE = 1024;
 
    /**
     * Default size to extend the buffer with
     */
    static final int DEF_EXT  = 1024;
    
    /**
     * Variable to hold the data
     */
    protected byte[] buf = null;
    
    /**
     * Current length of data in the buffer
     */
    protected int bufSize = 0;
    
    /**
     * Constructs a new XByteBuffer
     * @param size - the initial size of the byte buffer
     */
    public XByteBuffer(int size) {
        buf = new byte[size];
    }

    /**
     * Constructs a new XByteBuffer with an initial size of 1024 bytes
     */
    public XByteBuffer()  {
        this(DEF_SIZE);
    }

    /**
     * Returns the bytes in the buffer, in its exact length
     */
    public byte[] getBytes() {
        byte[] b = new byte[bufSize];
        System.arraycopy(buf,0,b,0,bufSize);
        return b;
    }

    /**
     * Resets the buffer
     */
    public void clear() {
        bufSize = 0;
    }

    /**
     * Appends the data to the buffer. If the data is incorrectly formatted, ie, the data should always start with the
     * header, false will be returned and the data will be discarded.
     * @param b - bytes to be appended
     * @param off - the offset to extract data from
     * @param len - the number of bytes to append.
     * @return true if the data was appended correctly. Returns false if the package is incorrect, ie missing header or something, or the length of data is 0
     */
    public boolean append(byte[] b, int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0))  {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return false;
        }

        int newcount = bufSize + len;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, bufSize);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, bufSize, len);
        bufSize = newcount;

        if (bufSize > START_DATA.length && (firstIndexOf(buf,0,START_DATA)==-1)){
            bufSize = 0;
            log.error("Discarded the package, invalid header");
            return false;
        }
        return true;
    }


    /**
     * Internal mechanism to make a check if a complete package exists
     * within the buffer
     * @return - true if a complete package (header,compress,size,data,footer) exists within the buffer
     */
    public int countPackages()
    {
        int cnt = 0;
        int pos = START_DATA.length;
        int start = 0;

        while ( start < bufSize ) {
            //first check start header
            int index = XByteBuffer.firstIndexOf(buf,start,START_DATA);
            //if the header (START_DATA) isn't the first thing or
            //the buffer isn't even 14 bytes
            if ( index != start || ((bufSize-start)<14) ) break;
            //next 4 bytes are compress flag not needed for count packages
            //then get the size 4 bytes
            int size = toInt(buf, pos+4);
            //now the total buffer has to be long enough to hold
            //START_DATA.length+8+size+END_DATA.length
            pos = start + START_DATA.length + 8 + size;
            if ( (pos + END_DATA.length) > bufSize) break;
            //and finally check the footer of the package END_DATA
            int newpos = firstIndexOf(buf, pos, END_DATA);
            //mismatch, there is no package
            if (newpos != pos) break;
            //increase the packet count
            cnt++;
            //reset the values
            start = pos + END_DATA.length;
            pos = start + START_DATA.length;
        }
        return cnt;
    }

    /**
     * Method to check if a package exists in this byte buffer.
     * @return - true if a complete package (header,compress,size,data,footer) exists within the buffer
     */
    public boolean doesPackageExist()  {
        return (countPackages()>0);
    }

    /**
     * Extracts the message bytes from a package.
     * If no package exists, a IllegalStateException will be thrown.
     * @param clearFromBuffer - if true, the package will be removed from the byte buffer
     * @return - returns the actual message bytes (header, compress,size and footer not included).
     */
    public ClusterData extractPackage(boolean clearFromBuffer)
            throws java.io.IOException {
        int psize = countPackages();
        if (psize == 0) throw new java.lang.IllegalStateException("No package exists in XByteBuffer");
        int compress = toInt(buf, START_DATA.length);
        int size = toInt(buf, START_DATA.length +4);
        byte[] data = new byte[size];
        System.arraycopy(buf, START_DATA.length + 8, data, 0, size);
        ClusterData cdata = new ClusterData() ;
        cdata.setMessage(data);
        cdata.setCompress(compress);
        if (clearFromBuffer) {
            int totalsize = START_DATA.length + 8 + size + END_DATA.length;
            bufSize = bufSize - totalsize;
            System.arraycopy(buf, totalsize, buf, 0, bufSize);
        }
        return cdata;
    }

    /**
     * Convert four bytes to an int
     * @param b - the byte array containing the four bytes
     * @param off - the offset
     * @return the integer value constructed from the four bytes
     * @exception java.lang.ArrayIndexOutOfBoundsException
     */
    public static int toInt(byte[] b,int off){
        return ( ( (int) b[off+3]) & 0xFF) +
            ( ( ( (int) b[off+2]) & 0xFF) << 8) +
            ( ( ( (int) b[off+1]) & 0xFF) << 16) +
            ( ( ( (int) b[off+0]) & 0xFF) << 24);
    }

    /**
     * Convert eight bytes to a long
     * @param b - the byte array containing the four bytes
     * @param off - the offset
     * @return the long value constructed from the eight bytes
     * @exception java.lang.ArrayIndexOutOfBoundsException
     */
    public static long toLong(byte[] b,int off){
        return ( ( (long) b[off+7]) & 0xFF) +
            ( ( ( (long) b[off+6]) & 0xFF) << 8) +
            ( ( ( (long) b[off+5]) & 0xFF) << 16) +
            ( ( ( (long) b[off+4]) & 0xFF) << 24) +
            ( ( ( (long) b[off+3]) & 0xFF) << 32) +
            ( ( ( (long) b[off+2]) & 0xFF) << 40) +
            ( ( ( (long) b[off+1]) & 0xFF) << 48) +
            ( ( ( (long) b[off+0]) & 0xFF) << 56);
    }

    /**
     * Converts an integer to four bytes
     * @param n - the integer
     * @return - four bytes in an array
     */
    public static byte[] toBytes(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);
        return b;
    }

    /**
     * Converts an long to eight bytes
     * @param n - the long
     * @return - eight bytes in an array
     */
    public static byte[] toBytes(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n);
        n >>>= 8;
        b[6] = (byte) (n);
        n >>>= 8;
        b[5] = (byte) (n);
        n >>>= 8;
        b[4] = (byte) (n);
        n >>>= 8;
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);
        return b;
    }

    /**
     * Similar to a String.IndexOf, but uses pure bytes
     * @param src - the source bytes to be searched
     * @param srcOff - offset on the source buffer
     * @param find - the string to be found within src
     * @return - the index of the first matching byte. -1 if the find array is not found
     */
    public static int firstIndexOf(byte[] src, int srcOff, byte[] find){
        int result = -1;
        if (find.length > src.length) return result;
        if (find.length == 0 || src.length == 0) return result;
        if (srcOff >= src.length ) throw new java.lang.ArrayIndexOutOfBoundsException();
        boolean found = false;
        int srclen = src.length;
        int findlen = find.length;
        byte first = find[0];
        int pos = srcOff;
        while (!found) {
            //find the first byte
            while (pos < srclen){
                if (first == src[pos])
                    break;
                pos++;
            }
            if (pos >= srclen)
                return -1;

            //we found the first character
            //match the rest of the bytes - they have to match
            if ( (srclen - pos) < findlen)
                return -1;
            //assume it does exist
            found = true;
            for (int i = 1; ( (i < findlen) && found); i++)
                found = found && (find[i] == src[pos + i]);
            if (found)
                result = pos;
            else if ( (srclen - pos) < findlen)
                return -1; //no more matches possible
            else
                pos++;
        }
        return result;
    }

    /**
     * Creates a complete data package
     * @param indata - the message data to be contained within the package
     * @param compressed - compression flag for the indata buffer
     * @return - a full package (header,compress,size,data,footer)
     * 
     */
    public static byte[] createDataPackage(byte[] indata, int compressed)
            throws java.io.IOException {
        byte[] data = indata;
        byte[] comprdata = XByteBuffer.toBytes(compressed);
        int length = 
            START_DATA.length + //header length
            4 + //compression flag
            4 + //data length indicator
            data.length + //actual data length
            END_DATA.length; //footer length
        byte[] result = new byte[length];
        System.arraycopy(START_DATA, 0, result, 0, START_DATA.length);
        System.arraycopy(comprdata, 0, result, START_DATA.length, 4);
        System.arraycopy(toBytes(data.length), 0, result, START_DATA.length + 4, 4);
        System.arraycopy(data, 0, result, START_DATA.length + 8, data.length);
        System.arraycopy(END_DATA, 0, result, START_DATA.length + 8 + data.length, END_DATA.length);
        return result;
    }
}
