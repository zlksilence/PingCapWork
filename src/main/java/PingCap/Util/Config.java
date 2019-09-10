package PingCap.Util;

public class Config {

    private Config(){

    }
    // key size 必须小于 块大小的一半
    static public int INDEX_BLOCK_SIZE = 2*4*1024;

}
