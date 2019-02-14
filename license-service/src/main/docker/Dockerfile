FROM java:8
# 定义一个虚拟的文件夹，发生写操作是不会都不会记录进容器的存储层中
# 当然在启动镜像时是可以映射改匿名的文件夹的
# docker run -d -v myxxx:tmp   ==> myxxx 将会映射匿名的 tmp
# 将主机的 myxxx 目录，映射 tmp 目录
VOLUME /tmp
# arg 命令可以用于接受构建docker镜像时接受命令行传递过来的参数
ARG JAR_FILE
# add 复制功能 该命令将命令行传递过来的 jar程序名称改为 app 然后在 entrypoint 中指向执行
ADD ${JAR_FILE} app.jar
# 执行 shell 命令 bash -c 从后面的字符串中读取命令
RUN bash -c 'touch /app.jar'
# 设置时区
RUN echo "Asia/Shanghai" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata
# 暴露端口用户外部交互
EXPOSE 8080
# entrypoint 还可以继续获取docker run 执行是后添加的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
