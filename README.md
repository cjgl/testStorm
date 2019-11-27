# zookeeper安装

######安装配置zookeeper
	#解压zookeeper
	[root@agent1 ~]$ sudo tar -zxvf zookeeper-3.4.10.tar.gz
	#把解压后的zookeeper移到目录/opt/zookeeper下
	[root@agent1 ~]$ sudo mv zookeeper-3.4.10 /opt/zookeeper
	#拷贝/opt/zookeeper/conf/simple_zoo.cfg为 zoo.cfg
	[root@agent1 ~]$ sudo cp /opt/zookeeper/conf/simple_zoo.cfg zoo.cfg
	#编辑zoo.cfg
	[root@agent1 ~]$ sudo vim /opt/zookeeper/conf/zoo.cfg
	#修改内容如下
	ticketTime=2000 #服务器之间或客户端与服务器之间维持心跳的时间间隔，每隔tickTime时间就会发送一个心跳。
	initLimit=10 #配置 Zookeeper 接受客户端（此客户端不是用户连接 Zookeeper 服务器的客户端，而是 Zookeeper 服务器集群中连接到 Leader 的 Follower 服务器）初始化连接时最长能忍受多少个心跳时间间隔数。当已超过initLimit个tickTime长度后 Zookeeper 服务器还没有收到客户端的返回信息，则表明客户端连接失败。总的时间长度就是 initLimit * tickTime 秒。
	syncLimit=5 #配置 Leader 与 Follower 之间发送消息，请求和应答时间长度，最长不能超过多少个 tickTime 的时间长度，总的时间长度就是 syncLimit * tickTime 秒。
	clientPort=2181 #Zookeeper服务器监听的端口，以接受客户端的访问请求。
	dataDir=/opt/zookeeper/data #Zookeeper 保存数据的目录，默认情况下，Zookeeper 将写数据的日志文件也保存在这个目录里。
	#dataLogDir：若没提供的话则用dataDir。zookeeper的持久化都存储在这两个目录里。dataLogDir里是放到的顺序日志(WAL)。而dataDir里放的是内存数据结构的snapshot，便于快速恢复。为了达到性能最大化，一般建议把dataDir和dataLogDir分到不同的磁盘上，以充分利用磁盘顺序写的特性。
	#server.A=B：C：D：其中 A 是一个数字，表示这个是第几号服务器；B 是这个服务器的 ip 地址；C 表示的是这个服务器与集群中的 Leader 服务器交换信息的端口；D 表示的是万一集群中的 Leader 服务器挂了，需要一个端口来重新进行选举，选出一个新的 Leader，此端口就是用来执行选举时服务器相互通信的端口。如果是伪集群的配置方式，由于 B 都是一样，所以不同的 Zookeeper 实例通信端口号不能一样，所以要给它们分配不同的端口号。2888端口是zookeeper服务相互通信使用的，3888端口是zookeeper服务选举使用的
	server.1=192.168.56.1:2888:3888
	server.2=192.168.56.2:2888:3888
	server.3=192.168.56.3:2888:3888
	
##### 配置zookeeper环境变量
	[root@apollo~]$ vim /etc/profile
	#添加如下内容
	ZOOKEEPER_HOME=/opt/zookeeper
	PATH=$PATH:ZOOKEEPER_HOME/bin
	export ZOOKEEPER_HOME

	#保存修改
	[root@apollo~]$ source /etc/profile

###### 添加myid文件
	[root@agent1 ~]# echo "1" >> /opt/zookeeper/data/myid
	[root@agent2 ~]# echo "2" >> /opt/zookeeper/data/myid
	[root@agent3 ~]# echo "3" >> /opt/zookeeper/data/myid

###### 启动zookeeper
	zkServer.sh start
	
###### 验证zookeeper各个节点状态
	zkServer.sh status

#  安装配置Storm集群
	
###### 安装Storm
	#解压
	[root@agent1 root]# tar -zxvf apache-storm-1.1.0.tar.gz 
	#移到/opt/storm下
	[root@agent1 root]# mv apache-storm-1.1.0 /opt/storm
	
###### 配置storm.yaml
	#进入$STORM_HOME目录
	[root@agent1 storm]# cd $STORM_HOME
	#创建目录
	[root@agent1 storm]# mkdir -p data
	#进入配置文件目录
	[root@agent1 storm]# cd $STORM_HOME/conf
	#修改配置文件storm.yaml
	[root@agent1 conf]# vim storm.yaml
	#修改的配置项如下
	storm.zookeeper.servers:
     - "192.168.56.1"
     - "192.168.56.2"
     - "192.168.56.3"
	
	nimbus.seeds: ["192.168.56.1"]
	
	storm.local.dir: "/opt/storm/data"
	
	ui.port: 8081

###### 启动Storm
	1.启动主控节点
	nohup $STORM_HOME/bin/storm nimbus &
	2.启动工作节点
	nohup $STORM_HOME/bin/storm supervisor &
	3.启动管理页面
	nohup $STORM_HOME/bin/storm ui &
	查看管理页面 http://172.16.1.82:8081/
	
# 运行程序
	storm jar testStorm.jar cn.fy.WordCountTopology
	
	查看类 jar xvf testStorm.jar | grep -i WordCountTopology
	
# Storm集群停止
	$ kill -9 `ps -ef | grep daemon.nimbus | awk '{print $2}' | head -n 1`
	$ kill -9 `ps -ef | grep daemon.supervisor | awk '{print $2}' | head -n 1`
	$ kill -9 `ps -ef | grep ui.core | awk '{print $2}' | head -n 1`
	$ kill -9 `ps -ef | grep daemon.logviewer | awk '{print $2}' | head -n 1`
	

