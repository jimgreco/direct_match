package com.core.fix.msgs;

//
// THIS FILE IS AUTO-GENERATED 
//
public class FixMsgTypes {
    public static final char Heartbeat = '0';
    public static final char TestRequest = '1';
    public static final char ResendRequest = '2';
    public static final char Reject = '3';
    public static final char SequenceReset = '4';
    public static final char Logout = '5';
    public static final char ExecutionReport = '8';
    public static final char OrderCancelReject = '9';
    public static final char SecurityDefinitionRequest = 'c';
    public static final char SecurityDefinition = 'd';
    public static final char SecurityStatusRequest = 'e';
    public static final char SecurityStatus = 'f';
    public static final char Logon = 'A';
    public static final char NewOrderSingle = 'D';
    public static final char OrderCancelRequest = 'F';
    public static final char OrderCancelReplaceRequest = 'G';
    public static final char MarketDataRequest = 'V';
    public static final char MarketDataSnapshot = 'W';
    public static final char MarketDataIncrementalRefresh = 'X';
    public static final char MarketDataRequestReject = 'Y';
    public static final char BusinessReject = 'j';
    public static final char SecurityListRequest = 'x';
    public static final char SecurityList = 'y';

    public static String getMsgTypeName(char id) {
        switch(id) {
            case Heartbeat:
                return "Heartbeat";
            case TestRequest:
                return "TestRequest";
            case ResendRequest:
                return "ResendRequest";
            case Reject:
                return "Reject";
            case SequenceReset:
                return "SequenceReset";
            case Logout:
                return "Logout";
            case ExecutionReport:
                return "ExecutionReport";
            case OrderCancelReject:
                return "OrderCancelReject";
            case SecurityDefinitionRequest:
                return "SecurityDefinitionRequest";
            case SecurityDefinition:
                return "SecurityDefinition";
            case SecurityStatusRequest:
                return "SecurityStatusRequest";
            case SecurityStatus:
                return "SecurityStatus";
            case Logon:
                return "Logon";
            case NewOrderSingle:
                return "NewOrderSingle";
            case OrderCancelRequest:
                return "OrderCancelRequest";
            case OrderCancelReplaceRequest:
                return "OrderCancelReplaceRequest";
            case MarketDataRequest:
                return "MarketDataRequest";
            case MarketDataSnapshot:
                return "MarketDataSnapshot";
            case MarketDataIncrementalRefresh:
                return "MarketDataIncrementalRefresh";
            case MarketDataRequestReject:
                return "MarketDataRequestReject";
            case BusinessReject:
                return "BusinessReject";
            case SecurityListRequest:
                return "SecurityListRequest";
            case SecurityList:
                return "SecurityList";
            default:
                return "Unknown";
        }
    }
}

