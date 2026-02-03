# -*- coding: utf-8 -*-
"""
数据库初始化脚本
连接到192.168.100.100服务器，上传init.sql并初始化数据库
"""

from ssh_manager import SSHManager

def main():
    host = "192.168.100.100"
    username = "root"
    password = "123"
    local_file = r"D:\develop\campus-fenbushi\mysql\init.sql"
    remote_file = "/app/campus/mysql/init.sql"

    print("=" * 60)
    print("数据库初始化任务开始")
    print("=" * 60)

    with SSHManager(host=host, username=username, password=password) as mgr:
        # 步骤1: 创建远程目录
        print("\n[1/5] 创建远程目录...")
        mgr.execute_command("mkdir -p /app/campus/mysql")
        print("   目录创建成功: /app/campus/mysql")

        # 步骤2: 上传文件
        print("\n[2/5] 上传 init.sql 文件...")
        mgr.upload_file(local_file, remote_file)
        print(f"   文件上传成功: {local_file} -> {remote_file}")

        # 步骤3: 执行数据库初始化
        print("\n[3/5] 执行数据库初始化...")
        # 获取DB_PASSWORD
        result = mgr.execute_command("docker exec campus-mysql printenv DB_PASSWORD")
        db_password = result.stdout.strip()
        print(f"   DB_PASSWORD: {db_password}")

        # 使用 docker exec -i 直接导入SQL文件
        import_cmd = f"docker exec -i campus-mysql mysql -uroot -p{db_password} < {remote_file}"
        print(f"   执行命令: {import_cmd[:50]}...")
        mgr.execute_command(import_cmd)
        print("   SQL文件导入成功!")

        # 步骤4: 验证表已创建
        print("\n[4/5] 验证数据库表...")
        verify_cmd = f"docker exec campus-mysql mysql -uroot -p{db_password} -e \"USE campus_fenbushi; SHOW TABLES;\""
        result = mgr.execute_command(verify_cmd)
        print("   表列表:")
        for line in result.stdout.strip().split('\n'):
            print(f"      {line}")

        # 步骤5: 验证数据插入
        print("\n[5/5] 验证数据插入...")

        # 验证 post 表
        post_cmd = f"docker exec campus-mysql mysql -uroot -p{db_password} -e \"SELECT COUNT(*) AS post_count FROM post;\""
        result = mgr.execute_command(post_cmd)
        print(f"   post 表记录数:")
        for line in result.stdout.strip().split('\n'):
            if 'COUNT' in line or 'post_count' in line or line.strip().isdigit():
                print(f"      {line}")

        # 验证 item 表
        item_cmd = f"docker exec campus-mysql mysql -uroot -p{db_password} -e \"SELECT COUNT(*) AS item_count FROM item;\""
        result = mgr.execute_command(item_cmd)
        print(f"   item 表记录数:")
        for line in result.stdout.strip().split('\n'):
            if 'COUNT' in line or 'item_count' in line or line.strip().isdigit():
                print(f"      {line}")

    print("\n" + "=" * 60)
    print("数据库初始化任务完成!")
    print("=" * 60)

if __name__ == "__main__":
    main()
