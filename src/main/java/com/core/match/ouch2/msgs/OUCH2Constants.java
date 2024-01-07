package com.core.match.ouch2.msgs;

//
// THIS FILE IS AUTO-GENERATED
//
public class OUCH2Constants {

    public static void setParam(String param, String value) {
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
	
    public static class RejectReason {
        public static final char InvalidAccount = 'A';
        public static final char InvalidTrader = 'V';
        public static final char InvalidSecurity = 'S';
        public static final char InvalidPrice = 'X';
        public static final char InvalidQuantity = 'Q';
        public static final char InvalidSide = 'I';
        public static final char InvalidTIF = 'T';
        public static final char InvalidHiddenQuantity = 'H';
        public static final char DuplicateClOrdID = 'O';
        public static final char UnknownClOrdID = 'U';
        public static final char TooLateToCancelOrModify = 'Y';
        public static final char AccountDisabled = 'D';
        public static final char TraderDisabled = 'B';
        public static final char SecurityDisabled = 'L';
        public static final char TradingSystemClosed = 'C';
        public static final char RiskViolation = 'Z';
        public static final char InternalError = 'E';
   
        public static String toString(char code) {
            switch(code) {
                case InvalidAccount:
                    return "InvalidAccount";
                case InvalidTrader:
                    return "InvalidTrader";
                case InvalidSecurity:
                    return "InvalidSecurity";
                case InvalidPrice:
                    return "InvalidPrice";
                case InvalidQuantity:
                    return "InvalidQuantity";
                case InvalidSide:
                    return "InvalidSide";
                case InvalidTIF:
                    return "InvalidTIF";
                case InvalidHiddenQuantity:
                    return "InvalidHiddenQuantity";
                case DuplicateClOrdID:
                    return "DuplicateClOrdID";
                case UnknownClOrdID:
                    return "UnknownClOrdID";
                case TooLateToCancelOrModify:
                    return "TooLateToCancelOrModify";
                case AccountDisabled:
                    return "AccountDisabled";
                case TraderDisabled:
                    return "TraderDisabled";
                case SecurityDisabled:
                    return "SecurityDisabled";
                case TradingSystemClosed:
                    return "TradingSystemClosed";
                case RiskViolation:
                    return "RiskViolation";
                case InternalError:
                    return "InternalError";
                default:
                    return "Unknown";
            }
        }
    }
	
    public static class CanceledReason {
        public static final char UserRequest = 'U';
        public static final char ManuallyCanceled = 'M';
        public static final char EndOfTradingDay = 'E';
   
        public static String toString(char code) {
            switch(code) {
                case UserRequest:
                    return "UserRequest";
                case ManuallyCanceled:
                    return "ManuallyCanceled";
                case EndOfTradingDay:
                    return "EndOfTradingDay";
                default:
                    return "Unknown";
            }
        }
    }
	
    public static class TimeInForce {
        public static final char DAY = '0';
        public static final char IOC = '3';
   
        public static String toString(char code) {
            switch(code) {
                case DAY:
                    return "DAY";
                case IOC:
                    return "IOC";
                default:
                    return "Unknown";
            }
        }
    }

    public static class Messages {
        public static final char TradeConfirmation = 'T';
        public static final char Order = 'O';
        public static final char Cancel = 'X';
        public static final char Replace = 'U';
        public static final char Accepted = 'A';
        public static final char Canceled = 'C';
        public static final char Replaced = 'M';
        public static final char CancelRejected = 'I';
        public static final char Rejected = 'J';
        public static final char Fill = 'E';

        public static String toString(char code) {
            switch(code) {
                case 'T':
                    return "TradeConfirmation";
                case 'O':
                    return "Order";
                case 'X':
                    return "Cancel";
                case 'U':
                    return "Replace";
                case 'A':
                    return "Accepted";
                case 'C':
                    return "Canceled";
                case 'M':
                    return "Replaced";
                case 'I':
                    return "CancelRejected";
                case 'J':
                    return "Rejected";
                case 'E':
                    return "Fill";
                default:
                    return "Unknown";
            }
        }
    }
}
