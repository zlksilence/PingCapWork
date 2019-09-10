import PingCap.BplusTree.BplusTree;
import PingCap.Data.Node;
import PingCap.Util.Util;
import org.junit.Test;

import java.io.File;
import java.util.Random;

public class TreeTest {
    // B+索引测试 新建索引文件，随机造数据插入并落盘
    @Test
    public void testCreateTreeData() {
        int size = 1000;
        String indexName = "index";
        String metaName = "meta";
        File file = new File(indexName);
        if(file.exists()) file.delete();
        BplusTree tree = BplusTree.createBTree("index","meta",true);
        System.out.println("\nTest random insert " + size + " datas, of order:");

        int randomNumber = 0;
        long current = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
//            randomNumber = random.nextInt(size);
            randomNumber =i;
            if(i==42){
                System.out.println(i);
            }
            tree.insertOrUpdate(Util.intToByteArray(randomNumber), randomNumber);
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time  " + duration+" ms");
        System.out.println("current node number:"+tree.cacheManger.getNumNode());
        System.out.println(tree.getHeight());
//        byte[] b=new byte[0];
//        Long t = tree.search(b);
        // open indexFile
        System.out.println("查询："+tree.search(Util.intToByteArray(5)));
        for (int i = 0; i < size; i++) {
            Long p = tree.search(Util.intToByteArray(i));
            if(p!=null) {
//                System.out.println("key:"+i+",value:"+p);
            }else {
                System.out.println("can not find key:" +i);
            }
        }
        tree.cacheManger.flushAll();
    }
    @Test
    public void testTreeSearch(){
        BplusTree tree = BplusTree.createBTree("index","meta",false);

        int size = 200;
        for (int i = 0; i < size; i++) {
            Long p = tree.search(Util.intToByteArray(i));
            if(p!=null)
                System.out.println("key:"+i+",value:"+p);
            else {
                System.out.println("can not find key:" +i);
            }
        }
//        p= tree.search(n.getKey());
//        System.out.println(p);
//        tree.close();
    }
}
