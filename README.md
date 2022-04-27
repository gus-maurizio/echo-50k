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

```

Client:
```
export JAVA_HOME=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home
export PATH=/usr/local/bin/openjdk/jdk-19.jdk/Contents/Home/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/python@3.10/bin:/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

echo-50k % java --enable-preview -ea -cp target/conn-scale-1.0.0.jar conn50k.EClient 127.0.0.1 9000 5000

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


