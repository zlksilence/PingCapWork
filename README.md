# 使用说明

## 代码结构


BplusTree 包：存放B+树相关实现

Data 包：数据文件的实现 

Util 包：一些配置信息和公共的方法
QueryIml 类 ： 查询操作的实现 

test测试：
		DataTest 类： 数据文件测试，测试生成数据文件

​		TreeTest 类： B+树索引的相关测试

​		TestMain 类 ： 主功能，测试用b+树查询数据文件的key value

## 使用说明：

1. 在DataTest 调用productRandomData() 生成数据文件 datafile
2. 在TestMain 调用prePare() 预处理datafile，生成indexfile和metafile
3. 在TestMain 调用testQueryM()，根据indexfile，metafile和datafile查询数据

## B+树说明

B+树：
此B+树存放与磁盘上，每个Node节点通过**索引文件**偏移量来确定位置指针并表示此Node

节点的前驱、后继和父节点等指针 都是通过文件的偏移量来实现

不同与普通B+树，此B+树Node的大小大于 NDXEX_BLOCK_SIZE(默认 8kb) 后将发生分裂，
以此来保证每个节点落盘时大小都不会超过 INDXEX_BLOCK_SIZE(默认 8kb)

B+树叶子节点存放key和 value ，value是此key在 《数据文件》 的偏移量
B+树的非叶子节点存放key 和 子节点的指针

B+树 暂仅支持**查询、更新、和插入**，不支持删除操作

**通过LRU算法来缓存B+树的Node节点**


