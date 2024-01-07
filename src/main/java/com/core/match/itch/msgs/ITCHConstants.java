package com.core.match.itch.msgs;

//
// THIS FILE IS AUTO-GENERATED
//
public class ITCHConstants {

    public static void setParam(String param, String value) {
    }
	
    public static class EventCode {
        public static final char StartOfTradingSession = 'S';
        public static final char EndOfTradingSession = 'E';
   
        public static String toString(char code) {
            switch(code) {
                case StartOfTradingSession:
                    return "StartOfTradingSession";
                case EndOfTradingSession:
                    return "EndOfTradingSession";
                default:
                    return "Unknown";
            }
        }
    }
	
    public static class Side {
        public static final char Buy = 'B';
        public static final char Sell = 'S';
   
        public static String toString(char code) {
            switch(code) {
                case Buy:
                    return "Buy";
                case Sell:
                    return "Sell";
                default:
                    return "Unknown";
            }
        }
    }
	
    public static class SecurityType {
        public static final char TreasuryNote = 'N';
        public static final char TreasuryBond = 'B';
        public static final char DiscreteSpread = 'S';
        public static final char DiscreteButterfly = 'T';
        public static final char Roll = 'R';
        public static final char WhenIssued = 'W';
   
        public static String toString(char code) {
            switch(code) {
                case TreasuryNote:
                    return "TreasuryNote";
                case TreasuryBond:
                    return "TreasuryBond";
                case DiscreteSpread:
                    return "DiscreteSpread";
                case DiscreteButterfly:
                    return "DiscreteButterfly";
                case Roll:
                    return "Roll";
                case WhenIssued:
                    return "WhenIssued";
                default:
                    return "Unknown";
            }
        }
    }
	
    public static class SecuritySource {
        public static final char CUSIP = '1';
        public static final char Synthetic = '2';
   
        public static String toString(char code) {
            switch(code) {
                case CUSIP:
                    return "CUSIP";
                case Synthetic:
                    return "Synthetic";
                default:
                    return "Unknown";
            }
        }
    }

    public static class Messages {
        public static final char System = 'S';
        public static final char Security = 'R';
        public static final char Order = 'A';
        public static final char OrderCancel = 'X';
        public static final char OrderExecuted = 'E';
        public static final char Trade = 'P';

        public static String toString(char code) {
            switch(code) {
                case 'S':
                    return "System";
                case 'R':
                    return "Security";
                case 'A':
                    return "Order";
                case 'X':
                    return "OrderCancel";
                case 'E':
                    return "OrderExecuted";
                case 'P':
                    return "Trade";
                default:
                    return "Unknown";
            }
        }
    }
}
