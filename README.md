# Connect 50K+

Tests to achieve 500,000 persistent connections each in client and server
Java application using [virtual threads](https://openjdk.java.net/projects/loom/). [download virtual threads java 19 loom](https://jdk.java.net/19/)

export JAVA_HOME=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home
export PATH=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

https://wiki.openjdk.java.net/pages/viewpage.action?pageId=64094243

# Components

The project consists of two simple components, `EServer` and `EClient`.

`EServer` creates TCP passive server sockets, accepting new connections on each as they come in.
For each active socket created, `EServer` receives bytes in and echoes them back out.

`EClient` initiates outgoing TCP connections to a range of ports on a single destination server.
For each socket created, `EClient` sends a message to the server, awaits the response, and goes to sleep
for a time before sending again.

`EClient` terminates immediately if any of the follow occurs:
* Connection timeout
* Socket read timeout
* Integrity error with message received
* TCP connection closure
* TCP connection reset
* Any other unexpected I/O condition


`/etc/sysctl.conf`:
```
fs.file-max=10485760
fs.nr_open=10485760

net.core.default_qdisc=fq
net.core.somaxconn=16192
net.core.netdev_max_backlog=16192
net.core.rmem_max=16777216
net.core.wmem_max=16777216
net.core.rmem_default=16777216
net.core.wmem_default=16777216

net.ipv4.tcp_congestion_control=bbr
net.ipv4.tcp_max_syn_backlog=16192
net.ipv4.ip_local_port_range=1024 65535

net.ipv4.tcp_rmem=4096 87380 16777216
net.ipv4.tcp_wmem=4096 87380 16777216
net.ipv4.tcp_mem=1638400 1638400 1638400

net.netfilter.nf_conntrack_buckets=1966050
net.netfilter.nf_conntrack_max=7864200

#EC2 Amazon Linux
net.core.netdev_max_backlog=65536
net.core.optmem_max=25165824
net.ipv4.tcp_max_tw_buckets=1440000
net.ipv4.tcp_slow_start_after_idle=0
net.ipv4.tcp_tw_reuse=1 
net.ipv4.tcp_abort_on_overflow=1 
net.ipv4.conf.all.route_localnet=1 ```

`/etc/security/limits.conf`:
```
* soft nofile 8000000
* hard nofile 9000000
```


Server:
```
export JAVA_HOME=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home
export PATH=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

mvn clean package
echo-50k % java --enable-preview -ea -cp target/conn-scale-1.0.0.jar conn50k.EServer 0.0.0.0 9000 50 16192 16

Args[host=0.0.0.0, port=9000, portCount=50, backlog=16192, bufferSize=16]
[0] connections=0, messages=0
[1013] connections=0, messages=0
[2018] connections=0, messages=0
[3024] connections=0, messages=0
[4030] connections=0, messages=0
[5035] connections=0, messages=0
[6041] connections=0, messages=0
[7046] connections=0, messages=0
[8052] connections=0, messages=0
```

Client:
```
export JAVA_HOME=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home
export PATH=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

echo-50k % java --enable-preview -ea -cp target/conn-scale-1.0.0.jar conn50k.EClient 127.0.0.1 9000 5000
Args[host=127.0.0.1, port=9000, portCount=5000, numConnections=1, socketTimeout=10000, warmUp=1000, sleep=1000]
[0] connections=0, messages=0
[1012] connections=4982, messages=4982
[2017] connections=5000, messages=9993
[3023] connections=5000, messages=15035
[4028] connections=5000, messages=20042
[5034] connections=5000, messages=25052
[6037] connections=5000, messages=30052
[7043] connections=5000, messages=35052
[8048] connections=5000, messages=40052
[9049] connections=5000, messages=45052
[10051] connections=5000, messages=50052
[11052] connections=5000, messages=55052

```

## EC2

EC2 instances:

* c5.2xlarge
* 16GB RAM
* 8 vCPU
* Amazon Linux 2 with Linux Kernel 5.10
* Amazon Corretto 17 or better (openjdk 19 with loom)

The server launched with a passive server port range of [9000, 9049].
The client launched with the same server target port range and a connections-per-port count of 10,000, for a total of 500,000 target connections.


```
$ ./jdk-19/bin/java --enable-preview -ea -cp conn-scale-1.0.0.jar conn50k.EServer 0.0.0.0 9000 50 16192 16
Args[host=0.0.0.0, port=9000, portCount=50, backlog=16192, bufferSize=16]
[0] connections=0, messages=0
[1015] connections=0, messages=0
...
[17020] connections=2412, messages=2412
[18020] connections=10317, messages=10316
...
[2106644] connections=500000, messages=17414982
[2107645] connections=500000, messages=17423544
```

```
[ec2-user@ip-10-39-196-215 ~]$ ./jdk-19/bin/java --enable-preview -ea -cp conn-scale-1.0.0.jar conn50k.EClient 10.39.197.143 9000 50 10000 30000 60000 60000
Args[host=10.39.197.143, port=9000, portCount=50, numConnections=10000, socketTimeout=30000, warmUp=60000, sleep=60000]
[0] connections=131, messages=114
[1014] connections=4949, messages=4949
[2014] connections=13248, messages=13246
...
[2091751] connections=500000, messages=17428105
[2092751] connections=500000, messages=17432046
```

---

The `ss` command reflects that both client and server had 500,000+ sockets open.

Server:
```
[ec2-user@ip-10-39-197-143 ~]$ ss -s
Total: 500233 (kernel 0)
TCP:   500057 (estab 500002, closed 0, orphaned 0, synrecv 0, timewait 0/0), ports 0

Transport Total     IP        IPv6
*	  0         -         -        
RAW	  0         0         0        
UDP	  8         4         4        
TCP	  500057    5         500052   
INET	  500065    9         500056   
FRAG	  0         0         0 
```

Client:
```
[ec2-user@ip-10-39-196-215 ~]$ ss -s
Total: 500183 (kernel 0)
TCP:   500007 (estab 500002, closed 0, orphaned 0, synrecv 0, timewait 0/0), ports 0

Transport Total     IP        IPv6
*	  0         -         -        
RAW	  0         0         0        
UDP	  8         4         4        
TCP	  500007    5         500002   
INET	  500015    9         500006   
FRAG	  0         0         0 
```

---

The server Java process used 2.3 GB of committed resident memory and 8.4 GB of virtual memory.
After running for 35.12m, it used 14m42s of CPU time.

The client Java process used 2.8 GB of committed resident memory and 8.9 GB of virtual memory.
After running for 34.88m, it used 25m19s of CPU time.


Server:
```
COMMAND                                                                                              %CPU     TIME   PID %MEM   RSS    VSZ
./jdk-19/bin/java --enable-preview -ea -cp conn-scale-1.0.0.jar conn50k.EServer 36.7 00:12:42 18432 14.5 2317180 8434320
```

Client:
```
COMMAND                                                                                              %CPU     TIME   PID %MEM   RSS    VSZ
./jdk-19/bin/java --enable-preview -ea -cp conn-scale-1.0.0.jar conn50k.EClient 73.9 00:25:19 18120 17.9 2848356 8901320
```
