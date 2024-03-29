package com.core.fix.msgs;

import com.core.util.ArrayUtils;
import com.core.util.log.Log;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
 */
public class FixDispatcher {
    private final Log log;
	
    public FixDispatcher(Log log) {
        this.log = log;
    } 

    private FixHeartbeatListener[] HeartbeatListeners = new FixHeartbeatListener[0];
    private FixTestRequestListener[] TestRequestListeners = new FixTestRequestListener[0];
    private FixResendRequestListener[] ResendRequestListeners = new FixResendRequestListener[0];
    private FixRejectListener[] RejectListeners = new FixRejectListener[0];
    private FixSequenceResetListener[] SequenceResetListeners = new FixSequenceResetListener[0];
    private FixLogoutListener[] LogoutListeners = new FixLogoutListener[0];
    private FixExecutionReportListener[] ExecutionReportListeners = new FixExecutionReportListener[0];
    private FixSecurityDefinitionRequestListener[] SecurityDefinitionRequestListeners = new FixSecurityDefinitionRequestListener[0];
    private FixSecurityStatusRequestListener[] SecurityStatusRequestListeners = new FixSecurityStatusRequestListener[0];
    private FixLogonListener[] LogonListeners = new FixLogonListener[0];
    private FixNewOrderSingleListener[] NewOrderSingleListeners = new FixNewOrderSingleListener[0];
    private FixOrderCancelRequestListener[] OrderCancelRequestListeners = new FixOrderCancelRequestListener[0];
    private FixOrderCancelReplaceRequestListener[] OrderCancelReplaceRequestListeners = new FixOrderCancelReplaceRequestListener[0];
    private FixMarketDataRequestListener[] MarketDataRequestListeners = new FixMarketDataRequestListener[0];
    private FixMarketDataSnapshotListener[] MarketDataSnapshotListeners = new FixMarketDataSnapshotListener[0];
    private FixMarketDataIncrementalRefreshListener[] MarketDataIncrementalRefreshListeners = new FixMarketDataIncrementalRefreshListener[0];
    private FixMarketDataRequestRejectListener[] MarketDataRequestRejectListeners = new FixMarketDataRequestRejectListener[0];
    private FixBusinessRejectListener[] BusinessRejectListeners = new FixBusinessRejectListener[0];
    private FixSecurityListRequestListener[] SecurityListRequestListeners = new FixSecurityListRequestListener[0];
    private FixSecurityListListener[] SecurityListListeners = new FixSecurityListListener[0];

    public boolean onMessage(char msgType) {
        switch (msgType) {
            case '0':
                if (HeartbeatListeners.length == 0) {
                    return false;
                }
                for (FixHeartbeatListener HeartbeatListener : HeartbeatListeners) {
                    HeartbeatListener.onFixHeartbeat();
                }
                return true;
            case '1':
                if (TestRequestListeners.length == 0) {
                    return false;
                }
                for (FixTestRequestListener TestRequestListener : TestRequestListeners) {
                    TestRequestListener.onFixTestRequest();
                }
                return true;
            case '2':
                if (ResendRequestListeners.length == 0) {
                    return false;
                }
                for (FixResendRequestListener ResendRequestListener : ResendRequestListeners) {
                    ResendRequestListener.onFixResendRequest();
                }
                return true;
            case '3':
                if (RejectListeners.length == 0) {
                    return false;
                }
                for (FixRejectListener RejectListener : RejectListeners) {
                    RejectListener.onFixReject();
                }
                return true;
            case '4':
                if (SequenceResetListeners.length == 0) {
                    return false;
                }
                for (FixSequenceResetListener SequenceResetListener : SequenceResetListeners) {
                    SequenceResetListener.onFixSequenceReset();
                }
                return true;
            case '5':
                if (LogoutListeners.length == 0) {
                    return false;
                }
                for (FixLogoutListener LogoutListener : LogoutListeners) {
                    LogoutListener.onFixLogout();
                }
                return true;
            case '8':
                if (ExecutionReportListeners.length == 0) {
                    return false;
                }
                for (FixExecutionReportListener ExecutionReportListener : ExecutionReportListeners) {
                    ExecutionReportListener.onFixExecutionReport();
                }
                return true;
            case 'c':
                if (SecurityDefinitionRequestListeners.length == 0) {
                    return false;
                }
                for (FixSecurityDefinitionRequestListener SecurityDefinitionRequestListener : SecurityDefinitionRequestListeners) {
                    SecurityDefinitionRequestListener.onFixSecurityDefinitionRequest();
                }
                return true;
            case 'e':
                if (SecurityStatusRequestListeners.length == 0) {
                    return false;
                }
                for (FixSecurityStatusRequestListener SecurityStatusRequestListener : SecurityStatusRequestListeners) {
                    SecurityStatusRequestListener.onFixSecurityStatusRequest();
                }
                return true;
            case 'A':
                if (LogonListeners.length == 0) {
                    return false;
                }
                for (FixLogonListener LogonListener : LogonListeners) {
                    LogonListener.onFixLogon();
                }
                return true;
            case 'D':
                if (NewOrderSingleListeners.length == 0) {
                    return false;
                }
                for (FixNewOrderSingleListener NewOrderSingleListener : NewOrderSingleListeners) {
                    NewOrderSingleListener.onFixNewOrderSingle();
                }
                return true;
            case 'F':
                if (OrderCancelRequestListeners.length == 0) {
                    return false;
                }
                for (FixOrderCancelRequestListener OrderCancelRequestListener : OrderCancelRequestListeners) {
                    OrderCancelRequestListener.onFixOrderCancelRequest();
                }
                return true;
            case 'G':
                if (OrderCancelReplaceRequestListeners.length == 0) {
                    return false;
                }
                for (FixOrderCancelReplaceRequestListener OrderCancelReplaceRequestListener : OrderCancelReplaceRequestListeners) {
                    OrderCancelReplaceRequestListener.onFixOrderCancelReplaceRequest();
                }
                return true;
            case 'V':
                if (MarketDataRequestListeners.length == 0) {
                    return false;
                }
                for (FixMarketDataRequestListener MarketDataRequestListener : MarketDataRequestListeners) {
                    MarketDataRequestListener.onFixMarketDataRequest();
                }
                return true;
            case 'W':
                if (MarketDataSnapshotListeners.length == 0) {
                    return false;
                }
                for (FixMarketDataSnapshotListener MarketDataSnapshotListener : MarketDataSnapshotListeners) {
                    MarketDataSnapshotListener.onFixMarketDataSnapshot();
                }
                return true;
            case 'X':
                if (MarketDataIncrementalRefreshListeners.length == 0) {
                    return false;
                }
                for (FixMarketDataIncrementalRefreshListener MarketDataIncrementalRefreshListener : MarketDataIncrementalRefreshListeners) {
                    MarketDataIncrementalRefreshListener.onFixMarketDataIncrementalRefresh();
                }
                return true;
            case 'Y':
                if (MarketDataRequestRejectListeners.length == 0) {
                    return false;
                }
                for (FixMarketDataRequestRejectListener MarketDataRequestRejectListener : MarketDataRequestRejectListeners) {
                    MarketDataRequestRejectListener.onFixMarketDataRequestReject();
                }
                return true;
            case 'j':
                if (BusinessRejectListeners.length == 0) {
                    return false;
                }
                for (FixBusinessRejectListener BusinessRejectListener : BusinessRejectListeners) {
                    BusinessRejectListener.onFixBusinessReject();
                }
                return true;
            case 'x':
                if (SecurityListRequestListeners.length == 0) {
                    return false;
                }
                for (FixSecurityListRequestListener SecurityListRequestListener : SecurityListRequestListeners) {
                    SecurityListRequestListener.onFixSecurityListRequest();
                }
                return true;
            case 'y':
                if (SecurityListListeners.length == 0) {
                    return false;
                }
                for (FixSecurityListListener SecurityListListener : SecurityListListeners) {
                    SecurityListListener.onFixSecurityList();
                }
                return true;
            default:
                log.error(log.log().add("Unknown message type: ").add(msgType));
                return false;
        }
    }

    public void subscribe(Object listener) {
        subscribe(listener, listener.getClass());
    }

    private void subscribe(Object message, Class<?> clz) {
        if (clz == null)
            return;

        Class<?>[] interfaces = clz.getInterfaces();


        for (Class<?> intCls : interfaces) {
          if (intCls.getInterfaces().length > 0){
             subscribe(message,intCls);
          }
            if (intCls.equals(FixHeartbeatListener.class)) {
                HeartbeatListeners = append(HeartbeatListeners, (FixHeartbeatListener) message);  
            }
            if (intCls.equals(FixTestRequestListener.class)) {
                TestRequestListeners = append(TestRequestListeners, (FixTestRequestListener) message);  
            }
            if (intCls.equals(FixResendRequestListener.class)) {
                ResendRequestListeners = append(ResendRequestListeners, (FixResendRequestListener) message);  
            }
            if (intCls.equals(FixRejectListener.class)) {
                RejectListeners = append(RejectListeners, (FixRejectListener) message);  
            }
            if (intCls.equals(FixSequenceResetListener.class)) {
                SequenceResetListeners = append(SequenceResetListeners, (FixSequenceResetListener) message);  
            }
            if (intCls.equals(FixLogoutListener.class)) {
                LogoutListeners = append(LogoutListeners, (FixLogoutListener) message);  
            }
            if (intCls.equals(FixSecurityDefinitionRequestListener.class)) {
                SecurityDefinitionRequestListeners = append(SecurityDefinitionRequestListeners, (FixSecurityDefinitionRequestListener) message);
            }
            if (intCls.equals(FixSecurityStatusRequestListener.class)) {
                SecurityStatusRequestListeners = append(SecurityStatusRequestListeners, (FixSecurityStatusRequestListener) message);
            }
            if (intCls.equals(FixExecutionReportListener.class)) {
                ExecutionReportListeners = append(ExecutionReportListeners, (FixExecutionReportListener) message);
            }
            if (intCls.equals(FixLogonListener.class)) {
                LogonListeners = append(LogonListeners, (FixLogonListener) message);  
            }
            if (intCls.equals(FixNewOrderSingleListener.class)) {
                NewOrderSingleListeners = append(NewOrderSingleListeners, (FixNewOrderSingleListener) message);  
            }
            if (intCls.equals(FixOrderCancelRequestListener.class)) {
                OrderCancelRequestListeners = append(OrderCancelRequestListeners, (FixOrderCancelRequestListener) message);  
            }
            if (intCls.equals(FixOrderCancelReplaceRequestListener.class)) {
                OrderCancelReplaceRequestListeners = append(OrderCancelReplaceRequestListeners, (FixOrderCancelReplaceRequestListener) message);  
            }
            if (intCls.equals(FixMarketDataRequestListener.class)) {
                MarketDataRequestListeners = append(MarketDataRequestListeners, (FixMarketDataRequestListener) message);  
            }
            if (intCls.equals(FixMarketDataSnapshotListener.class)) {
                MarketDataSnapshotListeners = append(MarketDataSnapshotListeners, (FixMarketDataSnapshotListener) message);  
            }
            if (intCls.equals(FixMarketDataIncrementalRefreshListener.class)) {
                MarketDataIncrementalRefreshListeners = append(MarketDataIncrementalRefreshListeners, (FixMarketDataIncrementalRefreshListener) message);  
            }
            if (intCls.equals(FixMarketDataRequestRejectListener.class)) {
                MarketDataRequestRejectListeners = append(MarketDataRequestRejectListeners, (FixMarketDataRequestRejectListener) message);  
            }
            if (intCls.equals(FixBusinessRejectListener.class)) {
                BusinessRejectListeners = append(BusinessRejectListeners, (FixBusinessRejectListener) message);  
            }
            if (intCls.equals(FixSecurityListRequestListener.class)) {
                SecurityListRequestListeners = append(SecurityListRequestListeners, (FixSecurityListRequestListener) message);  
            }
            if (intCls.equals(FixSecurityListListener.class)) {
                SecurityListListeners = append(SecurityListListeners, (FixSecurityListListener) message);  
            }
        }

        subscribe(message, clz.getSuperclass());
    }
 
    private static <T> T[] append(T[] collection, T item) {
        if (!isSubscribed(collection, item)) {
            return ArrayUtils.append(collection, item); 
        }
        return collection;
    }

    private static <T> boolean isSubscribed(T[] collection, Object item) {
        for (T i : collection) {
           if (i == item)
              return true;
        }
        return false;
    }
}

