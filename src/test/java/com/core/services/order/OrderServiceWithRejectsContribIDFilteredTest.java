package com.core.services.order;

import com.core.match.GenericAppTest;
import com.core.match.msgs.*;
import com.core.match.ouch.OUCHOrder;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.OrderServiceWithRejectsContribIDFiltered;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by liuli on 4/21/2016.
 */
public class OrderServiceWithRejectsContribIDFilteredTest extends GenericAppTest<OUCHOrder> {
    private SystemOutLog log;
    private OrderServiceRejectListener mockListener;
    private OrderServiceWithRejectsContribIDFiltered target;

    public OrderServiceWithRejectsContribIDFilteredTest() {
        super(OUCHOrder.class);
    }

    @Before
    public void setup(){
        target = createTarget();

        mockListener=mock(OrderServiceRejectListener.class);
        target.addRejectListener(mockListener);
        target.onContributorDefined((short)1, "Contributor1");
    }

    private OrderServiceWithRejectsContribIDFiltered createTarget(){
        SimulatedTimeSource timeSource = new SimulatedTimeSource();
        String name = "OrderServiceWithRejectsContribIDFilteredTest";
        log = new SystemOutLog("CORE01", name, timeSource);
        return new OrderServiceWithRejectsContribIDFiltered(OUCHOrder.class, log, dispatcher, 1000);
    }

    @Test
    public void testOnContributorDefined_AddContributor_Added(){
        target.onContributorDefined((short)3, "Contributor3");

        MatchOrderCommand cmd = msgs.getMatchOrderCommand();
        cmd.setContributorID((short)3);
        assertTrue(target.isInterested(cmd.toEvent()));
    }

    @Test
    public void testOnMatchCancelReplaceReject_MessageFromContributor1_Invoked(){
        MatchCancelReplaceRejectCommand cmd = msgs.getMatchCancelReplaceRejectCommand();
        cmd.setContributorID((short)1);

        target.onMatchCancelReplaceReject(cmd.toEvent());
        verify(mockListener).onCancelReplaceReject(any(OUCHOrder.class), any(MatchCancelReplaceRejectEvent.class));
    }

    @Test
    public void testOnMatchCancelReplaceReject_MessageFromContributor2_NotInvoked(){
        MatchCancelReplaceRejectCommand cmd = msgs.getMatchCancelReplaceRejectCommand();
        cmd.setContributorID((short)2);

        target.onMatchCancelReplaceReject(cmd.toEvent());
        verify(mockListener,never()).onCancelReplaceReject(any(OUCHOrder.class), any(MatchCancelReplaceRejectEvent.class));
    }

    @Test
    public void testOnMatchClientCancelReplaceReject_MessageFromContributor1_Invoked(){
        MatchClientCancelReplaceRejectCommand cmd = msgs.getMatchClientCancelReplaceRejectCommand();
        cmd.setContributorID((short)1);

        target.onMatchClientCancelReplaceReject(cmd.toEvent());
        verify(mockListener).onClientCancelReplaceReject(any(OUCHOrder.class), any(MatchClientCancelReplaceRejectEvent.class));
    }

    @Test
    public void testOnMatchClientCancelReplaceReject_MessageFromContributor2_NotInvoked(){
        MatchClientCancelReplaceRejectCommand cmd = msgs.getMatchClientCancelReplaceRejectCommand();
        cmd.setContributorID((short)2);

        target.onMatchClientCancelReplaceReject(cmd.toEvent());
        verify(mockListener,never()).onClientCancelReplaceReject(any(OUCHOrder.class), any(MatchClientCancelReplaceRejectEvent.class));
    }

    @Test
    public void testOnMatchClientOrderReject_MessageFromContributor1_Invoked(){
        MatchClientOrderRejectCommand cmd = msgs.getMatchClientOrderRejectCommand();
        cmd.setContributorID((short)1);

        target.onMatchClientOrderReject(cmd.toEvent());
        verify(mockListener).onClientOrderReject(any(MatchClientOrderRejectEvent.class));
    }

    @Test
    public void testOnMatchClientOrderReject_MessageFromContributor2_NotInvoked(){
        MatchClientOrderRejectCommand cmd = msgs.getMatchClientOrderRejectCommand();
        cmd.setContributorID((short)2);

        target.onMatchClientOrderReject(cmd.toEvent());
        verify(mockListener,never()).onClientOrderReject(any(MatchClientOrderRejectEvent.class));
    }

    @Test
    public void testOnMatchOrderReject_MessageFromContributor1_Invoked(){
        MatchOrderRejectCommand cmd = msgs.getMatchOrderRejectCommand();
        cmd.setContributorID((short)1);

        target.onMatchOrderReject(cmd.toEvent());
        verify(mockListener).onOrderReject(any(MatchOrderRejectEvent.class));
    }

    @Test
    public void testOnMatchOrderReject_MessageFromContributor2_NotInvoked(){
        MatchOrderRejectCommand cmd = msgs.getMatchOrderRejectCommand();
        cmd.setContributorID((short)2);

        target.onMatchOrderReject(cmd.toEvent());
        verify(mockListener,never()).onOrderReject(any(MatchOrderRejectEvent.class));
    }
}
