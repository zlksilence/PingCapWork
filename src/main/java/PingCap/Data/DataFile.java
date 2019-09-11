package PingCap.Data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import PingCap.Util.Util;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

/**
 * 数据文件
 * @author
 */
public class DataFile {
    static Logger log = Logger.getLogger("DataFile");
    RandomAccessFile dataFile;
    long fileLen;
    public DataFile(String dataName) throws IOException {
        log.log(Level.INFO,"opening data file");
        dataFile = new RandomAccessFile(dataName,"r");
        fileLen=dataFile.length();
    }

    //  not safe

    /**
     *  根据偏移p 查找 value
     * @param p
     * @param key
     * @return
     */
   public synchronized   byte[] find(long p,byte[] key)  {
        try {
            dataFile.seek(p);
            byte[] k = Util.readKey(dataFile);
            if ( Util.compareKey(key,k) ==0) {
                byte[] value = Util.readValue(dataFile);
                return value;
            } else {
                return null;
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件追加
     * @param key
     * @param value
     * @return
     * @throws IOException
     */
    public synchronized   long append (byte[] key,byte[] value) throws IOException {
        long position=fileLen;
        dataFile.seek(position);
        Util.writeKey(dataFile,key);
        Util.writeValue(dataFile,value);
        fileLen+=8+key.length+value.length;
        return position;
    }

    public void close() throws IOException{
        dataFile.close();
    }

}
