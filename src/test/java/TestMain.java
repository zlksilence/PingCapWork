import PingCap.BplusTree.BplusTree;
import PingCap.Data.Node;
import PingCap.Query;
import PingCap.QueryIml;
import PingCap.Util.Util;
import org.junit.Test;

import java.io.*;

public class TestMain {
    // 预处理 顺讯扫描数据文件 建立B+索引
    @Test
    public void prePare(){
//        String dataFileName = "data.bin";
        String dataFileName = "data_10G.bin";
        // 1000 大约13M  10000 大约121M 100000 大约1G
        long count =0;
        String indexName = "index_10G";
        String metaName = "meta_10G";
        File file = new File(indexName);
        if(file.exists()) file.delete();
        BplusTree tree = BplusTree.createBTree(indexName,metaName,true);
        long len=0;
        long current = System.currentTimeMillis();
        try(RandomAccessFile bis = new  RandomAccessFile(dataFileName,"r")) {
            long lenFile = bis.length();
            while (bis.getFilePointer()<lenFile) {
                    count++;
                    Node node = new Node(bis);
                    tree.insertOrUpdate(node.getKey(), len);
                    len+=node.len();
             }
            tree.close();
            System.out.println("已对"+count+"条数据("+(len/(1024*1024))+"M)建立了索引");
        }catch (IOException e){
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time : " + duration+" ms");
    }

    // B+索引测试 从磁盘读B+树索引并查询
    @Test
    public void testQuery(){
        String dataName = "data.bin";
        String indexName= "index";
        String metaName ="meta";
        Query q=null;
        long current = System.currentTimeMillis();
        try {
            q = new QueryIml(dataName,indexName,metaName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Node n =new Node("9s".getBytes(),null);
       byte[] v = q.find(new byte[0]);
        if(v!=null) {
            n.setValue(v);
            System.out.println(n.toUtf8String());
        }
        q.close();
        long duration = System.currentTimeMillis() - current;
        System.out.println("time : " + duration+" ms");
    }

    @Test
    public void testQueryM(){
        String dataName = "data.bin";
        String indexName= "index";
        String metaName ="meta";
        long count =1000;
        Query q=null;
        long current = System.currentTimeMillis();
        try {
            q = new QueryIml(dataName,indexName,metaName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int success=0;
        try(DataInputStream dos = new DataInputStream(new FileInputStream(dataName))) {
//            List<Node> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Node node=new Node(dos);
               byte[] v= q.find(node.getKey());
               if(v==null){
                   System.out.println("未查找到此key:"+new String(node.getKey()));
               }
               else{
                   if(Util.compareKey(node.getValue(),v)==0){
                       success++;
//                       System.out.println("查找到key");
                   }
                   else {
                       System.out.println("查找到key,但value不一致\nv1:"+node.toUtf8String()
                               +"\nv2:"+(new String(v,"utf8")));
                   }
               }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        q.close();
        long duration = System.currentTimeMillis() - current;
        System.out.println("time : " + duration+" ms");
        System.out.println("总共查找"+count+"个key，查找成功"+success+"个");
    }
}
