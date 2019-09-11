package PingCap.Util;

public class Config {

    private Config(){

    }
    // key size 必须小于 块大小的一半
    // 索引数据块即B=树 Nod 节大小  超过此大小 Node节点将发生分裂
    static public int INDEX_BLOCK_SIZE = 2*4*1024; // 8kb

}
