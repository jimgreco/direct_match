package com.core.match.msgs;

//
// THIS FILE IS AUTO-GENERATED
//
public class MatchConstants {
    public static final int STATICS_START_INDEX = 1;
    public static final int CLORDID_LENGTH = 32;
    public static final int CONTRIBUTOR_NAME_LENGTH = 10;
    public static String TIME_ZONE = "America/New_York";
    public static String SESSION_ROLLOVER_TIME = "18:00";
    public static int MAX_LIVE_ORDERS = 100000;
    public static int IMPLIED_DECIMALS = 9;
    public static int QTY_MULTIPLIER = 1000000;

    public static void setParam(String param, String value) {
        if (param.equalsIgnoreCase("TIME_ZONE")) {
            TIME_ZONE = value;
        }
        if (param.equalsIgnoreCase("SESSION_ROLLOVER_TIME")) {
            SESSION_ROLLOVER_TIME = value;
        }
        if (param.equalsIgnoreCase("MAX_LIVE_ORDERS")) {
            MAX_LIVE_ORDERS = Integer.parseInt(value);
        }
        if (param.equalsIgnoreCase("IMPLIED_DECIMALS")) {
            IMPLIED_DECIMALS = Integer.parseInt(value);
        }
        if (param.equalsIgnoreCase("QTY_MULTIPLIER")) {
            QTY_MULTIPLIER = Integer.parseInt(value);
        }
    }
	
    public static class Venue {
        public static final char InteractiveData = 'I';
        public static final char Bloomberg = 'B';
   
        public static String toString(char code) {
            switch(code) {
                case InteractiveData:
                    return "InteractiveData";
                case Bloomberg:
                    return "Bloomberg";
                default:
                    return "Unknown";
            }
        }

        public static char toChar(String name) {
            switch(name) {
                case "InteractiveData":
                    return InteractiveData;       
                case "Bloomberg":
                    return Bloomberg;       
                default:
                    return (char)0;
            }
        }

        public static boolean isValid(char code) {
            switch(code) {
                case InteractiveData:
                case Bloomberg:
                    return true;       
                default:
                    return false;
            }
        }
    }
	
    public static class SystemEvent {
        public static final char Open = 'O';
        public static final char Close = 'C';
   
        public static String toString(char code) {
            switch(code) {
                case Open:
                    return "Open";
                case Close:
                    return "Close";
                default:
                    return "Unknown";
            }
        }

        public static char toChar(String name) {
            switch(name) {
                case "Open":
                    return Open;       
                case "Close":
                    return Close;       
                default:
                    return (char)0;
            }
        }

        public static boolean isValid(char code) {
            switch(code) {
                case Open:
                case Close:
                    return true;       
                default:
                    return false;
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

        public static char toChar(String name) {
            switch(name) {
                case "TreasuryNote":
                    return TreasuryNote;       
                case "TreasuryBond":
                    return TreasuryBond;       
                case "DiscreteSpread":
                    return DiscreteSpread;       
                case "DiscreteButterfly":
                    return DiscreteButterfly;       
                case "Roll":
                    return Roll;       
                case "WhenIssued":
                    return WhenIssued;       
                default:
                    return (char)0;
            }
        }

        public static boolean isValid(char code) {
            switch(code) {
                case TreasuryNote:
                case TreasuryBond:
                case DiscreteSpread:
                case DiscreteButterfly:
                case Roll:
                case WhenIssued:
                    return true;       
                default:
                    return false;
            }
        }
    }
	
    public static class MiscRejectReason {
        public static final char InvalidBidPrice = 'B';
        public static final char InvalidVenue = 'D';
        public static final char LockedMarket = 'L';
        public static final char InvalidSecurityID = 'S';
        public static final char InvalidOfferPrice = 'O';
   
        public static String toString(char code) {
            switch(code) {
                case InvalidBidPrice:
                    return "InvalidBidPrice";
                case InvalidVenue:
                    return "InvalidVenue";
                case LockedMarket:
                    return "LockedMarket";
                case InvalidSecurityID:
                    return "InvalidSecurityID";
                case InvalidOfferPrice:
                    return "InvalidOfferPrice";
                default:
                    return "Unknown";
            }
        }

        public static char toChar(String name) {
            switch(name) {
                case "InvalidBidPrice":
                    return InvalidBidPrice;       
                case "InvalidVenue":
                    return InvalidVenue;       
                case "LockedMarket":
                    return LockedMarket;       
                case "InvalidSecurityID":
                    return InvalidSecurityID;       
                case "InvalidOfferPrice":
                    return InvalidOfferPrice;       
                default:
                    return (char)0;
            }
        }

        public static boolean isValid(char code) {
            switch(code) {
                case InvalidBidPrice:
                case InvalidVenue:
                case LockedMarket:
                case InvalidSecurityID:
                case InvalidOfferPrice:
                    return true;       
                default:
                    return false;
            }
        }
    }
	
    public static class OrderRejectReason {
        public static final char AccountDisabled = 'A';
        public static final char TraderDisabled = 'B';
        public static final char SecurityDisabled = 'C';
        public static final char InvalidTrader = 'D';
        public static final char InvalidAccount = 'E';
        public static final char InvalidSecurity = 'F';
        public static final char InvalidContributor = 'H';
        public static final char InvalidContributorSequence = 'I';
        public static final char TradingSystemClosed = 'J';
        public static final char TradingSystemNotAcceptingOrders = 'K';
        public static final char UnknownOrderID = 'L';
        public static final char InvalidPrice = 'M';
        public static final char InvalidQuantity = 'N';
        public static final char GUIRiskViolation = 'O';
        public static final char InvalidSide = 'P';
   
        public static String toString(char code) {
            switch(code) {
                case AccountDisabled:
                    return "AccountDisabled";
                case TraderDisabled:
                    return "TraderDisabled";
                case SecurityDisabled:
                    return "SecurityDisabled";
                case InvalidTrader:
                    return "InvalidTrader";
                case InvalidAccount:
                    return "InvalidAccount";
                case InvalidSecurity:
                    return "InvalidSecurity";
                case InvalidContributor:
                    return "InvalidContributor";
                case InvalidContributorSequence:
                    return "InvalidContributorSequence";
                case TradingSystemClosed:
                    return "TradingSystemClosed";
                case TradingSystemNotAcceptingOrders:
                    return "TradingSystemNotAcceptingOrders";
                case UnknownOrderID:
                    return "UnknownOrderID";
                case InvalidPrice:
                    return "InvalidPrice";
                case InvalidQuantity:
                    return "InvalidQuantity";
                case GUIRiskViolation:
                    return "GUIRiskViolation";
                case InvalidSide:
                    return "InvalidSide";
                default:
                    return "Unknown";
            }
        }

        public static char toChar(String name) {
            switch(name) {
                case "AccountDisabled":
                    return AccountDisabled;       
                case "TraderDisabled":
                    return TraderDisabled;       
                case "SecurityDisabled":
                    return SecurityDisabled;       
                case "InvalidTrader":
                    return InvalidTrader;       
                case "InvalidAccount":
                    return InvalidAccount;       
                case "InvalidSecurity":
                    return InvalidSecurity;       
                case "InvalidContributor":
                    return InvalidContributor;       
                case "InvalidContributorSequence":
                    return InvalidContributorSequence;       
                case "TradingSystemClosed":
                    return TradingSystemClosed;       
                case "TradingSystemNotAcceptingOrders":
                    return TradingSystemNotAcceptingOrders;       
                case "UnknownOrderID":
                    return UnknownOrderID;       
                case "InvalidPrice":
                    return InvalidPrice;       
                case "InvalidQuantity":
                    return InvalidQuantity;       
                case "GUIRiskViolation":
                    return GUIRiskViolation;       
                case "InvalidSide":
                    return InvalidSide;       
                default:
                    return (char)0;
            }
        }

        public static boolean isValid(char code) {
            switch(code) {
                case AccountDisabled:
                case TraderDisabled:
                case SecurityDisabled:
                case InvalidTrader:
                case InvalidAccount:
                case InvalidSecurity:
                case InvalidContributor:
                case InvalidContributorSequence:
                case TradingSystemClosed:
                case TradingSystemNotAcceptingOrders:
                case UnknownOrderID:
                case InvalidPrice:
                case InvalidQuantity:
                case GUIRiskViolation:
                case InvalidSide:
                    return true;       
                default:
                    return false;
            }
        }
    }

    public static class Messages {
        public static final char Contributor = 'C';
        public static final char Trader = 'T';
        public static final char SystemEvent = 'E';
        public static final char Account = 'A';
        public static final char Security = 'S';
        public static final char Order = 'O';
        public static final char ClientOrderReject = 'P';
        public static final char OrderReject = 'Q';
        public static final char Cancel = 'X';
        public static final char ClientCancelReplaceReject = 'Y';
        public static final char CancelReplaceReject = 'Z';
        public static final char Replace = 'U';
        public static final char Fill = 'F';
        public static final char Inbound = 'I';
        public static final char Outbound = 'J';
        public static final char Quote = 'D';
        public static final char MiscReject = 'M';

        public static String toString(char code) {
            switch(code) {
                case 'C':
                    return "Contributor";
                case 'T':
                    return "Trader";
                case 'E':
                    return "SystemEvent";
                case 'A':
                    return "Account";
                case 'S':
                    return "Security";
                case 'O':
                    return "Order";
                case 'P':
                    return "ClientOrderReject";
                case 'Q':
                    return "OrderReject";
                case 'X':
                    return "Cancel";
                case 'Y':
                    return "ClientCancelReplaceReject";
                case 'Z':
                    return "CancelReplaceReject";
                case 'U':
                    return "Replace";
                case 'F':
                    return "Fill";
                case 'I':
                    return "Inbound";
                case 'J':
                    return "Outbound";
                case 'D':
                    return "Quote";
                case 'M':
                    return "MiscReject";
                default:
                    return "Unknown";
            }
        }
    }
}
