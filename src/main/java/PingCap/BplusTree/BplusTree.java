package PingCap.BplusTree;

import PingCap.Util.Config;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * B+树：
 *  此B+树存放与磁盘上，每个Node节点通过《索引文件》偏移量来确定位置指针并表示此Node
 *  节点的前驱、后继和父节点等指针 都是通过文件的偏移量来实现
 *
 *  不同与普通B+树，此B+树Node的大小大于 NDXEX_BLOCK_SIZE(默认 8kb) 后将发生分裂，
 *  以此来保证每个节点落盘时大小都不会超过 INDXEX_BLOCK_SIZE(默认 8kb)
 *
 *  B+树叶子节点存放key和 value ，value是此key在 《数据文件》 的偏移量
 *  B+树的非叶子节点存放key 和 子节点的指针
 *
 * B+树 暂仅支持查询、更新、和插入，不支持删除操作
 *
 * 通过LRU算法来缓存B+树的Node节点
 * @author  zhoulikang
 */
public class BplusTree {
    static Logger log = Logger.getLogger("BplusTree");
    /** 根节点 */
    protected BplusNode root;

    /** 阶数，M值 */
    protected int order;

    /** 叶子节点的链表头 */
    protected BplusNode head;

    /** 树高*/
    protected int height = 0;

    public CacheManger cacheManger;

    public BplusNode getHead() {
        return head;
    }

    public void setHead(BplusNode head) {
        this.head = head;
    }

    public BplusNode getRoot() {
        return root;
    }

    public void setRoot(BplusNode root) {
        this.root = root;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public Long search(byte[] key) {
        return root.get(key);
    }

    public Long remove(byte[] key) {
        return root.remove(key);
    }

    public void insertOrUpdate(byte[]key, long value) {
//        if(key==null || key.length==0){
//            throw new Exception("key不能为null或空");
//        }
        if(2*key.length>Config.INDEX_BLOCK_SIZE){
            log.severe("key的大小超过了限制");
            return;
        }
        root.insert(key, value);
    }

    private BplusTree() {
    }

    /**
     *
     * @param indexName 索引文件名 存放B+树
     * @param metaName 元数据名  存放B+树的头
     * @param isCreate 是否能新建B+  树索引  新建即清空文件内容
     * @return
     */
    public static BplusTree createBTree(String indexName,String metaName,boolean isCreate){
        BplusTree tree = new BplusTree();
        CacheManger cacheManger = new CacheManger(indexName,metaName,isCreate,tree);
        tree.cacheManger =cacheManger;
        tree.head=tree.root;
        tree.order=5;
        return tree;
    }

    private BplusNode newNode(boolean isLeaf, boolean isRoot){
        return cacheManger.newNode(isLeaf,isRoot);
    }

    public void close() throws IOException {
        cacheManger.close();
    }
    @Override
    public String toString() {
        return "BplusTree{" +
                "root=" + root +
                ", order=" + order +
                ", head=" + head +
                ", height=" + height +
                '}';
    }
}
