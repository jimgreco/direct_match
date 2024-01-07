package com.core.match.ouch.client;

import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHCancelRejectedEvent;
import com.core.match.ouch.msgs.OUCHCanceledEvent;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHRejectedEvent;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by hli on 9/17/15.
 */
@SuppressWarnings("boxing")
public class OUCHClientOrderServiceTest {

    private SystemOutLog log;
    private OUCHClientOrderServiceListener mockListener1;
    private OUCHClientOrderServiceListener mockListener2;
    private OUCHClientOrderService target;
    long clOrderid;


    @Before
    public void setup(){
        SimulatedTimeSource timeSource = new SimulatedTimeSource();
        String name = "OUCHTest";
        log = new SystemOutLog("CORE01", name, timeSource);
        mockListener1=mock(OUCHClientOrderServiceListener.class);
        mockListener2=mock(OUCHClientOrderServiceListener.class);
        target=createTarget();
        target.addListener(mockListener1);
        target.addListener(mockListener2);
        clOrderid=123;

    }

    private OUCHClientOrderService createTarget(){
        return new OUCHClientOrderService(log);
    }



    @Test
    public void testOnOUCHReplaced_oldIdExistsInOrderMap_invokeCallbacksOnListeners() {
        OUCHAcceptedEvent orderAcceptMessage=new MyOrderAcceptMessage();

        target.onOUCHAccepted(orderAcceptMessage);
        verify(mockListener1).onOUCHAccepted(any(OUCHAcceptedEvent.class), any(OUCHClientOrder.class));

        OUCHReplacedEvent msg=mock(OUCHReplacedEvent.class);
        when(msg.getOldClOrdId()).thenReturn(clOrderid);

        //Act
        target.onOUCHReplaced(msg);

        verify(mockListener1).onOUCHReplaced(any(OUCHReplacedEvent.class), any(OUCHClientOrder.class));
        verify(mockListener2).onOUCHReplaced(any(OUCHReplacedEvent.class),any(OUCHClientOrder.class));


    }

    @Test
    public void testOnOUCHCanceled() {
        OUCHAcceptedEvent orderAcceptMessage=new MyOrderAcceptMessage();

        target.onOUCHAccepted(orderAcceptMessage);
        verify(mockListener1).onOUCHAccepted(any(OUCHAcceptedEvent.class), any(OUCHClientOrder.class));

        OUCHCanceledEvent msg=mock(OUCHCanceledEvent.class);
        when(msg.getClOrdID()).thenReturn(clOrderid);

        //Act
        target.onOUCHCanceled(msg);

        verify(mockListener1).onOUCHCanceled(any(OUCHCanceledEvent.class), any(OUCHClientOrder.class));
        verify(mockListener2).onOUCHCanceled(any(OUCHCanceledEvent.class), any(OUCHClientOrder.class));

    }

	@Test
    public void testOnOUCHCancelRejected_idFoundInOrderMap_invokeCallback() {
        OUCHAcceptedEvent orderAcceptMessage=new MyOrderAcceptMessage();

        target.onOUCHAccepted(orderAcceptMessage);
        verify(mockListener1).onOUCHAccepted(any(OUCHAcceptedEvent.class), any(OUCHClientOrder.class));

        OUCHCancelRejectedEvent msg=mock(OUCHCancelRejectedEvent.class);
        when(msg.getClOrdID()).thenReturn(clOrderid);

        //Act
        target.onOUCHCancelRejected(msg);

        verify(mockListener1).onOUCHCancelRejected(any(OUCHCancelRejectedEvent.class), any(OUCHClientOrder.class));
        verify(mockListener2).onOUCHCancelRejected(any(OUCHCancelRejectedEvent.class), any(OUCHClientOrder.class));

    }

    @Test
    public void testOnOUCHCancelRejected_orderIdNotInMap_DoNotInvokeCallbackOnListeners() {


        OUCHCancelRejectedEvent msg=mock(OUCHCancelRejectedEvent.class);
        long id=1;
        when(msg.getClOrdID()).thenReturn(id);

        //Act
        target.onOUCHCancelRejected(msg);

        verify(mockListener1, never()).onOUCHCancelRejected(any(OUCHCancelRejectedEvent.class), any(OUCHClientOrder.class));
        verify(mockListener2, never()).onOUCHCancelRejected(any(OUCHCancelRejectedEvent.class), any(OUCHClientOrder.class));

    }

    @Test
    public void testOnOUCHRejected_invokeCallbacks() {
        OUCHRejectedEvent msg=mock(OUCHRejectedEvent.class);

        target.onOUCHRejected(msg);

        verify(mockListener1).onOUCHRejected(any(OUCHRejectedEvent.class));
        verify(mockListener2).onOUCHRejected(any(OUCHRejectedEvent.class));
    }

    @Test
    public void testOnOUCHFill_idInOrderMap_invokeListenersCallback() {
        OUCHAcceptedEvent orderAcceptMessage=new MyOrderAcceptMessage();

        target.onOUCHAccepted(orderAcceptMessage);
        verify(mockListener1).onOUCHAccepted(any(OUCHAcceptedEvent.class), any(OUCHClientOrder.class));

        OUCHFillEvent msg=mock(OUCHFillEvent.class);
        when(msg.getClOrdID()).thenReturn(clOrderid);

        //Act
        target.onOUCHFill(msg);

        verify(mockListener1).onOUCHFill(any(OUCHFillEvent.class), any(OUCHClientOrder.class));
        verify(mockListener2).onOUCHFill(any(OUCHFillEvent.class), any(OUCHClientOrder.class));
    }

    private class MyOrderAcceptMessage implements OUCHAcceptedEvent{

        public MyOrderAcceptMessage()
		{
			// TODO Auto-generated constructor stub
		}

		@Override
        public OUCHAcceptedCommand toCommand() {
            return null;
        }

        @Override
        public char getSide() {
            return OUCHConstants.Side.Buy;
        }

        @Override
        public boolean hasSide() {
            return true;
        }

        @Override
        public int getQty() {
            return 100;
        }

        @Override
        public double getQtyAsQty() {
            return getQty();
        }

        @Override
        public boolean hasQty() {
            return true;
        }

        @Override
        public ByteBuffer getSecurity() {
            return null;
        }

        @Override
        public int getSecurityLength() {
            return 0;
        }

        @Override
        public String getSecurityAsString() {
            return "2Y";
        }

        @Override
        public boolean hasSecurity() {
            return true;
        }

        @Override
        public long getPrice() {
            return 1000;
        }

        @Override
        public String getPriceAs32nd() {
            return null;
        }

        @Override
        public double getPriceAsDouble() {
            return 0;
        }

        @Override
        public boolean hasPrice() {
            return true;
        }

        @Override
        public char getTimeInForce() {
            return OUCHConstants.TimeInForce.DAY;
        }

        @Override
        public boolean hasTimeInForce() {
            return true;
        }

        @Override
        public int getMaxDisplayedQty() {
            return -1;
        }

        @Override
        public double getMaxDisplayedQtyAsQty() {
            return -1;
        }

        @Override
        public boolean hasMaxDisplayedQty() {
            return false;
        }

        @Override
        public ByteBuffer getTrader() {
            return null;
        }

        @Override
        public int getTraderLength() {
            return 0;
        }

        @Override
        public String getTraderAsString() {
            return "hli";
        }

        @Override
        public boolean hasTrader() {
            return true;
        }

        @Override
        public ByteBuffer getRawBuffer() {
            return null;
        }

        @Override
        public String getMsgName() {
            return null;
        }

        @Override
        public char getMsgType() {
            return 0;
        }

        @Override
        public boolean hasMsgType() {
            return false;
        }

        @Override
        public long getClOrdID() {
            return clOrderid;
        }

        @Override
        public boolean hasClOrdID() {
            return true;
        }
    }



}