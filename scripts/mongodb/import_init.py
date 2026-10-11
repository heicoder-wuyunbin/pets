#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
将 scripts/mongodb/01-auth-init.js 的集合与索引导入到远程 MongoDB。

用法:
    python import_init.py [host] [port] [user] [password]
默认: 192.168.110.200 27017 root root (authSource=admin, 库 pets_auth)
"""
import sys

from pymongo import ASCENDING, DESCENDING, MongoClient

DB_NAME = "pets_auth"


def main():
    host = sys.argv[1] if len(sys.argv) > 1 else "192.168.110.200"
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 27017
    user = sys.argv[3] if len(sys.argv) > 3 else "root"
    password = sys.argv[4] if len(sys.argv) > 4 else "root"

    client = MongoClient(
        "mongodb://%s:%s@%s:%s/?authSource=admin" % (user, password, host, port)
    )
    db = client[DB_NAME]
    print("connected: %s:%s as %s, db=%s" % (host, port, user, DB_NAME))

    # 1. sms_code — 短信验证码留痕
    if "sms_code" not in db.list_collection_names():
        db.create_collection("sms_code")
    db.sms_code.create_index(
        [("phone", ASCENDING), ("biz_type", ASCENDING), ("create_time", DESCENDING)],
        name="idx_phone_biz_time",
    )
    db.sms_code.create_index(
        [("expire_at", ASCENDING)], name="idx_ttl_expire", expireAfterSeconds=0
    )

    # 2. login_log — 登录日志
    if "login_log" not in db.list_collection_names():
        db.create_collection("login_log")
    db.login_log.create_index(
        [("phone", ASCENDING), ("create_time", DESCENDING)], name="idx_phone_time"
    )
    db.login_log.create_index(
        [("account_id", ASCENDING), ("create_time", DESCENDING)], name="idx_account_time"
    )
    db.login_log.create_index(
        [("create_time", ASCENDING)], name="idx_ttl_create", expireAfterSeconds=7776000
    )

    for name in ("sms_code", "login_log"):
        print("\n%s indexes:" % name)
        for idx in db[name].list_indexes():
            print("  %s  %s" % (idx["name"], idx["key"]))

    client.close()


if __name__ == "__main__":
    main()
