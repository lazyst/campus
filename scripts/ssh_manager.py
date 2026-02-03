#!/usr/bin/env python3
"""
SSH Manager - SSH连接、命令执行和文件传输工具
"""
# type: ignore

import os
import stat
from dataclasses import dataclass
from typing import Optional
import paramiko


@dataclass
class CommandResult:
    """命令执行结果"""

    stdout: str
    stderr: str
    return_code: int
    success: bool


class SSHError(Exception):
    """SSH操作错误"""

    pass


class SSHManager:
    """SSH连接管理器"""

    def __init__(
        self, host: str, username: str, password: str, port: int = 22, timeout: int = 30
    ):
        """
        初始化SSH管理器

        Args:
            host: 服务器地址
            username: 用户名
            password: 密码
            port: SSH端口（默认22）
            timeout: 连接超时时间（默认30秒）
        """
        self.host = host
        self.username = username
        self.password = password
        self.port = port
        self.timeout = timeout
        self.client: Optional[paramiko.SSHClient] = None
        self.sftp: Optional[paramiko.SFTPClient] = None

    def connect(self) -> None:
        """建立SSH连接"""
        try:
            self.client = paramiko.SSHClient()
            self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            self.client.connect(
                hostname=self.host,
                port=self.port,
                username=self.username,
                password=self.password,
                timeout=self.timeout,
            )
            # 初始化SFTP
            self.sftp = self.client.open_sftp()
        except paramiko.AuthenticationException as e:
            raise SSHError(f"认证失败: {str(e)}")
        except paramiko.SSHException as e:
            raise SSHError(f"SSH错误: {str(e)}")
        except Exception as e:
            raise SSHError(f"连接失败: {str(e)}")

    def execute_command(self, command: str) -> CommandResult:
        """在远程服务器上执行命令"""
        if self.client is None:
            self.connect()

        assert self.client is not None, "SSH客户端连接失败"
        client = self.client
        try:
            stdin, stdout, stderr = client.exec_command(command, timeout=self.timeout)
            stdout_content = stdout.read().decode("utf-8")
            stderr_content = stderr.read().decode("utf-8")
            return_code = stdout.channel.recv_exit_status()

            return CommandResult(
                stdout=stdout_content,
                stderr=stderr_content,
                return_code=return_code,
                success=(return_code == 0),
            )
        except Exception as e:
            raise SSHError(f"命令执行失败: {str(e)}")

    def _mkdir_p(self, remote_path: str) -> None:
        """递归创建远程目录"""
        # 规范化路径
        remote_path = remote_path.replace("\\", "/")

        # 确保sftp已初始化
        if self.sftp is None:
            if self.client is None:
                self.connect()
            self.sftp = self.client.open_sftp()

        # 分割路径
        parts = remote_path.split("/")
        current = ""

        for part in parts:
            if part:
                current = current + "/" + part if current else "/" + part
                try:
                    self.sftp.stat(current)
                except:
                    try:
                        self.sftp.mkdir(current)
                    except:
                        pass

    def upload_file(self, local_path: str, remote_path: str) -> None:
        """上传单个文件到服务器"""
        if not os.path.exists(local_path):
            raise SSHError(f"本地文件不存在: {local_path}")

        if self.client is None:
            self.connect()

        assert self.client is not None, "SSH客户端连接失败"
        try:
            if self.sftp is None:
                self.sftp = self.client.open_sftp()
            sftp = self.sftp

            # 规范化远程路径（使用正斜杠）
            remote_path = remote_path.replace("\\", "/")

            # 远程目录处理（使用Linux风格）
            remote_dir = remote_path.rsplit("/", 1)[0] if "/" in remote_path else ""
            if remote_dir:
                try:
                    sftp.stat(remote_dir)
                except:
                    # 目录不存在，创建它
                    self._mkdir_p(remote_dir)

            sftp.put(local_path, remote_path)
        except Exception as e:
            raise SSHError(f"文件上传失败: {str(e)}")

    def upload_folder(self, local_folder: str, remote_folder: str) -> None:
        """上传整个文件夹到服务器"""
        # 规范化远程路径（Windows转Linux）
        remote_folder = remote_folder.replace("\\", "/")

        if not os.path.exists(local_folder):
            raise SSHError(f"本地文件夹不存在: {local_folder}")

        if not os.path.isdir(local_folder):
            raise SSHError(f"本地路径不是文件夹: {local_folder}")

        if self.client is None:
            self.connect()

        assert self.client is not None, "SSH客户端连接失败"
        try:
            if self.sftp is None:
                self.sftp = self.client.open_sftp()
            sftp = self.sftp

            # 创建远程文件夹
            try:
                sftp.mkdir(remote_folder)
            except OSError:
                pass  # 文件夹可能已存在

            # 遍历本地文件夹
            for item in os.listdir(local_folder):
                local_item = os.path.join(local_folder, item)
                # 规范化远程路径
                remote_item = remote_folder + "/" + item

                if os.path.isdir(local_item):
                    self.upload_folder(local_item, remote_item)
                else:
                    self.upload_file(local_item, remote_item)

        except Exception as e:
            raise SSHError(f"文件夹上传失败: {str(e)}")

    def download_file(self, remote_path: str, local_path: str) -> None:
        """从服务器下载单个文件"""
        if self.client is None:
            self.connect()

        assert self.client is not None, "SSH客户端连接失败"
        try:
            if self.sftp is None:
                self.sftp = self.client.open_sftp()
            sftp = self.sftp

            # 规范化路径
            remote_path = remote_path.replace("\\", "/")

            # 检查远程文件是否存在
            try:
                sftp.stat(remote_path)
            except FileNotFoundError:
                raise SSHError(f"远程文件不存在: {remote_path}")

            # 确保本地目录存在
            local_dir = os.path.dirname(local_path)
            if local_dir:
                os.makedirs(local_dir, exist_ok=True)

            sftp.get(remote_path, local_path)
        except SSHError:
            raise
        except Exception as e:
            raise SSHError(
                f"文件下载失败: {str(e)} (远程: {remote_path} -> 本地: {local_path})"
            )

    def download_folder(self, remote_folder: str, local_folder: str) -> None:
        """从服务器下载整个文件夹"""
        # 规范化远程路径
        remote_folder = remote_folder.replace("\\", "/")

        if self.client is None:
            self.connect()

        assert self.client is not None, "SSH客户端连接失败"
        try:
            if self.sftp is None:
                self.sftp = self.client.open_sftp()
            sftp = self.sftp

            # 创建本地文件夹
            os.makedirs(local_folder, exist_ok=True)

            # 列出远程文件夹内容
            for item in sftp.listdir(remote_folder):
                remote_item = remote_folder + "/" + item
                local_item = os.path.join(local_folder, item)

                try:
                    # 检查是否为文件夹
                    stat_result = sftp.stat(remote_item)
                    mode = stat_result.st_mode
                    if stat.S_ISDIR(mode):
                        self.download_folder(remote_item, local_item)
                    else:
                        self.download_file(remote_item, local_item)
                except:
                    # 如果不是文件夹，按文件处理
                    self.download_file(remote_item, local_item)

        except Exception as e:
            raise SSHError(f"文件夹下载失败: {str(e)}")

    def close(self) -> None:
        """关闭SSH连接"""
        if self.sftp:
            self.sftp.close()
            self.sftp = None
        if self.client:
            self.client.close()
            self.client = None

    def __enter__(self):
        """上下文管理器入口"""
        self.connect()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """上下文管理器退出"""
        self.close()
        return False


def execute_ssh_task(
    host: str,
    username: str,
    password: str,
    command: str,
    port: int = 22,
    timeout: int = 30,
) -> CommandResult:
    """便捷函数：执行单个SSH任务"""
    manager = SSHManager(host, username, password, port, timeout)
    try:
        return manager.execute_command(command)
    finally:
        manager.close()


def batch_execute(servers: list, command: str, timeout: int = 30) -> list:
    """批量在多个服务器上执行命令"""
    results = []
    manager = None
    for server in servers:
        try:
            manager = SSHManager(
                host=server["host"],
                username=server["username"],
                password=server["password"],
                timeout=timeout,
            )
            result = manager.execute_command(command)
            results.append(
                {"server": server["host"], "success": result.success, "result": result}
            )
        except SSHError as e:
            results.append(
                {"server": server["host"], "success": False, "error": str(e)}
            )
        finally:
            if manager is not None and manager.client is not None:
                manager.close()

    return results


# 示例使用
if __name__ == "__main__":
    print("SSH Manager示例")
    print("=" * 50)

    # 示例1: 基本连接和命令执行
    print("\n示例1: 连接并执行命令")
    try:
        manager = SSHManager(host="192.168.1.100", username="user", password="pass")
        result = manager.execute_command("echo 'Hello from server'")
        print(f"输出: {result.stdout}")
        print(f"返回码: {result.return_code}")
        manager.close()
    except SSHError as e:
        print(f"错误: {e}")

    # 示例2: 使用上下文管理器
    print("\n示例2: 使用上下文管理器")
    try:
        with SSHManager(host="192.168.1.100", username="user", password="pass") as mgr:
            result = mgr.execute_command("pwd")
            print(f"当前目录: {result.stdout}")
    except SSHError as e:
        print(f"错误: {e}")

    # 示例3: 批量执行
    print("\n示例3: 批量执行命令")
    servers = [
        {"host": "192.168.1.100", "username": "user", "password": "pass"},
        {"host": "192.168.1.101", "username": "user", "password": "pass"},
    ]
    results = batch_execute(servers, "whoami")
    for res in results:
        print(f"服务器 {res['server']}: {res['success']}")
