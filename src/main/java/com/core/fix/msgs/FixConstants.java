package com.core.fix.msgs;

//
// THIS FILE IS AUTOGENERATED
//
public class FixConstants {
    public static byte SOH = 1;
    public static final byte PRINTED_SOH = '|';

    public static class CommissionType { 
        public static final char Absolute = '3';
    }

    public static class ExecTransType { 
        public static final char New = '0';
        public static final char Status = '3';
    }

    public static class SecurityIDSource { 
        public static final char CUSIP = '1';
    }

    public static class OrdStatus { 
        public static final char New = '0';
        public static final char PartiallyFilled = '1';
        public static final char Filled = '2';
        public static final char Canceled = '4';
        public static final char Replaced = '5';
        public static final char Rejected = '8';
    }

    public static class OrdType { 
        public static final char Limit = '2';
    }

    public static class Side { 
        public static final char Buy = '1';
        public static final char Sell = '2';
    }

    public static class TimeInForce { 
        public static final char Day = '0';
        public static final char IOC = '3';
    }

    public static class CxlRejectReason { 
        public static final char TooLateToCancel = '0';
        public static final char UnknownOrder = '1';
        public static final char BrokerOption = '2';
        public static final char Pending = '3';
        public static final char RiskChecks = '4';
    }

    public static class ExecType { 
        public static final char New = '0';
        public static final char PartialFill = '1';
        public static final char Fill = '2';
        public static final char Canceled = '4';
        public static final char Replace = '5';
        public static final char Rejected = '8';
        public static final char Trade = 'F';
        public static final char OrderStatus = 'I';
    }

    public static class SubscriptionRequestType { 
        public static final char SnapshotAndUpdate = '1';
    }

    public static class MDUpdateType { 
        public static final char IncrementalRefresh = '1';
    }

    public static class MDEntryType { 
        public static final char Bid = '0';
        public static final char Offer = '1';
        public static final char Trade = '2';
    }

    public static class MDUpdateAction { 
        public static final char New = '0';
        public static final char Change = '1';
        public static final char Delete = '2';
    }

    public static class SessionRejectReason { 
        public static final char InvalidTagNumber = '0';
        public static final char RequiredTagMissing = '1';
        public static final char TagNotDefinedForThisMsgType = '2';
        public static final char UndefinedTag = '3';
        public static final char TagSpecifiedWithoutAValue = '4';
        public static final char ValueIsIncorrectForThisTag = '5';
        public static final char IncorrectDataFormatForValue = '6';
        public static final char CompIDProblem = '9';
        public static final char SendingTimeAccuracyProblem = 'A';
        public static final char InvalidMsgType = 'B';
    }

    public static class BusinessRejectReason { 
        public static final char Other = '0';
        public static final char UnknownID = '1';
        public static final char UnknownSecurity = '2';
        public static final char UnsupportedMessageType = '3';
        public static final char ApplicationNotAvailable = '4';
        public static final char ConditionallyReqMissingField = '5';
    }

    public static class CxlRejResponseTo { 
        public static final char OrderCancelRequest = '1';
        public static final char OrderCancelReplaceRequest = '2';
    }

    public static class PartyIDSource { 
        public static final char ProprietaryCode = 'D';
    }

    public static class PartyRole { 
        public static final char OrderOriginationTrader = '1';
    }

    public static class TradeReportTransType { 
        public static final char New = '0';
    }

    public static class NoSides { 
        public static final char OneSide = '1';
        public static final char BothSides = '2';
    }

    public static class SecurityListRequestType { 
        public static final char Symbol = '0';
    }

    public static class SecurityRequestResult { 
        public static final char ValidRequest = '0';
        public static final char InvalidRequest = '1';
    }

    public static class PreviouslyReported { 
        public static final char Yes = 'Y';
        public static final char No = 'N';
    }

    public static class ClearingInstruction { 
        public static final char ProcessNormally = '0';
        public static final char MultilateralNetting = '5';
    }

    public static class TradeReportType { 
        public static final char Submit = '0';
    }

    public static class LastFragment { 
        public static final char Yes = 'Y';
        public static final char No = 'N';
    }
}
