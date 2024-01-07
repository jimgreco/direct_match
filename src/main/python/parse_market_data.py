#!/usr/bin/python

import sys

TIME = 1
SECURITY = 3
TRADE_0 = 25
BID_PRICE_0 = 62
ASK_PRICE_0 = 81
PRICE_QTY_DIFF = 6

sec = { 
    'usg_02Y': { 'name': 'US2Y',  'lastQty': 0 },
    'usg_03Y': { 'name': 'US3Y',  'lastQty': 0 },
    'usg_05Y': { 'name': 'US5Y',  'lastQty': 0 },
    'usg_07Y': { 'name': 'US7Y',  'lastQty': 0 },
    'usg_10Y': { 'name': 'US10Y', 'lastQty': 0 },
    'usg_30Y': { 'name': 'US30Y', 'lastQty': 0 }
}

def main(argv):
    infile = open(argv[0], 'r')
    out = open(argv[1], 'w')
    started = False

    for line in infile:
       cols = line.split(',')

       time = cols[TIME][11:]
       if time[0:2] == '08':
           started = True
  
       if not started:
          continue

       security = cols[SECURITY]
       if security not in sec:
          continue

       s = sec[cols[SECURITY]]

       tradePrice = cols[TRADE_0]
       tradeQty = cols[TRADE_0 + PRICE_QTY_DIFF]              
       
       if tradePrice and tradeQty:
           lastQty = s['lastQty']
           tradeQtyInt = int(tradeQty)
           if tradeQtyInt <= lastQty:
               continue;
                                      
           s['lastQty'] = tradeQtyInt
           tradeQty = tradeQtyInt - lastQty
           out.write('TRADE')           
       else:
           doIt = True
           for i in range(0,5):
              if not cols[BID_PRICE_0 + i] or not cols[ASK_PRICE_0 + i]:
                  doIt = False
           if not doIt:
               continue
           s['lastQty'] = 0           
           out.write('QUOTE')

       out.write(',')
       out.write(cols[TIME][11:])
       out.write(',')
       out.write(s['name'])
       
       if tradePrice:
           out.write(',')
           out.write(tradePrice)
           out.write(',')
           out.write(str(tradeQty))
       else:
           for i in range(0,5):
              out.write(',')
              out.write(cols[BID_PRICE_0 + i])
              out.write(',')
              out.write(cols[BID_PRICE_0 + PRICE_QTY_DIFF + i])
              out.write(',')
              out.write(cols[ASK_PRICE_0 + i]) 
              out.write(',')
              out.write(cols[ASK_PRICE_0 + PRICE_QTY_DIFF + i])
              
       out.write('\n')

    out.close()

if __name__ == "__main__":
   main(sys.argv[1:])
