package PingCap;


import PingCap.BplusTree.BplusTree;
import PingCap.Data.DataFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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
