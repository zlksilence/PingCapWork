package PingCap.Data;

import java.io.*;
import java.util.Arrays;

import PingCap.Util.Util;

public class Node {
    private byte[] key;
    private byte[] value;
    public Node(byte[] k, byte[] v){
        key = k;
        value = v;
    }
    public Node(){
    }
    public Node(DataInput dataInput) throws IOException {
        key = Util.readKey(dataInput);
        value = Util.readValue(dataInput);
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    public byte[] toBytes(){
        return Util.getBytes(key,value);
    }
    public void write(DataOutput out) throws IOException{
        Util.writeKey(out,key);
        Util.writeValue(out,value);
    }
    public void read(DataInput input) throws IOException{
        key = Util.readKey(input);
        value = Util.readValue(input);
    }

    public int len(){
        return 8+key.length+value.length;
    }
    public String toUtf8String()  {
        String re=null;
        try {
            String k = new String(key, "utf8");
            String v = new String(value,"utf8");
            return "Node{" +
                    "key size= "+key.length+
                    ",key=" + k +
                    ",value size= "+value.length+
                    ", value=" +v+
                    '}';
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();return null;
        }
    }
    @Override
    public String toString() {
        return "Node{" +
                "key size= "+key.length+
                ",key=" + Arrays.toString(key) +
                ",value size= "+value.length+
                ", value=" + Arrays.toString(value) +
                '}';
    }



    public static void main(String[] arg) throws Exception {
//        Node d = new Node("key".getBytes(),"valuefortest中".getBytes());
//        System.out.println(d.toUtf8String());
//       DataOutputStream ots = new DataOutputStream(new FileOutputStream("temp"));
//       for(int i=0;i<10;i++){
//           d.setValue((""+i).getBytes());
//           d.write(ots);
//       }
//       ots.close();
//        DataInputStream ips = new DataInputStream(new FileInputStream("temp"));
//     //   int i = ips.readInt();
//        for(int i=0;i<10;i++) {
//            Node d1 = new Node(ips);
//            System.out.println(d1.toUtf8String());
//        }
        RandomAccessFile rdf = new RandomAccessFile("temp","r");
        rdf.seek(24);
        Node d1 = new Node(rdf);
       System.out.println(rdf.getFilePointer());
        System.out.println(d1.toUtf8String());
        rdf.close();
    }
}
