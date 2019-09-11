package PingCap.BplusTree;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
    meta索引元文件格式
    4字节 版本
    8字节 root根索引块所在索引文件的偏移
    8字节 空闲链表块所在的头节点
    其他信息
    @author zhoulikang
 */
public class Meta {
    private int version;
    private long rootPosition;
    private long emptyPosition;  // TODO 未实现 仅支持使用内存中的空闲块
    private long maxPosition;
    public Meta(int version,long rootPosition,long emptyPosition,long maxPosition){
        this.version =version;
        this.rootPosition =rootPosition;
        this.emptyPosition = emptyPosition;
        this.maxPosition =maxPosition;
    }
    public Meta(){
        this.version =1;
        this.rootPosition =0;
        this.emptyPosition = -1;
        this.maxPosition=0;
    }

    /**
     * 反序列化
     * @param dataInput
     * @throws IOException
     */
    public void read(DataInput dataInput) throws IOException{
            version = dataInput.readInt();
            rootPosition =dataInput.readLong();
            emptyPosition = dataInput.readLong();
            maxPosition=dataInput.readLong();
    }

    /**
     * 序列化
     * @param dataOutput
     * @throws IOException
     */
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(version);
        dataOutput.writeLong(rootPosition);
        dataOutput.writeLong(emptyPosition);
        dataOutput.writeLong(maxPosition);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getRootPosition() {
        return rootPosition;
    }

    public void setRootPosition(long rootPosition) {
        this.rootPosition = rootPosition;
    }

    public long getEmptyPosition() {
        return emptyPosition;
    }

    public void setEmptyPosition(long emptyPosition) {
        this.emptyPosition = emptyPosition;
    }

    public long getMaxPosition() {
        return maxPosition;
    }

    public void setMaxPosition(long maxPosition) {
        this.maxPosition = maxPosition;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "version=" + version +
                ", rootPosition=" + rootPosition +
                ", emptyPosition=" + emptyPosition +
                ", maxPosition=" + maxPosition +
                '}';
    }
}
