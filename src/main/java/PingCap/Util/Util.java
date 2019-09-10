package PingCap.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public class Util {
    private Util(){

    }
    public static byte[] readKey(DataInput input) throws IOException {
        int len =input.readInt();
        byte[] key = new byte[len];
        input.readFully(key);
        return key;
    }
    public static byte[] readValue(DataInput input) throws IOException{
        int len = input.readInt();
        byte[] value = new byte[len];
        input.readFully(value);
        return value;
    }
    public static void writeKey(DataOutput output, byte[] key) throws IOException{
        output.writeInt(key.length);
        output.write(key);
    }

    public static void writeValue(DataOutput output,byte[] value) throws IOException{
        output.writeInt(value.length);
        output.write(value);
    }

    public static byte[] getBytes(byte[] key,byte[] value){
        int klen =key.length;
        byte[] data = new byte[key.length+value.length+8];
        System.arraycopy(klen,0,data,0,4);
        System.arraycopy(key,0,data,4,klen);
        System.arraycopy(value.length,32+key.length,data,(klen=32+key.length),4);
        System.arraycopy(value,0,data,klen,value.length);
        return data;
    }

    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static int encodeInt(int n, byte[] buf, int pos){
// move sign to low-order bit, and flip others if negative
        n = (n << 1) ^ (n >> 31);
        int start = pos;
        if ((n & ~0x7F) != 0) {
            buf[pos++] = (byte)((n | 0x80) & 0xFF);
            n >>>= 7;
            if (n > 0x7F) {
                buf[pos++] = (byte)((n | 0x80) & 0xFF);
                n >>>= 7;
                if (n > 0x7F) {
                    buf[pos++] = (byte)((n | 0x80) & 0xFF);
                    n >>>= 7;
                    if (n > 0x7F) {
                        buf[pos++] = (byte)((n | 0x80) & 0xFF);
                        n >>>= 7;
                    }
                }
            }
        }
        buf[pos++] = (byte) n;
        return pos - start;
    }
    public static int decodeInt(byte[] buf, int pos) throws IOException {
        int len = 1;
        int b = buf[pos] & 0xff;
        int n = b & 0x7f;
        if (b > 0x7f) {
            b = buf[pos + len++] & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f) {
                b = buf[pos + len++] & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f) {
                    b = buf[pos + len++] & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f) {
                        b = buf[pos + len++] & 0xff;
                        n ^= (b & 0x7f) << 28;
                        if (b > 0x7f) {
                            throw new IOException("Invalid int encoding");
                        }
                    }
                }
            }
        }
        pos += len;
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

   public static int compareKey(byte[] a,byte[] b){
        int len = Math.min(a.length,b.length);
        for(int i=0;i<len;i++){
            if(a[i]<b[i]){
                return -1;
            }
            else if(a[i] > b[i]){
                return 1;
            }
        }
        if(len ==a.length && len==b.length){
            return 0;
        }
        if(len == a.length){
            return -1;
        }else {
            return 1;
        }
    }

    public static void main(String[] arg)throws Exception{
        int a = Integer.MAX_VALUE;
        byte[] bytes = new byte[10];
        System.out.println(encodeInt(a,bytes,0));
        System.out.println(decodeInt(bytes,0));
    }
}
