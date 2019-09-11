# 使用说明

## 代码结构

整个项目是Maven工程


BplusTree 包：存放B+树相关实现

Data 包：数据文件的实现 （通过指针访问key和value）

Util 包：一些配置信息和公共的方法

QueryIml 类 ： 查询操作的实现 

test测试：
		DataTest 类： 数据文件测试，测试生成数据文件

​		TreeTest 类： B+树索引的相关测试

​		TestMain 类 ： 主功能，用b+树所以测试查询数据文件的key value

## 使用说明：

1. 在DataTest 调用productRandomData() 生成数据文件 datafile
2. 在TestMain 调用prePare() 预处理datafile，生成indexfile和metafile
3. 在TestMain 调用testQueryM()，根据indexfile，metafile和datafile查询数据

## B+树说明

B+树：
此B+树存放与磁盘上，每个Node节点通过**指针**（**索引文件偏移量**）来访问其他节点

节点的前驱、后继和父节点等指针 都是通过文件的偏移量来实现

叶子节点通过**指针**（**数据文件的偏移量**）来访问此key所对应的value

不同与普通B+树，此B+树Node的大小大于 NDXEX_BLOCK_SIZE(默认 8kb) 后将发生分裂，
以此来保证每个节点落盘时大小都不会超过 INDXEX_BLOCK_SIZE(默认 8kb)

B+树叶子节点存放key和 value ，value是此key在 **数据文件** 的偏移量
B+树的非叶子节点存放key 和 子节点的指针

B+树 暂仅支持**查询、更新、和插入**，不支持删除操作

**通过LRU算法来缓存B+树的Node节点**

## 数据文件说明

生成的数据文件内部都是由无数个key value组成，每个存储格式依次为：key_size(4字节)、key，value_size（4字节）、value。key的大小限制不能超过索引块的大小（因为要通过key来建立B+树），value的大小没有限制。

## 数据预处理

即通过顺序扫描**数据文件**，对扫描到的每一个key，插入到B+树的索引文件中。生成的B+树索引存放在indexfile文件里，为来查找效率内存缓存（大约3G）里一些B+树的Node节点。**预处理过程中数据文件内容不变** 。

