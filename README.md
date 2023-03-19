
## lftp 文件上传
brew install lftp
```shell
mvn clean package -Dmaven.test.skip=true
sftp root@youip <<EOF
put ./target/openai-demo*.jar /opt/openai-demo/
bye
EOF
```

# SSL配置
参考文档：https://blog.csdn.net/rakish_wind/article/details/115840424
免费SSL证书：
https://freessl.cn/acme-deploy?domains=openai.okeeper.com

# 设置Clash代理
https://i.jakeyu.top/2021/11/27/centos-%E4%BD%BF%E7%94%A8-Clash-%E6%A2%AF%E5%AD%90/

https://www.jianshu.com/p/1702a352797d

后台启动：nohup ./clash >out.log 2>&1 &


# prompt
https://github.com/PlexPt/awesome-chatgpt-prompts-zh

https://github.com/f/awesome-chatgpt-prompts