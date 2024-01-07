package com.core.fix.msgs;

import com.core.fix.*;
import com.core.fix.tags.StubFixTag;
import org.mockito.Mockito;

//
// THIS FILE IS AUTOGENERATED 
//
public class FixStubTags {
    public final StubFixTag Account = new StubFixTag(1);
    public final StubFixTag AvgPx = new StubFixTag(6);
    public final StubFixTag BeginSeqNo = new StubFixTag(7);
    public final StubFixTag BeginString = new StubFixTag(8);
    public final StubFixTag BodyLength = new StubFixTag(9);
    public final StubFixTag CheckSum = new StubFixTag(10);
    public final StubFixTag ClOrdID = new StubFixTag(11);
    public final StubFixTag Commission = new StubFixTag(12);
    public final StubFixTag CommissionType = new StubFixTag(13);
    public final StubFixTag CumQty = new StubFixTag(14);
    public final StubFixTag Currency = new StubFixTag(15);
    public final StubFixTag EndSeqNo = new StubFixTag(16);
    public final StubFixTag ExecID = new StubFixTag(17);
    public final StubFixTag ExecTransType = new StubFixTag(20);
    public final StubFixTag HandlInst = new StubFixTag(21);
    public final StubFixTag SecurityIDSource = new StubFixTag(22);
    public final StubFixTag LastPx = new StubFixTag(31);
    public final StubFixTag LastShares = new StubFixTag(32);
    public final StubFixTag MsgSeqNum = new StubFixTag(34);
    public final StubFixTag MsgType = new StubFixTag(35);
    public final StubFixTag NewSeqNo = new StubFixTag(36);
    public final StubFixTag OrderID = new StubFixTag(37);
    public final StubFixTag OrderQty = new StubFixTag(38);
    public final StubFixTag OrdStatus = new StubFixTag(39);
    public final StubFixTag OrdType = new StubFixTag(40);
    public final StubFixTag OrigClOrdID = new StubFixTag(41);
    public final StubFixTag PossDupFlag = new StubFixTag(43);
    public final StubFixTag Price = new StubFixTag(44);
    public final StubFixTag RefSeqNum = new StubFixTag(45);
    public final StubFixTag SecurityID = new StubFixTag(48);
    public final StubFixTag SenderCompID = new StubFixTag(49);
    public final StubFixTag SendingTime = new StubFixTag(52);
    public final StubFixTag Side = new StubFixTag(54);
    public final StubFixTag Symbol = new StubFixTag(55);
    public final StubFixTag TargetCompID = new StubFixTag(56);
    public final StubFixTag Text = new StubFixTag(58);
    public final StubFixTag TimeInForce = new StubFixTag(59);
    public final StubFixTag TransactTime = new StubFixTag(60);
    public final StubFixTag SettlementDate = new StubFixTag(64);
    public final StubFixTag TradeDate = new StubFixTag(75);
    public final StubFixTag EncryptMethod = new StubFixTag(98);
    public final StubFixTag CxlRejectReason = new StubFixTag(102);
    public final StubFixTag HeartBtInt = new StubFixTag(108);
    public final StubFixTag MinQty = new StubFixTag(110);
    public final StubFixTag MaxFloor = new StubFixTag(111);
    public final StubFixTag TestReqID = new StubFixTag(112);
    public final StubFixTag NetMoney = new StubFixTag(118);
    public final StubFixTag OrigSendingTime = new StubFixTag(122);
    public final StubFixTag GapFillFlag = new StubFixTag(123);
    public final StubFixTag ResetSeqNumFlag = new StubFixTag(141);
    public final StubFixTag NoRelatedSym = new StubFixTag(146);
    public final StubFixTag ExecType = new StubFixTag(150);
    public final StubFixTag LeavesQty = new StubFixTag(151);
    public final StubFixTag SecurityType = new StubFixTag(167);
    public final StubFixTag CouponRate = new StubFixTag(223);
    public final StubFixTag MDReqID = new StubFixTag(262);
    public final StubFixTag SubscriptionRequestType = new StubFixTag(263);
    public final StubFixTag MarketDepth = new StubFixTag(264);
    public final StubFixTag MDUpdateType = new StubFixTag(265);
    public final StubFixTag NoMDEntryTypes = new StubFixTag(267);
    public final StubFixTag NoMDEntries = new StubFixTag(268);
    public final StubFixTag MDEntryType = new StubFixTag(269);
    public final StubFixTag MDEntryPx = new StubFixTag(270);
    public final StubFixTag MDEntrySize = new StubFixTag(271);
    public final StubFixTag MDEntryTime = new StubFixTag(273);
    public final StubFixTag MDEntryID = new StubFixTag(278);
    public final StubFixTag MDUpdateAction = new StubFixTag(279);
    public final StubFixTag SecurityReqID = new StubFixTag(320);
    public final StubFixTag SecurityResponseID = new StubFixTag(322);
    public final StubFixTag RefTagID = new StubFixTag(371);
    public final StubFixTag RefMsgType = new StubFixTag(372);
    public final StubFixTag SessionRejectReason = new StubFixTag(373);
    public final StubFixTag BusinessRejRefID = new StubFixTag(379);
    public final StubFixTag BusinessRejectReason = new StubFixTag(380);
    public final StubFixTag TotNoRelatedSym = new StubFixTag(393);
    public final StubFixTag CxlRejResponseTo = new StubFixTag(434);
    public final StubFixTag PartyIDSource = new StubFixTag(447);
    public final StubFixTag PartyID = new StubFixTag(448);
    public final StubFixTag PartyRole = new StubFixTag(452);
    public final StubFixTag NoPartyIDs = new StubFixTag(453);
    public final StubFixTag TradeReportTransType = new StubFixTag(487);
    public final StubFixTag MaturityDate = new StubFixTag(541);
    public final StubFixTag NoSides = new StubFixTag(552);
    public final StubFixTag SecurityListRequestType = new StubFixTag(559);
    public final StubFixTag SecurityRequestResult = new StubFixTag(560);
    public final StubFixTag PreviouslyReported = new StubFixTag(570);
    public final StubFixTag TradeReportID = new StubFixTag(571);
    public final StubFixTag NoClearingInstructions = new StubFixTag(576);
    public final StubFixTag ClearingInstruction = new StubFixTag(577);
    public final StubFixTag TradeReportType = new StubFixTag(856);
    public final StubFixTag LastFragment = new StubFixTag(893);

    public void init(FixParser parser) {
       Mockito.when(parser.createReadWriteFIXTag(1)).thenReturn(Account);
       Mockito.when(parser.createWriteOnlyFIXTag(1)).thenReturn(Account);
       Mockito.when(parser.createReadWriteFIXGroupTag(1)).thenReturn(Account);
       Mockito.when(parser.createReadWriteFIXTag(6)).thenReturn(AvgPx);
       Mockito.when(parser.createWriteOnlyFIXTag(6)).thenReturn(AvgPx);
       Mockito.when(parser.createReadWriteFIXGroupTag(6)).thenReturn(AvgPx);
       Mockito.when(parser.createReadWriteFIXTag(7)).thenReturn(BeginSeqNo);
       Mockito.when(parser.createWriteOnlyFIXTag(7)).thenReturn(BeginSeqNo);
       Mockito.when(parser.createReadWriteFIXGroupTag(7)).thenReturn(BeginSeqNo);
       Mockito.when(parser.createReadWriteFIXTag(8)).thenReturn(BeginString);
       Mockito.when(parser.createWriteOnlyFIXTag(8)).thenReturn(BeginString);
       Mockito.when(parser.createReadWriteFIXGroupTag(8)).thenReturn(BeginString);
       Mockito.when(parser.createReadWriteFIXTag(9)).thenReturn(BodyLength);
       Mockito.when(parser.createWriteOnlyFIXTag(9)).thenReturn(BodyLength);
       Mockito.when(parser.createReadWriteFIXGroupTag(9)).thenReturn(BodyLength);
       Mockito.when(parser.createReadWriteFIXTag(10)).thenReturn(CheckSum);
       Mockito.when(parser.createWriteOnlyFIXTag(10)).thenReturn(CheckSum);
       Mockito.when(parser.createReadWriteFIXGroupTag(10)).thenReturn(CheckSum);
       Mockito.when(parser.createReadWriteFIXTag(11)).thenReturn(ClOrdID);
       Mockito.when(parser.createWriteOnlyFIXTag(11)).thenReturn(ClOrdID);
       Mockito.when(parser.createReadWriteFIXGroupTag(11)).thenReturn(ClOrdID);
       Mockito.when(parser.createReadWriteFIXTag(12)).thenReturn(Commission);
       Mockito.when(parser.createWriteOnlyFIXTag(12)).thenReturn(Commission);
       Mockito.when(parser.createReadWriteFIXGroupTag(12)).thenReturn(Commission);
       Mockito.when(parser.createReadWriteFIXTag(13)).thenReturn(CommissionType);
       Mockito.when(parser.createWriteOnlyFIXTag(13)).thenReturn(CommissionType);
       Mockito.when(parser.createReadWriteFIXGroupTag(13)).thenReturn(CommissionType);
       Mockito.when(parser.createReadWriteFIXTag(14)).thenReturn(CumQty);
       Mockito.when(parser.createWriteOnlyFIXTag(14)).thenReturn(CumQty);
       Mockito.when(parser.createReadWriteFIXGroupTag(14)).thenReturn(CumQty);
       Mockito.when(parser.createReadWriteFIXTag(15)).thenReturn(Currency);
       Mockito.when(parser.createWriteOnlyFIXTag(15)).thenReturn(Currency);
       Mockito.when(parser.createReadWriteFIXGroupTag(15)).thenReturn(Currency);
       Mockito.when(parser.createReadWriteFIXTag(16)).thenReturn(EndSeqNo);
       Mockito.when(parser.createWriteOnlyFIXTag(16)).thenReturn(EndSeqNo);
       Mockito.when(parser.createReadWriteFIXGroupTag(16)).thenReturn(EndSeqNo);
       Mockito.when(parser.createReadWriteFIXTag(17)).thenReturn(ExecID);
       Mockito.when(parser.createWriteOnlyFIXTag(17)).thenReturn(ExecID);
       Mockito.when(parser.createReadWriteFIXGroupTag(17)).thenReturn(ExecID);
       Mockito.when(parser.createReadWriteFIXTag(20)).thenReturn(ExecTransType);
       Mockito.when(parser.createWriteOnlyFIXTag(20)).thenReturn(ExecTransType);
       Mockito.when(parser.createReadWriteFIXGroupTag(20)).thenReturn(ExecTransType);
       Mockito.when(parser.createReadWriteFIXTag(21)).thenReturn(HandlInst);
       Mockito.when(parser.createWriteOnlyFIXTag(21)).thenReturn(HandlInst);
       Mockito.when(parser.createReadWriteFIXGroupTag(21)).thenReturn(HandlInst);
       Mockito.when(parser.createReadWriteFIXTag(22)).thenReturn(SecurityIDSource);
       Mockito.when(parser.createWriteOnlyFIXTag(22)).thenReturn(SecurityIDSource);
       Mockito.when(parser.createReadWriteFIXGroupTag(22)).thenReturn(SecurityIDSource);
       Mockito.when(parser.createReadWriteFIXTag(31)).thenReturn(LastPx);
       Mockito.when(parser.createWriteOnlyFIXTag(31)).thenReturn(LastPx);
       Mockito.when(parser.createReadWriteFIXGroupTag(31)).thenReturn(LastPx);
       Mockito.when(parser.createReadWriteFIXTag(32)).thenReturn(LastShares);
       Mockito.when(parser.createWriteOnlyFIXTag(32)).thenReturn(LastShares);
       Mockito.when(parser.createReadWriteFIXGroupTag(32)).thenReturn(LastShares);
       Mockito.when(parser.createReadWriteFIXTag(34)).thenReturn(MsgSeqNum);
       Mockito.when(parser.createWriteOnlyFIXTag(34)).thenReturn(MsgSeqNum);
       Mockito.when(parser.createReadWriteFIXGroupTag(34)).thenReturn(MsgSeqNum);
       Mockito.when(parser.createReadWriteFIXTag(35)).thenReturn(MsgType);
       Mockito.when(parser.createWriteOnlyFIXTag(35)).thenReturn(MsgType);
       Mockito.when(parser.createReadWriteFIXGroupTag(35)).thenReturn(MsgType);
       Mockito.when(parser.createReadWriteFIXTag(36)).thenReturn(NewSeqNo);
       Mockito.when(parser.createWriteOnlyFIXTag(36)).thenReturn(NewSeqNo);
       Mockito.when(parser.createReadWriteFIXGroupTag(36)).thenReturn(NewSeqNo);
       Mockito.when(parser.createReadWriteFIXTag(37)).thenReturn(OrderID);
       Mockito.when(parser.createWriteOnlyFIXTag(37)).thenReturn(OrderID);
       Mockito.when(parser.createReadWriteFIXGroupTag(37)).thenReturn(OrderID);
       Mockito.when(parser.createReadWriteFIXTag(38)).thenReturn(OrderQty);
       Mockito.when(parser.createWriteOnlyFIXTag(38)).thenReturn(OrderQty);
       Mockito.when(parser.createReadWriteFIXGroupTag(38)).thenReturn(OrderQty);
       Mockito.when(parser.createReadWriteFIXTag(39)).thenReturn(OrdStatus);
       Mockito.when(parser.createWriteOnlyFIXTag(39)).thenReturn(OrdStatus);
       Mockito.when(parser.createReadWriteFIXGroupTag(39)).thenReturn(OrdStatus);
       Mockito.when(parser.createReadWriteFIXTag(40)).thenReturn(OrdType);
       Mockito.when(parser.createWriteOnlyFIXTag(40)).thenReturn(OrdType);
       Mockito.when(parser.createReadWriteFIXGroupTag(40)).thenReturn(OrdType);
       Mockito.when(parser.createReadWriteFIXTag(41)).thenReturn(OrigClOrdID);
       Mockito.when(parser.createWriteOnlyFIXTag(41)).thenReturn(OrigClOrdID);
       Mockito.when(parser.createReadWriteFIXGroupTag(41)).thenReturn(OrigClOrdID);
       Mockito.when(parser.createReadWriteFIXTag(43)).thenReturn(PossDupFlag);
       Mockito.when(parser.createWriteOnlyFIXTag(43)).thenReturn(PossDupFlag);
       Mockito.when(parser.createReadWriteFIXGroupTag(43)).thenReturn(PossDupFlag);
       Mockito.when(parser.createReadWriteFIXTag(44)).thenReturn(Price);
       Mockito.when(parser.createWriteOnlyFIXTag(44)).thenReturn(Price);
       Mockito.when(parser.createReadWriteFIXGroupTag(44)).thenReturn(Price);
       Mockito.when(parser.createReadWriteFIXTag(45)).thenReturn(RefSeqNum);
       Mockito.when(parser.createWriteOnlyFIXTag(45)).thenReturn(RefSeqNum);
       Mockito.when(parser.createReadWriteFIXGroupTag(45)).thenReturn(RefSeqNum);
       Mockito.when(parser.createReadWriteFIXTag(48)).thenReturn(SecurityID);
       Mockito.when(parser.createWriteOnlyFIXTag(48)).thenReturn(SecurityID);
       Mockito.when(parser.createReadWriteFIXGroupTag(48)).thenReturn(SecurityID);
       Mockito.when(parser.createReadWriteFIXTag(49)).thenReturn(SenderCompID);
       Mockito.when(parser.createWriteOnlyFIXTag(49)).thenReturn(SenderCompID);
       Mockito.when(parser.createReadWriteFIXGroupTag(49)).thenReturn(SenderCompID);
       Mockito.when(parser.createReadWriteFIXTag(52)).thenReturn(SendingTime);
       Mockito.when(parser.createWriteOnlyFIXTag(52)).thenReturn(SendingTime);
       Mockito.when(parser.createReadWriteFIXGroupTag(52)).thenReturn(SendingTime);
       Mockito.when(parser.createReadWriteFIXTag(54)).thenReturn(Side);
       Mockito.when(parser.createWriteOnlyFIXTag(54)).thenReturn(Side);
       Mockito.when(parser.createReadWriteFIXGroupTag(54)).thenReturn(Side);
       Mockito.when(parser.createReadWriteFIXTag(55)).thenReturn(Symbol);
       Mockito.when(parser.createWriteOnlyFIXTag(55)).thenReturn(Symbol);
       Mockito.when(parser.createReadWriteFIXGroupTag(55)).thenReturn(Symbol);
       Mockito.when(parser.createReadWriteFIXTag(56)).thenReturn(TargetCompID);
       Mockito.when(parser.createWriteOnlyFIXTag(56)).thenReturn(TargetCompID);
       Mockito.when(parser.createReadWriteFIXGroupTag(56)).thenReturn(TargetCompID);
       Mockito.when(parser.createReadWriteFIXTag(58)).thenReturn(Text);
       Mockito.when(parser.createWriteOnlyFIXTag(58)).thenReturn(Text);
       Mockito.when(parser.createReadWriteFIXGroupTag(58)).thenReturn(Text);
       Mockito.when(parser.createReadWriteFIXTag(59)).thenReturn(TimeInForce);
       Mockito.when(parser.createWriteOnlyFIXTag(59)).thenReturn(TimeInForce);
       Mockito.when(parser.createReadWriteFIXGroupTag(59)).thenReturn(TimeInForce);
       Mockito.when(parser.createReadWriteFIXTag(60)).thenReturn(TransactTime);
       Mockito.when(parser.createWriteOnlyFIXTag(60)).thenReturn(TransactTime);
       Mockito.when(parser.createReadWriteFIXGroupTag(60)).thenReturn(TransactTime);
       Mockito.when(parser.createReadWriteFIXTag(64)).thenReturn(SettlementDate);
       Mockito.when(parser.createWriteOnlyFIXTag(64)).thenReturn(SettlementDate);
       Mockito.when(parser.createReadWriteFIXGroupTag(64)).thenReturn(SettlementDate);
       Mockito.when(parser.createReadWriteFIXTag(75)).thenReturn(TradeDate);
       Mockito.when(parser.createWriteOnlyFIXTag(75)).thenReturn(TradeDate);
       Mockito.when(parser.createReadWriteFIXGroupTag(75)).thenReturn(TradeDate);
       Mockito.when(parser.createReadWriteFIXTag(98)).thenReturn(EncryptMethod);
       Mockito.when(parser.createWriteOnlyFIXTag(98)).thenReturn(EncryptMethod);
       Mockito.when(parser.createReadWriteFIXGroupTag(98)).thenReturn(EncryptMethod);
       Mockito.when(parser.createReadWriteFIXTag(102)).thenReturn(CxlRejectReason);
       Mockito.when(parser.createWriteOnlyFIXTag(102)).thenReturn(CxlRejectReason);
       Mockito.when(parser.createReadWriteFIXGroupTag(102)).thenReturn(CxlRejectReason);
       Mockito.when(parser.createReadWriteFIXTag(108)).thenReturn(HeartBtInt);
       Mockito.when(parser.createWriteOnlyFIXTag(108)).thenReturn(HeartBtInt);
       Mockito.when(parser.createReadWriteFIXGroupTag(108)).thenReturn(HeartBtInt);
       Mockito.when(parser.createReadWriteFIXTag(110)).thenReturn(MinQty);
       Mockito.when(parser.createWriteOnlyFIXTag(110)).thenReturn(MinQty);
       Mockito.when(parser.createReadWriteFIXGroupTag(110)).thenReturn(MinQty);
       Mockito.when(parser.createReadWriteFIXTag(111)).thenReturn(MaxFloor);
       Mockito.when(parser.createWriteOnlyFIXTag(111)).thenReturn(MaxFloor);
       Mockito.when(parser.createReadWriteFIXGroupTag(111)).thenReturn(MaxFloor);
       Mockito.when(parser.createReadWriteFIXTag(112)).thenReturn(TestReqID);
       Mockito.when(parser.createWriteOnlyFIXTag(112)).thenReturn(TestReqID);
       Mockito.when(parser.createReadWriteFIXGroupTag(112)).thenReturn(TestReqID);
       Mockito.when(parser.createReadWriteFIXTag(118)).thenReturn(NetMoney);
       Mockito.when(parser.createWriteOnlyFIXTag(118)).thenReturn(NetMoney);
       Mockito.when(parser.createReadWriteFIXGroupTag(118)).thenReturn(NetMoney);
       Mockito.when(parser.createReadWriteFIXTag(122)).thenReturn(OrigSendingTime);
       Mockito.when(parser.createWriteOnlyFIXTag(122)).thenReturn(OrigSendingTime);
       Mockito.when(parser.createReadWriteFIXGroupTag(122)).thenReturn(OrigSendingTime);
       Mockito.when(parser.createReadWriteFIXTag(123)).thenReturn(GapFillFlag);
       Mockito.when(parser.createWriteOnlyFIXTag(123)).thenReturn(GapFillFlag);
       Mockito.when(parser.createReadWriteFIXGroupTag(123)).thenReturn(GapFillFlag);
       Mockito.when(parser.createReadWriteFIXTag(141)).thenReturn(ResetSeqNumFlag);
       Mockito.when(parser.createWriteOnlyFIXTag(141)).thenReturn(ResetSeqNumFlag);
       Mockito.when(parser.createReadWriteFIXGroupTag(141)).thenReturn(ResetSeqNumFlag);
       Mockito.when(parser.createReadWriteFIXTag(146)).thenReturn(NoRelatedSym);
       Mockito.when(parser.createWriteOnlyFIXTag(146)).thenReturn(NoRelatedSym);
       Mockito.when(parser.createReadWriteFIXGroupTag(146)).thenReturn(NoRelatedSym);
       Mockito.when(parser.createReadWriteFIXTag(150)).thenReturn(ExecType);
       Mockito.when(parser.createWriteOnlyFIXTag(150)).thenReturn(ExecType);
       Mockito.when(parser.createReadWriteFIXGroupTag(150)).thenReturn(ExecType);
       Mockito.when(parser.createReadWriteFIXTag(151)).thenReturn(LeavesQty);
       Mockito.when(parser.createWriteOnlyFIXTag(151)).thenReturn(LeavesQty);
       Mockito.when(parser.createReadWriteFIXGroupTag(151)).thenReturn(LeavesQty);
       Mockito.when(parser.createReadWriteFIXTag(167)).thenReturn(SecurityType);
       Mockito.when(parser.createWriteOnlyFIXTag(167)).thenReturn(SecurityType);
       Mockito.when(parser.createReadWriteFIXGroupTag(167)).thenReturn(SecurityType);
       Mockito.when(parser.createReadWriteFIXTag(223)).thenReturn(CouponRate);
       Mockito.when(parser.createWriteOnlyFIXTag(223)).thenReturn(CouponRate);
       Mockito.when(parser.createReadWriteFIXGroupTag(223)).thenReturn(CouponRate);
       Mockito.when(parser.createReadWriteFIXTag(262)).thenReturn(MDReqID);
       Mockito.when(parser.createWriteOnlyFIXTag(262)).thenReturn(MDReqID);
       Mockito.when(parser.createReadWriteFIXGroupTag(262)).thenReturn(MDReqID);
       Mockito.when(parser.createReadWriteFIXTag(263)).thenReturn(SubscriptionRequestType);
       Mockito.when(parser.createWriteOnlyFIXTag(263)).thenReturn(SubscriptionRequestType);
       Mockito.when(parser.createReadWriteFIXGroupTag(263)).thenReturn(SubscriptionRequestType);
       Mockito.when(parser.createReadWriteFIXTag(264)).thenReturn(MarketDepth);
       Mockito.when(parser.createWriteOnlyFIXTag(264)).thenReturn(MarketDepth);
       Mockito.when(parser.createReadWriteFIXGroupTag(264)).thenReturn(MarketDepth);
       Mockito.when(parser.createReadWriteFIXTag(265)).thenReturn(MDUpdateType);
       Mockito.when(parser.createWriteOnlyFIXTag(265)).thenReturn(MDUpdateType);
       Mockito.when(parser.createReadWriteFIXGroupTag(265)).thenReturn(MDUpdateType);
       Mockito.when(parser.createReadWriteFIXTag(267)).thenReturn(NoMDEntryTypes);
       Mockito.when(parser.createWriteOnlyFIXTag(267)).thenReturn(NoMDEntryTypes);
       Mockito.when(parser.createReadWriteFIXGroupTag(267)).thenReturn(NoMDEntryTypes);
       Mockito.when(parser.createReadWriteFIXTag(268)).thenReturn(NoMDEntries);
       Mockito.when(parser.createWriteOnlyFIXTag(268)).thenReturn(NoMDEntries);
       Mockito.when(parser.createReadWriteFIXGroupTag(268)).thenReturn(NoMDEntries);
       Mockito.when(parser.createReadWriteFIXTag(269)).thenReturn(MDEntryType);
       Mockito.when(parser.createWriteOnlyFIXTag(269)).thenReturn(MDEntryType);
       Mockito.when(parser.createReadWriteFIXGroupTag(269)).thenReturn(MDEntryType);
       Mockito.when(parser.createReadWriteFIXTag(270)).thenReturn(MDEntryPx);
       Mockito.when(parser.createWriteOnlyFIXTag(270)).thenReturn(MDEntryPx);
       Mockito.when(parser.createReadWriteFIXGroupTag(270)).thenReturn(MDEntryPx);
       Mockito.when(parser.createReadWriteFIXTag(271)).thenReturn(MDEntrySize);
       Mockito.when(parser.createWriteOnlyFIXTag(271)).thenReturn(MDEntrySize);
       Mockito.when(parser.createReadWriteFIXGroupTag(271)).thenReturn(MDEntrySize);
       Mockito.when(parser.createReadWriteFIXTag(273)).thenReturn(MDEntryTime);
       Mockito.when(parser.createWriteOnlyFIXTag(273)).thenReturn(MDEntryTime);
       Mockito.when(parser.createReadWriteFIXGroupTag(273)).thenReturn(MDEntryTime);
       Mockito.when(parser.createReadWriteFIXTag(278)).thenReturn(MDEntryID);
       Mockito.when(parser.createWriteOnlyFIXTag(278)).thenReturn(MDEntryID);
       Mockito.when(parser.createReadWriteFIXGroupTag(278)).thenReturn(MDEntryID);
       Mockito.when(parser.createReadWriteFIXTag(279)).thenReturn(MDUpdateAction);
       Mockito.when(parser.createWriteOnlyFIXTag(279)).thenReturn(MDUpdateAction);
       Mockito.when(parser.createReadWriteFIXGroupTag(279)).thenReturn(MDUpdateAction);
       Mockito.when(parser.createReadWriteFIXTag(320)).thenReturn(SecurityReqID);
       Mockito.when(parser.createWriteOnlyFIXTag(320)).thenReturn(SecurityReqID);
       Mockito.when(parser.createReadWriteFIXGroupTag(320)).thenReturn(SecurityReqID);
       Mockito.when(parser.createReadWriteFIXTag(322)).thenReturn(SecurityResponseID);
       Mockito.when(parser.createWriteOnlyFIXTag(322)).thenReturn(SecurityResponseID);
       Mockito.when(parser.createReadWriteFIXGroupTag(322)).thenReturn(SecurityResponseID);
       Mockito.when(parser.createReadWriteFIXTag(371)).thenReturn(RefTagID);
       Mockito.when(parser.createWriteOnlyFIXTag(371)).thenReturn(RefTagID);
       Mockito.when(parser.createReadWriteFIXGroupTag(371)).thenReturn(RefTagID);
       Mockito.when(parser.createReadWriteFIXTag(372)).thenReturn(RefMsgType);
       Mockito.when(parser.createWriteOnlyFIXTag(372)).thenReturn(RefMsgType);
       Mockito.when(parser.createReadWriteFIXGroupTag(372)).thenReturn(RefMsgType);
       Mockito.when(parser.createReadWriteFIXTag(373)).thenReturn(SessionRejectReason);
       Mockito.when(parser.createWriteOnlyFIXTag(373)).thenReturn(SessionRejectReason);
       Mockito.when(parser.createReadWriteFIXGroupTag(373)).thenReturn(SessionRejectReason);
       Mockito.when(parser.createReadWriteFIXTag(379)).thenReturn(BusinessRejRefID);
       Mockito.when(parser.createWriteOnlyFIXTag(379)).thenReturn(BusinessRejRefID);
       Mockito.when(parser.createReadWriteFIXGroupTag(379)).thenReturn(BusinessRejRefID);
       Mockito.when(parser.createReadWriteFIXTag(380)).thenReturn(BusinessRejectReason);
       Mockito.when(parser.createWriteOnlyFIXTag(380)).thenReturn(BusinessRejectReason);
       Mockito.when(parser.createReadWriteFIXGroupTag(380)).thenReturn(BusinessRejectReason);
       Mockito.when(parser.createReadWriteFIXTag(393)).thenReturn(TotNoRelatedSym);
       Mockito.when(parser.createWriteOnlyFIXTag(393)).thenReturn(TotNoRelatedSym);
       Mockito.when(parser.createReadWriteFIXGroupTag(393)).thenReturn(TotNoRelatedSym);
       Mockito.when(parser.createReadWriteFIXTag(434)).thenReturn(CxlRejResponseTo);
       Mockito.when(parser.createWriteOnlyFIXTag(434)).thenReturn(CxlRejResponseTo);
       Mockito.when(parser.createReadWriteFIXGroupTag(434)).thenReturn(CxlRejResponseTo);
       Mockito.when(parser.createReadWriteFIXTag(447)).thenReturn(PartyIDSource);
       Mockito.when(parser.createWriteOnlyFIXTag(447)).thenReturn(PartyIDSource);
       Mockito.when(parser.createReadWriteFIXGroupTag(447)).thenReturn(PartyIDSource);
       Mockito.when(parser.createReadWriteFIXTag(448)).thenReturn(PartyID);
       Mockito.when(parser.createWriteOnlyFIXTag(448)).thenReturn(PartyID);
       Mockito.when(parser.createReadWriteFIXGroupTag(448)).thenReturn(PartyID);
       Mockito.when(parser.createReadWriteFIXTag(452)).thenReturn(PartyRole);
       Mockito.when(parser.createWriteOnlyFIXTag(452)).thenReturn(PartyRole);
       Mockito.when(parser.createReadWriteFIXGroupTag(452)).thenReturn(PartyRole);
       Mockito.when(parser.createReadWriteFIXTag(453)).thenReturn(NoPartyIDs);
       Mockito.when(parser.createWriteOnlyFIXTag(453)).thenReturn(NoPartyIDs);
       Mockito.when(parser.createReadWriteFIXGroupTag(453)).thenReturn(NoPartyIDs);
       Mockito.when(parser.createReadWriteFIXTag(487)).thenReturn(TradeReportTransType);
       Mockito.when(parser.createWriteOnlyFIXTag(487)).thenReturn(TradeReportTransType);
       Mockito.when(parser.createReadWriteFIXGroupTag(487)).thenReturn(TradeReportTransType);
       Mockito.when(parser.createReadWriteFIXTag(541)).thenReturn(MaturityDate);
       Mockito.when(parser.createWriteOnlyFIXTag(541)).thenReturn(MaturityDate);
       Mockito.when(parser.createReadWriteFIXGroupTag(541)).thenReturn(MaturityDate);
       Mockito.when(parser.createReadWriteFIXTag(552)).thenReturn(NoSides);
       Mockito.when(parser.createWriteOnlyFIXTag(552)).thenReturn(NoSides);
       Mockito.when(parser.createReadWriteFIXGroupTag(552)).thenReturn(NoSides);
       Mockito.when(parser.createReadWriteFIXTag(559)).thenReturn(SecurityListRequestType);
       Mockito.when(parser.createWriteOnlyFIXTag(559)).thenReturn(SecurityListRequestType);
       Mockito.when(parser.createReadWriteFIXGroupTag(559)).thenReturn(SecurityListRequestType);
       Mockito.when(parser.createReadWriteFIXTag(560)).thenReturn(SecurityRequestResult);
       Mockito.when(parser.createWriteOnlyFIXTag(560)).thenReturn(SecurityRequestResult);
       Mockito.when(parser.createReadWriteFIXGroupTag(560)).thenReturn(SecurityRequestResult);
       Mockito.when(parser.createReadWriteFIXTag(570)).thenReturn(PreviouslyReported);
       Mockito.when(parser.createWriteOnlyFIXTag(570)).thenReturn(PreviouslyReported);
       Mockito.when(parser.createReadWriteFIXGroupTag(570)).thenReturn(PreviouslyReported);
       Mockito.when(parser.createReadWriteFIXTag(571)).thenReturn(TradeReportID);
       Mockito.when(parser.createWriteOnlyFIXTag(571)).thenReturn(TradeReportID);
       Mockito.when(parser.createReadWriteFIXGroupTag(571)).thenReturn(TradeReportID);
       Mockito.when(parser.createReadWriteFIXTag(576)).thenReturn(NoClearingInstructions);
       Mockito.when(parser.createWriteOnlyFIXTag(576)).thenReturn(NoClearingInstructions);
       Mockito.when(parser.createReadWriteFIXGroupTag(576)).thenReturn(NoClearingInstructions);
       Mockito.when(parser.createReadWriteFIXTag(577)).thenReturn(ClearingInstruction);
       Mockito.when(parser.createWriteOnlyFIXTag(577)).thenReturn(ClearingInstruction);
       Mockito.when(parser.createReadWriteFIXGroupTag(577)).thenReturn(ClearingInstruction);
       Mockito.when(parser.createReadWriteFIXTag(856)).thenReturn(TradeReportType);
       Mockito.when(parser.createWriteOnlyFIXTag(856)).thenReturn(TradeReportType);
       Mockito.when(parser.createReadWriteFIXGroupTag(856)).thenReturn(TradeReportType);
       Mockito.when(parser.createReadWriteFIXTag(893)).thenReturn(LastFragment);
       Mockito.when(parser.createWriteOnlyFIXTag(893)).thenReturn(LastFragment);
       Mockito.when(parser.createReadWriteFIXGroupTag(893)).thenReturn(LastFragment);
    }
}
