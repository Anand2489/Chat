__author__ = 'sworlite'

import MySQLdb
import random

db = MySQLdb.connect(host="localhost",
                     user="root",
                     passwd="rootp",
                     db="DBCourse")

cur = db.cursor()

alphabet = "abcdefghijklmnopqrstuvwxyz"
Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
number = "0123456789"

# Branch
s_Bname = 20    # string
s_Bcity = 15    # string
s_Assets = 5    # int

#Customer
s_Cname = 20    # string
s_City = 15     # string
s_Cid = 10      # int

#Account
s_AccNo = 10    # int
s_Balance = 10  # int

#Loan
s_LoanNo = 10   # int
s_Amount = 10   # int

#Depositor

#Borrower

records = 50

def stringBuilder (size):
    s = ""
    rand1 = random.randrange(0,25)
    s = s + Alphabet[rand1]
    for i in range(1,size,1):
        rand2 = random.randrange(0,25)
        s = s+alphabet[rand2]

    return s

def integerBuilder (size):
    s =""

    for i in range(0,size,1):
        rand1 = random.randint(0,9)
        s = s+number[rand1]

    return int(s)


def branchT ():

    for i in range(0,records,1):
        bname = stringBuilder(s_Bname)
        bcity = stringBuilder(s_Bcity)
        assets = integerBuilder(s_Assets)

        sql = "insert into Branch (Bname,Bcity,Assets) VALUES ('%s','%s','%d')" % (bname,bcity,assets)
        try:
            cur.execute(sql)
            db.commit()
        except IOError:
            print(IOError)
            db.rollback()

branchT()

def customerT ():

    for i in range(0,records,1):
        cname = stringBuilder(s_Cname)
        city = stringBuilder(s_City)
        cid = integerBuilder(s_Cid)

        sql = "insert into Customer (Cname,City,Cid) VALUES ('%s','%s','%d')" % (cname,city,cid)
        try:
            cur.execute(sql)
            db.commit()
        except IOError:
            print(IOError)
            db.rollback()

customerT()
db.close()
