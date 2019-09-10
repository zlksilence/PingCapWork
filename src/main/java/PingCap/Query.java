package PingCap;

import java.util.List;

public interface Query {
    void insert (byte[] key,byte[] value);
    byte[] find(byte[] key);
    void update(byte[]key,byte[]value);
    void delete(byte[]key);
    void insertBatch(List<byte[]> keys, List<byte[]> values);
    List<byte[]> findRange(byte[] start,byte[] end);
    void close();
}
