#!/usr/bin/python

import sys
import csv
import datetime
from os import listdir
from os.path import isfile, join
from operator import itemgetter

secRemap = { 
    '2_YEAR': '2Y',
    '3_YEAR': '3Y',
    '5_YEAR': '5Y',
    '7_YEAR': '7Y',
    '10_YEAR': '10Y',
    '30_YEAR': '30Y'
}

sideRemap = {
    'A': 'S',
    'B': 'B'
}

actions = { 
    "ALTER": "R", 
    "ADD": "A", 
    "DELETE": "X", 
    "TRADE": "F" 
}

clordidMap = {}

def get_files(fileDir):
   return [ join(fileDir, f) for f in listdir(fileDir) if isfile(join(fileDir, f)) ] 

def convert(types, values):
    return [t(v) for t, v in zip(types, values)]

def date(s):
    return datetime.datetime.strptime(s, '%H:%M:%S:%f %Y%m%d')

def print_date(d):
    return d.strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]

def sec(s):
    return secRemap[s]

def side(s):
    return sideRemap[s]

def price(s):
    return int(s)/256.0

def action(s):
    return actions[s.strip()]

def clordid(s):
    if s in clordidMap: 
        return clordidMap[s]
    else:
        clordidMap[s] = len(clordidMap) + 1
        return len(clordidMap)

def isNew(s):
    return s not in clordidMap    

colMap = {
    "timestamp": {"col": 0, type: date},
    "rowType": {"col": 1, type:str},
    "security": {"col": 2, type:sec},
    "cusip": {"col": 3, type:str},
    "orderBookPosition": {"col": 4, type:int},
    "changeInVisibleQty": {"col": 5, type:int},
    "action": {"col": 6, type:action},
    "unknown": {"col": 7, type:int},
    "clOrdID": {"col": 8, type:str},
    "price": {"col": 9, type:price},
    "displayedQty": {"col": 10, type:int},
    "side": {"col": 11, type: side},
    "totalQty": {"col": 12, type: int},
    "maxDisplayedQty": {"col": 13, type: int}
}

def concat_files(files):
    rows = []
    for file in files:
        print 'Opening: ' + file
        if ".dat" in file:
          with open(file, 'r') as f:
            for row in csv.reader(f, delimiter='|'):                
                if 'OB_CHANGE' in row:
                    row = convert((date, str, sec, str, int, int, action, int, str, price, int, side, int, int), row)
                    # "ts","security","action","id","price","qty","side","totalQty","maxDisplayedQty","clOrdID","ts"
                    row = [print_date(row[colMap["timestamp"]["col"]]), 
                            row[colMap["security"]["col"]], 
                            row[colMap["action"]["col"]], 
                            clordid(row[colMap["clOrdID"]["col"]]), 
                            row[colMap["price"]["col"]], 
                            row[colMap["displayedQty"]["col"]], 
                            row[colMap["side"]["col"]], 
                            row[colMap["totalQty"]["col"]], 
                            row[colMap["maxDisplayedQty"]["col"]], 
                            row[colMap["clOrdID"]["col"]],
                            row[colMap["timestamp"]["col"]],
                            len(rows)
                        ]                    
                    rows.append(row)
                #if rowType == 'TRADE':
                #    if isNew(row[9]):
                        # date trade security cusip price qty int int side cusip
                #        row2 = convert((date, action, sec, str, price, int, int, int, side, clordid), row)
                #        row2 = [row2[0], row2[2], "A", clordid(row2[9]), row2[4], row2[5], row2[8], row2[4], row2[5], row2[9]]
                #        rows.append(row2)
                #        row2 = convert((date, action, sec, str, price, int, int, int, side, clordid), row)
                #        row2 = [row2[0], row2[2], "X", clordid(row2[9]), row2[4], row2[5], row2[8], row2[4], row2[5], row2[9]]
                #        rows.append(row2)
    #rows.sort(key=lambda x: x[0])
    # timestamp => security => time_index
    rows.sort(key=itemgetter(10,2,11))
    return rows                

if __name__ == "__main__":
   d = sys.argv[1]
   out = sys.argv[2]
   files = get_files(d)
   rows = concat_files(files)
   with open(out, 'w') as f:
       w = csv.writer(f, delimiter=',')
       w.writerow(["ts","security","action","id","price","qty","side","totalQty","maxDisplayedQty","clordid","ts2","ts3"]);
       w.writerows(rows)
