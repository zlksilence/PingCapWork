package PingCap.BplusTree;

import PingCap.Util.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.logging.Logger;

/**
 * 缓存管理
 * LUR策略管理B+树的Node节点
 * 最老使用的Node将写回磁盘
 * 查找不到的Node将从磁盘读取或新建
 * @author zhoulikang
 */
public class CacheManger {
    static Logger log = Logger.getLogger("CacheManger");
    Map<Long,BplusNode> m;   // 存储内存中的Node
    RandomAccessFile indexFile=null;
    RandomAccessFile metaFile=null;
    List<Long> empty = new LinkedList<>();  /// 存储已被释放的Node
    Long currentP=0L;      //   当前文件的偏移量
    public Meta meta;      // 元数据信息
    public BplusTree tree;  // B+数
    private int cacheSize=10; // 缓存的Node节点数量  大约 3G = 384×8KB
    public long maxSize=0;
    public CacheManger(String indexName,String metaName,boolean isCreate,BplusTree tree) {
//        m = new HashMap<>();
        // LUR 缓存使用的Node Long是Node在索引文件的便宜
        m = new LinkedHashMap<Long,BplusNode>((int) Math.ceil(cacheSize / 0.75f) + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long,BplusNode> eldest) {
//                flush(eldest.getValue());
                if(size()>cacheSize && !eldest.getValue().isRoot ){
                    if(eldest.getValue().isRoot || eldest.getValue().used>0){
                        m.remove(eldest.getKey());
                        m.put(eldest.getKey(),eldest.getValue());
                        return false;
                    }
                    else {
                        flush(eldest.getValue());
                        return true;
                    }
                }
                else {
                    return false;
                }
//                return size() > cacheSize;
            }
        };
        this.tree =tree;
        if(isCreate) {
            tree.root = new BplusNode(0,true,true,this);
            meta = new Meta();
            currentP=0L+Config.INDEX_BLOCK_SIZE;
            try {
                log.info("opening index file");
                indexFile = new RandomAccessFile(indexName, "rw");
                log.info("opening meta file");
                metaFile = new RandomAccessFile(metaName, "rw");
            } catch (FileNotFoundException e) {
                log.severe("索引文件未找到！");
            }
        }
        else {
            try {
                log.info("opening index file");
                indexFile = new RandomAccessFile(indexName, "rw");
                log.info("opening meta file");
                metaFile = new RandomAccessFile(metaName, "rw");
                meta = new Meta();
                meta.read(metaFile);
                tree.root = new BplusNode(meta.getMaxPosition(),true,true,this);
                indexFile.seek(meta.getRootPosition());
                tree.root.read(indexFile);
                currentP=meta.getMaxPosition();
            } catch (FileNotFoundException e) {
                log.severe("索引文件未找到！");
            }
            catch (IOException e){

            }
        }
        m.put(tree.root.position,tree.root);
    }

    /**
     * 根据偏移position获取一个Node
     * @param position
     * @return
     */
   public   BplusNode getNode(long position){
       if(position<0){
            return null;
       }
       // 缓存存在则 直接返回
        if(m.containsKey(position)){
            return m.get(position);
        }
       BplusNode node =null;
       try {
           // 不存在则从磁盘读取
            node = new BplusNode(position, false, false, this);
            indexFile.seek(node.position);
            node.read(indexFile);
            m.put(position,node);

        }catch (IOException e){
           log.severe("读取索引块失败！");
        }
       return node;
    }

    /**
     * 直接新建一个索引Node
     * @param isLeaf
     * @param isRoot
     * @return
     */
    public BplusNode newNode(boolean isLeaf, boolean isRoot){
         //  从空闲块新建
        if(empty.size()>0){
            BplusNode node  =new BplusNode(empty.get(0),isLeaf,isRoot,this);
            empty.remove(0);
            m.put(node.position,node);
            return node;
        }
        // 从文件末尾新建索引块
        BplusNode node  =new BplusNode(currentP,isLeaf,isRoot,this);
        m.put(currentP,node);
        currentP+=Config.INDEX_BLOCK_SIZE;
        return node;
    }

    /**
     * 释放一个节点
     * @param position
     */
    public void deleteNode(long position){
        if(m.containsKey(position)){
            m.remove(position);
        }
        empty.add(position);
//        empty.add(position);
    }
    private BplusNode createBplusNode(long position){
        try {
            indexFile.seek(position);
            BplusNode node = new BplusNode(position,false,false,this);
            node.read(indexFile);
            return node;
        }
        catch (IOException e){
            log.severe(e.toString());
        }
        return  null;
    }
    public long size_file(){
        return currentP;
    }
    public long size_mem(){
        return m.size()*Config.INDEX_BLOCK_SIZE;
    }
    public int getNumNode(){
        return m.size();
    }

    /**
     * 将一个Node写入磁盘
     * @param node
     */
    private void flush(BplusNode node){
        try {
            indexFile.seek(node.position);
            int b =  node.write(indexFile);
            if (b != node.bsize) {
                log.info("数据不一致 position:"+node.position +"\n"+ b + ":" + node.bsize);
            }
            if(node.bsize>maxSize){
                maxSize = node.bsize;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("脏也刷新失败！");
        }
    }

    /**
     * 将全部的Node写入磁盘
     */
    public void flushAll(){
        try {
            m.values().forEach(node->{
//            m.keySet().forEach(key -> {
                    flush(node);
            });
            log.info("所有索引数据页已写回磁盘");
            meta.setRootPosition(tree.root.position);
            meta.setMaxPosition(currentP);
            meta.write(metaFile);
            log.info("元数据页已写回磁盘");
            log.info("最大写入的索引块为"+((1.0*maxSize)/(1024))+"KB");
        }catch (IOException e){
            e.printStackTrace();
            log.severe("元数组刷新失败！"+e.toString());
        }
    }
    public List<BplusNode> getAll(){
        List<BplusNode> list = new ArrayList<>();
        m.keySet().forEach(key->{
            list.add(m.get(key));
        });
        return list;
    }
    public void close()throws IOException{
        flushAll();
        indexFile.close();
        metaFile.close();
    }
    @Override
    public String toString() {
        return "CacheManger{" +
                "m=" + m +
                ",\n indexFile=" + indexFile +
                ", metaFile=" + metaFile +
                ", empty=" + empty +
                ", currentP=" + currentP +
                ", meta=" + meta +
                ",\n tree=" + tree +
                '}';
    }
}
