package com.core.fix.msgs;

//
// THIS FILE IS AUTO-GENERATED 
//
public class FixTags {
    public static final int Account = 1;
    public static final int AvgPx = 6;
    public static final int BeginSeqNo = 7;
    public static final int BeginString = 8;
    public static final int BodyLength = 9;
    public static final int CheckSum = 10;
    public static final int ClOrdID = 11;
    public static final int Commission = 12;
    public static final int CommissionType = 13;
    public static final int CumQty = 14;
    public static final int Currency = 15;
    public static final int EndSeqNo = 16;
    public static final int ExecID = 17;
    public static final int ExecTransType = 20;
    public static final int HandlInst = 21;
    public static final int SecurityIDSource = 22;
    public static final int LastPx = 31;
    public static final int LastShares = 32;
    public static final int MsgSeqNum = 34;
    public static final int MsgType = 35;
    public static final int NewSeqNo = 36;
    public static final int OrderID = 37;
    public static final int OrderQty = 38;
    public static final int OrdStatus = 39;
    public static final int OrdType = 40;
    public static final int OrigClOrdID = 41;
    public static final int PossDupFlag = 43;
    public static final int Price = 44;
    public static final int RefSeqNum = 45;
    public static final int SecurityID = 48;
    public static final int SenderCompID = 49;
    public static final int SendingTime = 52;
    public static final int Side = 54;
    public static final int Symbol = 55;
    public static final int TargetCompID = 56;
    public static final int Text = 58;
    public static final int TimeInForce = 59;
    public static final int TransactTime = 60;
    public static final int SettlementDate = 64;
    public static final int TradeDate = 75;
    public static final int EncryptMethod = 98;
    public static final int CxlRejectReason = 102;
    public static final int HeartBtInt = 108;
    public static final int MinQty = 110;
    public static final int MaxFloor = 111;
    public static final int TestReqID = 112;
    public static final int NetMoney = 118;
    public static final int OrigSendingTime = 122;
    public static final int GapFillFlag = 123;
    public static final int ResetSeqNumFlag = 141;
    public static final int NoRelatedSym = 146;
    public static final int ExecType = 150;
    public static final int LeavesQty = 151;
    public static final int SecurityType = 167;
    public static final int CouponRate = 223;
    public static final int ContractMultiplier = 231;
    public static final int MDReqID = 262;
    public static final int SubscriptionRequestType = 263;
    public static final int MarketDepth = 264;
    public static final int MDUpdateType = 265;
    public static final int NoMDEntryTypes = 267;
    public static final int NoMDEntries = 268;
    public static final int MDEntryType = 269;
    public static final int MDEntryPx = 270;
    public static final int MDEntrySize = 271;
    public static final int MDEntryTime = 273;
    public static final int MDEntryID = 278;
    public static final int MDUpdateAction = 279;
    public static final int MDEntryPositionNo = 290;
    public static final int SecurityReqID = 320;
    public static final int SecurityReqType = 321;
    public static final int SecurityResponseID = 322;
    public static final int SecurityStatusReqID = 324;
    public static final int SecurityTradingStatus = 326;
    public static final int RefTagID = 371;
    public static final int RefMsgType = 372;
    public static final int SessionRejectReason = 373;
    public static final int BusinessRejRefID = 379;
    public static final int BusinessRejectReason = 380;
    public static final int TotNoRelatedSym = 393;
    public static final int CxlRejResponseTo = 434;
    public static final int PartyIDSource = 447;
    public static final int PartyID = 448;
    public static final int PartyRole = 452;
    public static final int NoPartyIDs = 453;
    public static final int TradeReportTransType = 487;
    public static final int MaturityDate = 541;
    public static final int NoSides = 552;
    public static final int SecurityListRequestType = 559;
    public static final int SecurityRequestResult = 560;
    public static final int PreviouslyReported = 570;
    public static final int TradeReportID = 571;
    public static final int NoClearingInstructions = 576;
    public static final int ClearingInstruction = 577;
    public static final int TradeReportType = 856;
    public static final int LastFragment = 893;
    public static final int MinPriceIncrement = 969;
    public static final int MinPriceIncrementAmount = 1146;
    public static final int PriceDisplayType = 16451;

    public static String getTagName(int id) {
        switch(id) {
            case Account:
                return "Account";
            case AvgPx:
                return "AvgPx";
            case BeginSeqNo:
                return "BeginSeqNo";
            case BeginString:
                return "BeginString";
            case BodyLength:
                return "BodyLength";
            case CheckSum:
                return "CheckSum";
            case ClOrdID:
                return "ClOrdID";
            case Commission:
                return "Commission";
            case CommissionType:
                return "CommissionType";
            case CumQty:
                return "CumQty";
            case Currency:
                return "Currency";
            case EndSeqNo:
                return "EndSeqNo";
            case ExecID:
                return "ExecID";
            case ExecTransType:
                return "ExecTransType";
            case HandlInst:
                return "HandlInst";
            case SecurityIDSource:
                return "SecurityIDSource";
            case LastPx:
                return "LastPx";
            case LastShares:
                return "LastShares";
            case MsgSeqNum:
                return "MsgSeqNum";
            case MsgType:
                return "MsgType";
            case NewSeqNo:
                return "NewSeqNo";
            case OrderID:
                return "OrderID";
            case OrderQty:
                return "OrderQty";
            case OrdStatus:
                return "OrdStatus";
            case OrdType:
                return "OrdType";
            case OrigClOrdID:
                return "OrigClOrdID";
            case PossDupFlag:
                return "PossDupFlag";
            case Price:
                return "Price";
            case RefSeqNum:
                return "RefSeqNum";
            case SecurityID:
                return "SecurityID";
            case SenderCompID:
                return "SenderCompID";
            case SendingTime:
                return "SendingTime";
            case Side:
                return "Side";
            case Symbol:
                return "Symbol";
            case TargetCompID:
                return "TargetCompID";
            case Text:
                return "Text";
            case TimeInForce:
                return "TimeInForce";
            case TransactTime:
                return "TransactTime";
            case SettlementDate:
                return "SettlementDate";
            case TradeDate:
                return "TradeDate";
            case EncryptMethod:
                return "EncryptMethod";
            case CxlRejectReason:
                return "CxlRejectReason";
            case HeartBtInt:
                return "HeartBtInt";
            case MinQty:
                return "MinQty";
            case MaxFloor:
                return "MaxFloor";
            case TestReqID:
                return "TestReqID";
            case NetMoney:
                return "NetMoney";
            case OrigSendingTime:
                return "OrigSendingTime";
            case GapFillFlag:
                return "GapFillFlag";
            case ResetSeqNumFlag:
                return "ResetSeqNumFlag";
            case NoRelatedSym:
                return "NoRelatedSym";
            case ExecType:
                return "ExecType";
            case LeavesQty:
                return "LeavesQty";
            case SecurityType:
                return "SecurityType";
            case CouponRate:
                return "CouponRate";
            case ContractMultiplier:
                return "ContractMultiplier";
            case MDReqID:
                return "MDReqID";
            case SubscriptionRequestType:
                return "SubscriptionRequestType";
            case MarketDepth:
                return "MarketDepth";
            case MDUpdateType:
                return "MDUpdateType";
            case NoMDEntryTypes:
                return "NoMDEntryTypes";
            case NoMDEntries:
                return "NoMDEntries";
            case MDEntryType:
                return "MDEntryType";
            case MDEntryPx:
                return "MDEntryPx";
            case MDEntrySize:
                return "MDEntrySize";
            case MDEntryTime:
                return "MDEntryTime";
            case MDEntryID:
                return "MDEntryID";
            case MDUpdateAction:
                return "MDUpdateAction";
            case MDEntryPositionNo:
                return "MDEntryPositionNo";
            case SecurityReqID:
                return "SecurityReqID";
            case SecurityReqType:
                return "SecurityReqType";
            case SecurityResponseID:
                return "SecurityResponseID";
            case RefTagID:
                return "RefTagID";
            case RefMsgType:
                return "RefMsgType";
            case SessionRejectReason:
                return "SessionRejectReason";
            case BusinessRejRefID:
                return "BusinessRejRefID";
            case BusinessRejectReason:
                return "BusinessRejectReason";
            case TotNoRelatedSym:
                return "TotNoRelatedSym";
            case CxlRejResponseTo:
                return "CxlRejResponseTo";
            case PartyIDSource:
                return "PartyIDSource";
            case PartyID:
                return "PartyID";
            case PartyRole:
                return "PartyRole";
            case NoPartyIDs:
                return "NoPartyIDs";
            case TradeReportTransType:
                return "TradeReportTransType";
            case MaturityDate:
                return "MaturityDate";
            case NoSides:
                return "NoSides";
            case SecurityListRequestType:
                return "SecurityListRequestType";
            case SecurityRequestResult:
                return "SecurityRequestResult";
            case PreviouslyReported:
                return "PreviouslyReported";
            case TradeReportID:
                return "TradeReportID";
            case NoClearingInstructions:
                return "NoClearingInstructions";
            case ClearingInstruction:
                return "ClearingInstruction";
            case TradeReportType:
                return "TradeReportType";
            case LastFragment:
                return "LastFragment";
            case MinPriceIncrement:
                return "MinPriceIncrement";
            case MinPriceIncrementAmount:
                return "MinPriceIncrementAmount";
            case PriceDisplayType:
                return "PriceDisplayType";
            default:
                return "Unknown";
        }
    }
}
