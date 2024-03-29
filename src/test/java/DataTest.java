import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import PingCap.Data.Node;
import PingCap.Util.Util;
import org.junit.Test;

public class DataTest {

    static String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    static Random random = new Random();
    static double[] size=       {0.1,0.5,1,4,16,64,128,1024}; // 单位kb  一下key value将表明生成的各大小的权重
    static int[] keyWeight=     { 8,   1,1,0,0, 0,  0,  0}; // key的大小权重
//    static int[] keyWeight=     { 4,   1,1,2,0, 0,  0,  0};
    static int[] valueWeight =  { 0,   0,0,6,2, 1,  1,  0}; //value的大小权重
    // 获取一个随机长度
    static byte[] getRandomByte(int len){
        char[] ch = new char[len];
        for(int i =0;i<len;i++){
            int a = random.nextInt(str.length());
            ch[i] = str.charAt(a);
        }
        return String.valueOf(ch).getBytes();
    }
    // 加权法随机获取一个key
    static int  getKeyLen(){
        int k = random.nextInt(10);
        for(int i =0;i<keyWeight.length;i++){
            int w = keyWeight[i];
            k = k-w;
            if(k<0){
                return random.nextInt((int)(size[i]*1024));
            }
        }
        return 0;
    }
    // 加权法随机获取一个value
    static int  getValueLen(){
        int k = random.nextInt(10);
        for(int i =0;i<valueWeight.length;i++){
            int w = valueWeight[i];
            k = k-w;
            if(k<0){
                return random.nextInt((int)size[i]*1024);
            }
        }
        return 0;
    }
    /**随机造成数据
       dataFileName 指定了数据文件的名字
     **/
    @Test
    public void productRandomData() {
        String dataFileName = "data.bin";
        // 造数据的条数 1000 大约13M  10000 大约121M 100000 大约1G
//        long count =1000000; // 大约10G
        long count =100000; // 大约1G
        long size=0;
        File file = new File(dataFileName);
        if(file.exists()) file.delete();
        long current = System.currentTimeMillis();
        try(RandomAccessFile dos = new  RandomAccessFile(dataFileName,"rw")) {
            int c=1;
            for (int i = 0; i < count; i++) {
                // 随机产生key 和 value
                byte[] key = getRandomByte(getKeyLen());
                byte[] value = getRandomByte(getValueLen());
                Util.writeKey(dos, key);
                Util.writeValue(dos, value);
                size+=4+key.length+4+value.length;
                if(size> (c*1024*1024)){
                    c++;
                    System.out.println("已写入"+(size/(1024*1024))+"M 的数据" );
                }
//                dos.flush();
            }
            System.out.println("已随机造了"+count+"条数据 大小大约"+(size/(1024*1024))+"M");
        }catch (IOException e){
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time  " + duration+" ms");
    }
    /**
     * 从文换的开始顺序读取 1000条数 并打印
     * 测试造的数据则正确与否
     * **/
    @Test
    public void printData() {
        String dataFileName = "data.bin";
        long count =1000;
        long current = System.currentTimeMillis();
        try(RandomAccessFile dos = new  RandomAccessFile(dataFileName,"r")) {
//            List<Node> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                System.out.println(new Node(dos));
            }
//            list.forEach(t->{
//                System.out.println(t.toUtf8String());
//            });
        }catch (IOException e){
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time  " + duration+" ms");
    }
    /**
     * 从文换的开始顺序读取 读取全部文件数据 并打印
     * 测试造的数据则正确与否
     * **/
    @Test
    public void printAllData() {
//        String dataFileName = "data.bin";
        String dataFileName = "newdata.bin";
        long count =0;
        long current = System.currentTimeMillis();
        try( RandomAccessFile bis = new  RandomAccessFile(dataFileName,"r")) {

            long len = bis.length();
            while (bis.getFilePointer()<len) {
                // 读取一个 key value 用Node封装
                Node node = new Node(bis);
                System.out.println(node.toUtf8String());
                count++;
            }
            System.out.println("总共"+count+"条数据");
        }catch (IOException e){
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time  " + duration+" ms");
    }

}
