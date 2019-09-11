package PingCap.BplusTree;

import PingCap.Util.Config;
import PingCap.Util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
    索引块物理布局 非叶子节点
    字节地址
       0        是否为root
       1        是否为leaf 为0x00
       2～5      父节点
       6～9     关键字key 的数量n
       ***      每个关键字由 key_size（32位）和 key_value组成
       (n+1)*32 每个子节点在索引文件的偏移  每个占 64位
     索引块物理布局 叶子节点
    字节地址
       0        是否为root
       1        是否为leaf 为0x01
       2～5      父节点
       6～9      前驱节点
       10～13    后继节点
       2～5     关键字key 的数量n
       ***      每个关键字由 key_size（32位）和 key_value组成
       ***      每个关键字对应的value在数据文件的偏移 64位
 */

public class BplusNode  {
    static Logger log = Logger.getLogger("BplusNode");
    /** 是否为叶子节点 */
    protected boolean isLeaf;

    /** 是否为根节点*/
    protected boolean isRoot;

    /** 父节点 */
    protected long parent; // TODO

    /** 叶节点的前节点*/
    protected long previous;// TODO

    /** 叶节点的后节点*/
    protected long next;// TODO

    /** 叶子节点的关键字  */
    protected List<Entry<byte[], Long>> entries;

    /** 非叶子节点的子节点 */
    protected List<Long> children;
    /* 此节点对应的文件偏移地址 */
    protected long position;
    CacheManger cacheManger;
    protected int used=0;
    /**  此节点已存储内容大小 B */
    protected long bsize;

    public BplusNode(long p,boolean isLeaf, boolean isRoot,CacheManger cacheManger) {
        entries = new LinkedList<>();
        children = new LinkedList<>();
        position = p;
        this.isLeaf =isLeaf;
        this.isRoot = isRoot;
        parent=-1;
        previous=-1;
        next = -1;
        bsize+= 14; // 头部固定开销
        if(isLeaf){
            bsize+=16;
        }
        this.cacheManger=cacheManger;
    }

    public void read(DataInput dataInput) {
        try {
            int count=0;
            byte[] bytes = new byte[Config.INDEX_BLOCK_SIZE];
//            dataInput.readFully(bytes);
            isRoot =dataInput.readBoolean();
            isLeaf = dataInput.readBoolean();
            parent = dataInput.readLong();
            count+=10;
            if (isLeaf){
                previous = dataInput.readLong();
                next = dataInput.readLong();
                int len = dataInput.readInt();
                count+= 20;
                for(int i=0;i<len;i++){
                    int size = dataInput.readInt();
                    byte[] key = new byte[size];
                    dataInput.readFully(key);
                    long p = dataInput.readLong();
                    count+=4+size+8;
                    addEntries(new SimpleEntry<byte[], Long>(key,p));
                }
            }
            else {
                int len = dataInput.readInt();
                count+= 4;
                for(int i=0;i<len;i++){
                    int size = dataInput.readInt();
                    byte[] key = new byte[size];
                    dataInput.readFully(key);
                    addEntries(new SimpleEntry<byte[], Long>(key,null));
                    addChildren(dataInput.readLong());
                    count+=4+size+8;
                }
                addChildren((dataInput.readLong()));
                count+=8;
            }
            bsize=count;
        }
        catch (IOException e){

        }
    }

    public int write(DataOutput dataOutput) throws IOException{
            int l=0;
            if(bsize>Config.INDEX_BLOCK_SIZE){
                log.severe("不能写入磁盘 节点大下超过限制");
                return -1;
            }
            dataOutput.writeBoolean(isRoot);
            dataOutput.writeBoolean(isLeaf);
            dataOutput.writeLong(parent);
            l+=10;
            if(isLeaf){
                dataOutput.writeLong(previous);
                dataOutput.writeLong(next);
                dataOutput.writeInt(entries.size());
                l+=20;
                for(int i=0;i<entries.size();i++){
                    dataOutput.writeInt(entries.get(i).getKey().length);
                    dataOutput.write(entries.get(i).getKey());
                    dataOutput.writeLong(entries.get(i).getValue());
                    l+= entries.get(i).getKey().length+4+8;
                };
            }
            else {
                dataOutput.writeInt(entries.size());
                l+=4;
                for(int i=0;i<entries.size();i++){
                    dataOutput.writeInt(entries.get(i).getKey().length);
                    dataOutput.write(entries.get(i).getKey());
                    dataOutput.writeLong(children.get(i));
                    l+= entries.get(i).getKey().length+4+8;
                };
                dataOutput.writeLong(children.get(children.size()-1));
                l+=8;
            }
            return l;
    }

    public long getBsize() {
        return bsize;
    }

    public void setBsize(long bsize) {
        this.bsize = bsize;
    }

    public Long get(byte[] key) {

        //如果是叶子节点
        if (isLeaf) {
            int low = 0, high = entries.size() - 1, mid;
            int comp ;
            while (low <= high) {
                mid = (low + high) / 2;
                comp = compareKey(entries.get(mid).getKey(),key);
                if (comp == 0) {
                    return entries.get(mid).getValue();
                } else if (comp < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            //未找到所要查询的对象
            return null;
        }
        //如果不是叶子节点
        //如果key小于节点最左边的key，沿第一个子节点继续搜索
        if (compareKey(key,entries.get(0).getKey()) < 0) {
            return getNode(children.get(0)).get(key);
            //如果key大于等于节点最右边的key，沿最后一个子节点继续搜索
        }else if (compareKey(key,entries.get(entries.size()-1).getKey()) >= 0) {
            return getNode(children.get(children.size()-1)).get(key); // TODO
            //否则沿比key大的前一个子节点继续搜索
        }else {
            int low = 0, high = entries.size() - 1, mid= 0;
            int comp ;
            while (low <= high) {
                mid = (low + high) / 2;
                comp = compareKey(entries.get(mid).getKey(),key);
                if (comp == 0) {
                    return getNode(children.get(mid+1)).get(key);
                } else if (comp < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            return getNode(children.get(low)).get(key); // TODO
        }
    }
    private BplusNode getNode(long position){
       return cacheManger.getNode(position);
    }
    private BplusNode newNode(boolean isLeaf, boolean isRoot){
        return cacheManger.newNode(isLeaf,isRoot);
    }
    private void deleteNode(long position){
        cacheManger.deleteNode(position);
    }
    /*
     插入/更新节点
     */
    public void insert(byte[] key, Long value){
        used++;
        //如果是叶子节点
        if (isLeaf){
            //不需要分裂，直接插入或更新
//            if (contains(key) != -1 || entries.size() < cacheManger.tree.getOrder()){ // TODO  大小分裂
//                insertOrUpdate(key, value);
                // 确保插入后节点的大小小于索引块最大容量
            if ((bsize+key.length+8+4) < Config.INDEX_BLOCK_SIZE){
                long pre = bsize;
                long cur = (bsize+key.length+8);
                insertOrUpdate(key, value);
                if(cacheManger.tree.getHeight() == 0){
                    cacheManger.tree.setHeight(1);
                }
                used--;
                return ;
            }
            ///my
//            insertOrUpdate(key, value);
//            updateInsert();
            //需要分裂
            //分裂成左右两个节点
            long left = newNode(true,false).position;
            long right = newNode(true,false).position;
            //设置链接
            if (getNode(previous) != null){
                getNode(previous).next = left;
                getNode(left).previous = previous ;
            }
            if (getNode(next) != null) {
                getNode(next).previous = right;
                getNode(right).next = next;
            }
            if (getNode(previous )== null){
                cacheManger.tree.setHead(getNode(left));
            }
            getNode(left).next = right;
            getNode(right).previous = left;
            previous = -1;
            next = -1;

            insertOrUpdate(key, value);
//            copy2Nodes(key, value, left, right);
            //复制原节点关键字到分裂出来的新节点
            copy2Nodes(left, right);
            AdjusParent(left,right);
//            //如果不是根节点
//            if (getNode(parent) != null) {
//                //调整父子节点关系
//                int index = getNode(parent).children.indexOf(this.position);
//                getNode(parent).removeChildren(this.position);
//                getNode(left).parent = parent;
//                getNode(right).parent = parent;
//                getNode(parent).addChildren(index,left);
//                getNode(parent).addChildren(index + 1, right);
//                getNode(parent).addEntries(index,getNode(right).entries.get(0));
//                entries = null; //删除当前节点的关键字信息
//                children = null; //删除当前节点的孩子节点引用
//                //父节点插入或更新关键字
//                getNode(parent).updateInsert();
//                parent = -1; //删除当前节点的父节点引用
//                deleteNode(position); // 释放此节点空间
//                //如果是根节点
//            }else {
//                isRoot = false;
//                long parent = newNode(false, true).position;
//                cacheManger.tree.setRoot(getNode(parent));
//                getNode(left).parent = parent;
//                getNode(right).parent = parent;
//                getNode(parent).addChildren(left);
//                getNode(parent).addChildren(right);
//                getNode(parent).addEntries(getNode(right).entries.get(0));
//                entries = null;
//                children = null;
//                // 释放此节点
//                deleteNode(this.position);
//            }
            check(left);
            check(right);
        }
        else {
            //如果不是叶子节点
            //如果key小于等于节点最左边的key，沿第一个子节点继续搜索
            if (compareKey(key, entries.get(0).getKey()) < 0) {
                getNode(children.get(0)).insert(key, value);
                //如果key大于节点最右边的key，沿最后一个子节点继续搜索
            } else if (compareKey(key, entries.get(entries.size() - 1).getKey()) >= 0) {
                getNode(children.get(children.size() - 1)).insert(key, value);
                //否则沿比key大的前一个子节点继续搜索
            } else {
                int low = 0, high = entries.size() - 1, mid = 0;
                int comp;
                while (low <= high) {
                    mid = (low + high) / 2;
                    comp = compareKey(entries.get(mid).getKey(), key);
                    if (comp == 0) {
                        getNode(children.get(mid + 1)).insert(key, value);
                        break;
                    } else if (comp < 0) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                }
                if (low > high) {
                    getNode(children.get(low)).insert(key, value);
                }
            }
        }
        used--;
    }
    private void check(long p){
        if(getNode(p).bsize>Config.INDEX_BLOCK_SIZE){
            if(getNode(p).entries.size()==1){
                log.severe("叶子节点某个key太大，超过Index Block 的最大值,不能被分裂");
                return;
            }
            getNode(p).breakUp();
        }
    }
    private void AdjusParent(long left,long right){
        //如果不是根节点
        if (getNode(parent) != null) {
            //调整父子节点关系
            int index = getNode(parent).children.indexOf(this.position);
            getNode(parent).removeChildren(this.position);
            getNode(left).parent = parent;
            getNode(right).parent = parent;
            getNode(parent).addChildren(index,left);
            getNode(parent).addChildren(index + 1, right);
            getNode(parent).addEntries(index,getNode(right).entries.get(0));
            entries = null; //删除当前节点的关键字信息
            children = null; //删除当前节点的孩子节点引用
            //父节点插入或更新关键字
            getNode(parent).updateInsert();
            parent = -1; //删除当前节点的父节点引用
            deleteNode(position); // 释放此节点空间
            //如果是根节点
        }else {
            isRoot = false;
            long parent = newNode(false, true).position;
            cacheManger.tree.setRoot(getNode(parent));
            getNode(left).parent = parent;
            getNode(right).parent = parent;
            getNode(parent).addChildren(left);
            getNode(parent).addChildren(right);
            getNode(parent).addEntries(getNode(right).entries.get(0));
            entries = null;
            children = null;
            // 释放此节点
            deleteNode(this.position);
        }
    }
    private void breakUp(){
        long left = newNode(true,false).position;
        long right = newNode(true,false).position;
        //设置链接
        if (getNode(previous) != null){
            getNode(previous).next = left;
            getNode(left).previous = previous ;
        }
        if (getNode(next) != null) {
            getNode(next).previous = right;
            getNode(right).next = next;
        }
        if (getNode(previous )== null){
            cacheManger.tree.setHead(getNode(left));
        }
        getNode(left).next = right;
        getNode(right).previous = left;
        previous = -1;
        next = -1;
        //复制原节点关键字到分裂出来的新节点
        copy2Nodes(left, right);
        AdjusParent(left,right);
        check(left);
        check(right);
    }
    // 复制节点 此节点数据复制到其他两个节点上
    private void copy2Nodes(long left, long right){
        int leftSize =(entries.size()/2);
        for (int i = 0; i < entries.size(); i++) {
            if(leftSize !=0){
                leftSize --;
                getNode(left).addEntries(entries.get(i));
            }else {
                getNode(right).addEntries(entries.get(i));
            }
        }
    }

    // 此节点数据复制到其他两个节点上 并插入key
//    private void copy2Nodes(byte[] key, long value, long left,
//                            long right) {
//        //左右两个节点关键字长度
////        int leftSize = (cacheManger.tree.getOrder() + 1) / 2 + (cacheManger.tree.getOrder() + 1) % 2;
//        int leftSize =(entries.size()/2);
//        boolean b = false;//用于记录新元素是否已经被插入
//        for (int i = 0; i < entries.size(); i++) {
//            if(leftSize !=0){
//                leftSize --;
//                if(!b&&compareKey(entries.get(i).getKey(),key) > 0){
//                    getNode(left).addEntries(new SimpleEntry<byte[] , Long>(key, value));
//                    b = true;
//                    i--;
//                }else {
//                    getNode(left).addEntries(entries.get(i));
//                }
//            }else {
//                if(!b&&compareKey(entries.get(i).getKey(),key) > 0){
//                    getNode(right).addEntries(new SimpleEntry<byte[], Long>(key, value));
//                    b = true;
//                    i--;
//                }else {
//                    getNode(right).addEntries(entries.get(i));
//                }
//            }
//        }
//        if(!b){
//            getNode(right).addEntries(new SimpleEntry<byte[], Long>(key, value));
//        }
//    }


    /** 插入节点后中间节点的更新  分裂*/
    protected void updateInsert(){
        used++;
        //如果子节点数超出阶数，则需要分裂该节点
//        if (children.size() > cacheManger.tree.getOrder()) {
        // 修改 当子节点大超过限制则分裂
        if (bsize > Config.INDEX_BLOCK_SIZE) {
            //分裂成左右两个节点
            if(children.size()<=3){
                log.severe("非叶子节点中某个key太大，超过Index Block 的最大值,不能被分裂");
            }
            long left = newNode(false,false).position;
            long right = newNode(false,false).position;
            //左右两个节点子节点的长度
//            int leftSize = (cacheManger.tree.getOrder() + 1) / 2 + (cacheManger.tree.getOrder() + 1) % 2;
//            int rightSize = (cacheManger.tree.getOrder() + 1) / 2;
            int leftSize = (children.size()) / 2 + (children.size()) % 2;
            int rightSize = (children.size() ) / 2;
            //复制子节点到分裂出来的新节点，并更新关键字
            for (int i = 0; i < leftSize; i++){
                getNode(left).addChildren(children.get(i));
                getNode(children.get(i)).parent = left;
            }
            for (int i = 0; i < rightSize; i++){
                getNode(right).addChildren(children.get(leftSize + i));
                getNode(children.get(leftSize + i)).parent = right;
            }
            for (int i = 0; i < leftSize - 1; i++) {
                getNode(left).addEntries(entries.get(i));
            }
            for (int i = 0; i < rightSize - 1; i++) {
                getNode(right).addEntries(entries.get(leftSize + i));
            }
            //如果不是根节点
            if (parent != -1) {
                //调整父子节点关系
                int index = getNode(parent).children.indexOf(this.position);
                getNode(parent).removeChildren(this.position);
                getNode(left).parent = parent;
                getNode(right).parent = parent;
                getNode(parent).addChildren(index,left);
                getNode(parent).addChildren(index + 1, right);
                getNode(parent).addEntries(index,entries.get(leftSize - 1));
                entries = null;
                children = null;
                deleteNode(this.position);
                //父节点更新关键字
                getNode(parent).updateInsert();
                parent = -1;
                //如果是根节点
            }else {
                isRoot = false;
                long parent = newNode(false, true).position;
                cacheManger. tree.setRoot(getNode(parent));
                cacheManger.tree.setHeight(cacheManger.tree.getHeight() + 1);
                getNode(left).parent = parent;
                getNode(right).parent = parent;
                getNode(parent).addChildren(left);
                getNode(parent).addChildren(right);
                getNode(parent).addEntries(entries.get(leftSize - 1));
                entries = null;
                children = null;
                deleteNode(this.position);
            }
            getNode(left).updateInsert();
            getNode(right).updateInsert();
        }
        used--;
    }

    /** 判断当前节点是否包含该关键字*/
    protected int contains(byte[] key) {
        int low = 0, high = entries.size() - 1, mid;
        int comp ;
        while (low <= high) {
            mid = (low + high) / 2;
            comp = compareKey(entries.get(mid).getKey(),key);
            if (comp == 0) {
                return mid;
            } else if (comp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /** 插入到当前节点的关键字中*/
    protected void insertOrUpdate(byte[] key, long value){
        used++;
        //二叉查找，插入
        int low = 0, high = entries.size() - 1, mid;
        int comp ;
        while (low <= high) {
            mid = (low + high) / 2;
            comp = compareKey(entries.get(mid).getKey(),key);
            if (comp == 0) {
                entries.get(mid).setValue(value);
                break;
            } else if (comp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if(low>high){
            addEntries(low, new SimpleEntry<byte[], Long>(key, value));
        }
        used--;
    }

    public void addEntries(Entry<byte[], Long> t) {
        addEntries(entries.size(),t);
    }
    public void addEntries(int index,Entry<byte[], Long> t) {
        entries.add(index,t);

        if(isLeaf) {
            bsize += t.getKey().length + 4+8;
        }
        else {
            bsize+=t.getKey().length+4;
        }
    }

    public void removeEntries(Entry<byte[], Long> t) {
        entries.remove(t);
//        bsize= bsize-(t.getKey().length+4);
        if(isLeaf) {
            bsize -= t.getKey().length + 4+8;
        }
        else {
            bsize-= t.getKey().length+4;
        }
    }

    public void addChildren(long l) {
        addChildren(this.children.size(),l);
    }
    public void addChildren(int index,long l) {

        this.children.add(index,l);

        if(!isLeaf)
            bsize+=8;
    }
    public void removeChildren(long l) {
        this.children.remove(l);
        if(!isLeaf)
            bsize-=8;

    }

    /** TODO  删除节点 */
    protected long remove(byte[] key){
        return 0;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("isRoot: ");
        sb.append(isRoot);
        sb.append(", ");
        sb.append("isLeaf: ");
        sb.append(isLeaf);
        sb.append(", ");
        sb.append("keys: ");
        for (Entry<byte[],Long> entry : entries){
            sb.append(entry.getKey());
            sb.append(", ");
        }
        sb.append(", ");
        return sb.toString();

    }
    int compareKey(byte[] a,byte[] b){
       return Util.compareKey(a,b);
    }
    public int realSize(){
        return 0;
    }
}
