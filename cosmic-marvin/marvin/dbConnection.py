import contextlib
import mysql
import os
from mysql.connector import errors

from marvin import cloudstackException


class DbConnection(object):
    def __init__(self, host="localhost", port=3306, user='cloud',
                 passwd='cloud', db='cloud'):
        self.host = host
        self.port = port
        self.user = str(user)  # Workaround: http://bugs.mysql.com/?id=67306
        self.passwd = passwd
        self.database = db

    def execute(self, sql=None, params=None, db=None):
        if sql is None:
            return None

        resultRow = []
        with contextlib. \
                closing(mysql.connector.connect(host=str(self.host),
                                                port=int(self.port),
                                                user=str(self.user),
                                                password=str(self.passwd),
                                                db=str(self.database) if not db else db)) as conn:
            conn.autocommit = True
            with contextlib.closing(conn.cursor(buffered=True)) as cursor:
                cursor.execute(sql, params)
                try:
                    resultRow = cursor.fetchall()
                except errors.InterfaceError:
                    # Raised on empty result - DML
                    resultRow = []
        return resultRow

    def executeSqlFromFile(self, fileName=None):
        if fileName is None:
            raise cloudstackException. \
                InvalidParameterException("file can't not none")

        if not os.path.exists(fileName):
            raise cloudstackException. \
                InvalidParameterException("%s not exists" % fileName)

        sqls = open(fileName, "r").read()
        return self.execute(sqls)


if __name__ == "__main__":
    db = DbConnection()
    '''
    try:

        result = db.executeSqlFromFile("/tmp/server-setup.sql")
        if result is not None:
            for r in result:
                print r[0], r[1]
    except cloudstackException.dbException, e:
        print e
    '''
    print db.execute("update vm_template set name='fjkd' where id=200")
    for i in range(10):
        result = db.execute("select job_status, created, \
last_updated from async_job where id=%d" % i)
        print result
