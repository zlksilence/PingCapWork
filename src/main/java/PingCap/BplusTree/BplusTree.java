package PingCap.BplusTree;

import PingCap.Util.Config;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * B+树的定义：
 *
 * 1.任意非叶子结点最多有M个子节点；且M>2；M为B+树的阶数
 * 2.除根结点以外的非叶子结点至少有 (M+1)/2个子节点；
 * 3.根结点至少有2个子节点；
 * 4.除根节点外每个结点存放至少（M-1）/2和至多M-1个关键字；（至少1个关键字）
 * 5.非叶子结点的子树指针比关键字多1个；
 * 6.非叶子节点的所有key按升序存放，假设节点的关键字分别为K[0], K[1] … K[M-2],
 *  指向子女的指针分别为P[0], P[1]…P[M-1]。则有：
 *  P[0] < K[0] <= P[1] < K[1] …..< K[M-2] <= P[M-1]
 * 7.所有叶子结点位于同一层；
 * 8.为所有叶子结点增加一个链指针；
 * 9.所有关键字都在叶子结点出现
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

    // 测试


//    private static void testOrderRemove(int size, int order) {
//        BplusTree tree = new BplusTree(order);
//        System.out.println("\nTest order remove " + size + " datas, of order:"
//                + order);
//        System.out.println("Begin order insert...");
//        for (int i = 0; i < size; i++) {
//
//            tree.insertOrUpdate(Util.intToByteArray(i), i);
//        }
//        System.out.println("Begin order remove...");
//        long current = System.currentTimeMillis();
//        for (int j = 0; j < size; j++) {
//            if (tree.remove(Util.intToByteArray(j)) == null) {
//                System.err.println("得不到数据:" + j);
//                break;
//            }
//        }
//        long duration = System.currentTimeMillis() - current;
//        System.out.println("time elpsed for duration: " + duration);
//        System.out.println(tree.getHeight());
//    }

//    private static void testRandomRemove(int size, int order) {
//        BplusTree tree = new BplusTree(order);
//        System.out.println("\nTest random remove " + size + " datas, of order:"
//                + order);
//        Random random = new Random();
//        boolean[] a = new boolean[size + 10];
//        List<Integer> list = new ArrayList<Integer>();
//        int randomNumber = 0;
//        System.out.println("Begin random insert...");
//        for (int i = 0; i < size; i++) {
//            randomNumber = random.nextInt(size);
//            a[randomNumber] = true;
//            list.add(randomNumber);
//            tree.insertOrUpdate(randomNumber, randomNumber);
//        }
//        System.out.println("Begin random remove...");
//        long current = System.currentTimeMillis();
//        for (int j = 0; j < size; j++) {
//            randomNumber = list.get(j);
//            if (a[randomNumber]) {
//                if (tree.remove(randomNumber) == null) {
//                    System.err.println("得不到数据:" + randomNumber);
//                    break;
//                } else {
//                    a[randomNumber] = false;
//                }
//            }
//        }
//        long duration = System.currentTimeMillis() - current;
//        System.out.println("time elpsed for duration: " + duration);
//        System.out.println(tree.getHeight());
//    }
//
//    private static void testOrderSearch(int size, int order) {
//        BplusTree tree = new BplusTree(order);
//        System.out.println("\nTest order search " + size + " datas, of order:"
//                + order);
//        System.out.println("Begin order insert...");
//        for (int i = 0; i < size; i++) {
//            tree.insertOrUpdate(i, i);
//        }
//        System.out.println("Begin order search...");
//        long current = System.currentTimeMillis();
//        for (int j = 0; j < size; j++) {
//            if (tree.get(j) == null) {
//                System.err.println("得不到数据:" + j);
//                break;
//            }
//        }
//        long duration = System.currentTimeMillis() - current;
//        System.out.println("time elpsed for duration: " + duration);
//    }
//
//    private static void testRandomSearch(int size, int order) {
//        BplusTree tree = new BplusTree(order);
//        System.out.println("\nTest random search " + size + " datas, of order:"
//                + order);
//        Random random = new Random();
//        boolean[] a = new boolean[size + 10];
//        int randomNumber = 0;
//        System.out.println("Begin random insert...");
//        for (int i = 0; i < size; i++) {
//            randomNumber = random.nextInt(size);
//            a[randomNumber] = true;
//            tree.insertOrUpdate(randomNumber, randomNumber);
//        }
//        System.out.println("Begin random search...");
//        long current = System.currentTimeMillis();
//        for (int j = 0; j < size; j++) {
//            randomNumber = random.nextInt(size);
//            if (a[randomNumber]) {
//                if (tree.get(randomNumber) == null) {
//                    System.err.println("得不到数据:" + randomNumber);
//                    break;
//                }
//            }
//        }
//        long duration = System.currentTimeMillis() - current;
//        System.out.println("time elpsed for duration: " + duration);
//    }
//

//
//    private static void testOrderInsert(int size, int order) {
//        BplusTree tree = new BplusTree(order);
//        System.out.println("\nTest order insert " + size + " datas, of order:"
//                + order);
//        long current = System.currentTimeMillis();
//        for (int i = 0; i < size; i++) {
//            tree.insertOrUpdate(i, i);
//        }
//        long duration = System.currentTimeMillis() - current;
//        System.out.println("time elpsed for duration: " + duration);
//    }

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
