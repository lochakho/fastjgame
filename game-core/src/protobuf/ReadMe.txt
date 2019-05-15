这里存放的游戏服务器之间通信调用的proto文件，以及工具。
proto文件生成的java类，和工具生成的枚举类，其实可以添加到忽略文件，每次编译前先运行进行生成。

1.运行 protofiles下的generateproto.bat 导出java类文件
2.运行mappingtools下的generateMessageId.bat 导出枚举文件(方便映射)