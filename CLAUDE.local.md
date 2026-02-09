# 项目指南

!important: 不要使用ssh命令，要使用ssh-mcp mcp代替

## 开发环境启动项目

mysql和redis服务在服务器192.168.100.100（dev）的docker容器中，本地后端需要连接他们

服务器：192.168.100.100 root/123

mysql数据库有读写分离，需要两个mysql

先检查mysql和redis服务是否已启动、可用

本地启动后端：http://localhost:8080

用户前端：http://localhost:3000

后台管理前端：http://localhost:3001

如果指定端口被占用，先杀掉占用端口的进程

## 工具

- 操作远程服务器时，使用ssh-mcp mcp

## 测试账号

- 后台管理管理员
    admin/admin123