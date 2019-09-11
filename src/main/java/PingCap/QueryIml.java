package PingCap;


import PingCap.BplusTree.BplusTree;
import PingCap.Data.DataFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *  基于B+树的查询实现
 *  此B+树存放与磁盘上，每个Node节点通过《索引文件》偏移量来确定位置指针并表示此Node
 *  节点的前驱、后继和父节点等指针 都是通过文件的偏移量来实现
 *
 *  不同与普通B+树，此B+树Node的大小大于 NDXEX_BLOCK_SIZE(默认 8kb) 后将发生分裂，
 *  以此来保证每个节点落盘时大小都不会超过 INDXEX_BLOCK_SIZE(默认 8kb)
 *
 *  B+树叶子节点存放key和 value ，value是此key在 《数据文件》 的偏移量
 *  B+树的非叶子节点存放key 和 子节点的指针
 *
 *  暂仅支持查询
 *
 *  查找：
 *      现在B+树索引中查找key所对应的value，根据此value指针去《数据文件》中读取真正的value
 *
 *  注： 使用前提时已通过预处理操作建立了《数据文件》所对应的B+树索引
 *  预处理操作在 测试类 TestMain 有实现
 */
public class QueryIml implements Query {

    String dataName;
    String indexName;
    String metaName;
    DataFile dataFile;
    BplusTree tree;

    public QueryIml(String dataName,String indexName,String metaName) throws IOException{
        this.dataName = dataName;
        this.indexName = indexName;
        this.metaName = metaName;
        init();
    }
    void init() throws  IOException{
//        try {
            dataFile = new DataFile(dataName);
            tree = BplusTree.createBTree(indexName,metaName,false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void insert(byte[] key, byte[] value) {
        // TODO
        long position = 0;
        try {
            position = dataFile.append(key,value);
            tree.insertOrUpdate(key,position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] find(byte[] key) {
        Long p = tree.search(key);
        if(p==null){
            return null;
        }
        else {
            return dataFile.find(p,key);
        }
    }

    @Override
    public void update(byte[] key, byte[] value) {
        //TODO
    }

    @Override
    public void delete(byte[] key) {
        //TODO
    }

    @Override
    public void insertBatch(List<byte[]> keys, List<byte[]> values) {
        //TODO
        for(int i=0;i<keys.size();i++){
            insert(keys.get(i),values.get(i));
        };
    }

    @Override
    public List<byte[]> findRange(byte[] start, byte[] end) {
        return null;
    }
    public void close(){
        try {
            dataFile.close();
            tree.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
