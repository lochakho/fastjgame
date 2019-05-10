### 为什么game-net包要单独作为模块呢？
+ game-net是通用逻辑，与做什么游戏没有太大关系，多数游戏类型游戏都可以用的。

### game-net使用的主要框架
+ IO框架 Netty [传送门](https://github.com/netty/netty) 
+ 并发框架 LMAX Disruptor [传送门](https://github.com/LMAX-Exchange/disruptor),
如果大家要学习disruptor的话，给大家推荐一下我加完注释的项目
[disruptor-translation](https://github.com/hl845740757/disruptor-translation)。

### game-net支持的协议
+ tcp 用于pc端建立长链接
+ websocket 用于移动端建立长链接
+ http 主要服务于后台工具

### game-net天然支持的序列化方式
+ Json 使用Google的Gson
+ ProtoBuf

### game-net支持自定义的序列化方式
+ 如果你想使用自定义的序列化方式，你需要实现 **MessageSerializer** 接口。
+ 请仔细它的文档，以确保不会产生线程安全问题。

### game-net是如何识别消息的？
+ 它基于消息映射，每一个消息(class)都有一个对应的messageId(int)。
+ 如果你需要自定义消息映射方式，你需要继承 **MessageMappingStrategy** 在初始化方法中完成所有映射。
+ 在Example的示例中使用的基于hash值的方式。
+ 可参考[netty-protoBuf解决方案](https://github.com/hl845740757/netty-any-protobuf)


### game-net支持断线重连
+ game-net的断线重连是基于tcp实现的，基于tcp会更加简单。使用udp的话有太多复杂逻辑。
+ java服务器之间的断线重连是完整的(java版的双方是完整的)，需要能看懂java代码，将其翻译为你项目使用的客户端语言。
+ 收发协议

### 理解game-net的断线重连可参考：
+ TCP3次握手
+ 滑动窗口协议
+ go-back-n协议
+ doc文件夹下的 异步调用协议字段说明.txt

### 如何启动测试？
+ 查看Main类的文档。
+ 将类文档中的ip地址改为服务器ip
+ 将参数复制到启动参数中即可。

+ 在main方法中初始化日志路径 logback.xml中的使用 logFilePath 系统属性
+ 创建一个NetBootstrap实例，设置各种参数
+ 调用NetBootstrap实例的start()方法。
+ 如果更多的测试，请查看Main类的文档

### 线程安全是如何实现的？
+ 基于特定的线程启动顺序实现的。
+ Main线程在初始化完所有的参数后，调用NetBootstrap实例的start()方法才会启动游戏线程(WorldThread)。
+ 游戏线程(WorldThread)初始化完IO线程需要的各种属性之后，才会启动Netty的IO线程。见 World.onStart()方法
+ 线程模型见doc文件夹下的 线程模型.png