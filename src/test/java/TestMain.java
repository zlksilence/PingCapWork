import PingCap.BplusTree.BplusTree;
import PingCap.Data.Node;
import PingCap.Query;
import PingCap.QueryIml;
import PingCap.Util.Util;
import org.junit.Test;

import java.io.*;

public class TestMain {
    // 预处理 顺讯扫描数据文件 建立B+索引

    /**
     * 数据文件的预处理
     * 针对数据文件 dataFileName 建立B+树
     *  B+树索引最终存放于 indexName
     *  metaName 存放 B+树的root节点所在位置
     *
     *  对于数据文件存在重复的key的问题，B+树会用新key的value覆盖旧的key
     */
    @Test
    public void prePare(){
        String dataFileName = "data.bin";
//        String dataFileName = "data_10G.bin";
        // 1000 大约13M  10000 大约121M 100000 大约1G
        long count =0;
//        String indexName = "index_10G";
        String indexName = "index";
//        String metaName = "meta_10G";
        String metaName = "meta";
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
    /**
     * 在已建立索引的情况下，测试查询情况
     * 从数据文件顺序读取1000个key，通过B+树查询此key
     *
     * 可能会出现 读取的key的value 与 查询出来的value不一致的情况，
     * 这是正常情况，因为随机造的数据会存在重复key的数据，B+树只会查询出最新的key对应的 value
     */
    @Test
    public void testQueryM(){
        String dataName = "data.bin";
//        String dataName = "data_10G.bin";
        String indexName= "index";
        String metaName ="meta";
//        String indexName = "index_10G";
//        String metaName = "meta_10G";
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
            for (int i = 0; i < count; i++) {
                Node node=new Node(dos);
                byte[] v= q.find(node.getKey());
//                System.out.println(i);
                if(v==null){
                    System.out.println("未查找到此key:"+new String(node.getKey()));
                }
                else{
                    if(Util.compareKey(node.getValue(),v)==0){
                        success++;
                    }
                    else {
                        // 可能会出现 读取的key的value 与 查询出来的value不一致的情况，
                        // 这是正常情况，因为随机造的数据会存在重复key的数据，B+树只会查询出最新的key对应的 value
                        System.out.println("查找到key,但value不一致\nv1:"+node.toUtf8String());
//                                +"\nv2:"+(new String(v,"utf8")));
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        q.close();
        long duration = System.currentTimeMillis() - current;
        System.out.println("time : " + duration+" ms");
        System.out.println("总共查找"+count+"个key，查找出完全一样的"+success+"个");
    }
    // 单个key的查询
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


}
