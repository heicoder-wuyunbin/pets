#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
将 scripts/mysql/service-dbs/*.sql 按服务导入到远程 MySQL (一服务一库)。

用法:
    python import_all.py [host] [port] [user] [password]
默认: 192.168.110.200 3306 root root
"""
import os
import sys

import pymysql
from pymysql.constants import CLIENT

HERE = os.path.dirname(os.path.abspath(__file__))

ORDER = [
    "pets_auth.sql",
    "pets_shop.sql",
    "pets_system.sql",
    "pets_trade.sql",
    "pets_goods.sql",
    "pets_stock.sql",
    "pets_member.sql",
    "pets_market.sql",
    "pets_report.sql",
]


def main():
    host = sys.argv[1] if len(sys.argv) > 1 else "192.168.110.200"
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 3306
    user = sys.argv[3] if len(sys.argv) > 3 else "root"
    password = sys.argv[4] if len(sys.argv) > 4 else "root"

    conn = pymysql.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        charset="utf8mb4",
        autocommit=True,
        client_flag=CLIENT.MULTI_STATEMENTS,
    )
    print("connected: %s:%s as %s" % (host, port, user))

    try:
        with conn.cursor() as cur:
            for name in ORDER:
                path = os.path.join(HERE, name)
                with open(path, "r", encoding="utf-8") as f:
                    sql = f.read()
                cur.execute(sql)
                # 消费多语句产生的所有结果集
                while cur.nextset():
                    pass
                print("OK  %s" % name)
    finally:
        conn.close()

    # 校验各库表数量
    conn = pymysql.connect(
        host=host, port=port, user=user, password=password, charset="utf8mb4"
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT table_schema, COUNT(*) FROM information_schema.tables "
                "WHERE table_schema LIKE 'pets\\_%' GROUP BY table_schema ORDER BY table_schema"
            )
            print("\n-- table counts --")
            for schema, cnt in cur.fetchall():
                print("%-14s %s tables" % (schema, cnt))
    finally:
        conn.close()


if __name__ == "__main__":
    main()
