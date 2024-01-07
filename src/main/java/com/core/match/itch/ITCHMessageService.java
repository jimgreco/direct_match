package com.core.match.itch;

import com.core.match.itch.msgs.ITCHMessages;
import com.core.match.itch.msgs.ITCHOrderCancelCommand;
import com.core.match.itch.msgs.ITCHOrderCommand;
import com.core.match.itch.msgs.ITCHOrderExecutedCommand;
import com.core.match.itch.msgs.ITCHSecurityCommand;
import com.core.match.itch.msgs.ITCHSystemCommand;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.events.SystemEventListener;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.DisplayedOrderServiceListener;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.util.TimeUtils;

import java.nio.ByteBuffer;

import static com.core.match.itch.msgs.ITCHConstants.EventCode.EndOfTradingSession;
import static com.core.match.itch.msgs.ITCHConstants.EventCode.StartOfTradingSession;
import static com.core.match.itch.msgs.ITCHConstants.SecuritySource.CUSIP;
import static com.core.match.itch.msgs.ITCHConstants.Side.Buy;
import static com.core.match.itch.msgs.ITCHConstants.Side.Sell;
import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

/**
 * Created by jgreco on 6/12/15.
 */
public class ITCHMessageService implements
        SystemEventListener,
        SecurityServiceListener<BaseSecurity>,
        DisplayedOrderServiceListener<DisplayedOrder> {
    private final ITCHServerListener listener;
    private final ITCHMessages messages;

    public ITCHMessageService(ITCHMessages messages,
                              DisplayedOrderService<DisplayedOrder> orders,
                              SecurityService<BaseSecurity> securities,
                              SystemEventService eventService,
                              ITCHServerListener listener) {
        this.messages = messages;
        this.listener = listener;

        orders.addListener(this);
        securities.addListener(this);
        eventService.addListener(this);
    }

    @Override
    public void onOpen(long timestamp) {
        sendSystem(StartOfTradingSession, timestamp);
    }

    @Override
    public void onClose(long timestamp) {
        sendSystem(EndOfTradingSession, timestamp);
    }

    private void sendSystem(char type, long timestamp) {
        ITCHSystemCommand cmd = messages.getITCHSystemCommand(getBuffer());
        cmd.setSecurityID((short) 0);
        cmd.setTimestamp(timestamp);
        cmd.setEventCode(type);

        listener.onMessage(cmd);
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        ITCHSecurityCommand cmd = messages.getITCHSecurityCommand(getBuffer());
        cmd.setSecurityID(security.getID());
        cmd.setTimestamp(msg.getTimestamp());
        cmd.setName(security.getName());
        cmd.setSecurityType(security.getType().getITCHType());

        cmd.setCoupon(security.getCoupon());
        cmd.setMaturityDate(TimeUtils.toDateInt(security.getMaturityDate()));
        cmd.setSecurityReference(security.getCUSIP());
        cmd.setSecurityReferenceSource(CUSIP);

        listener.onMessage(cmd);
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        //TODO:Currently do not support Multileg instument
    }

    private ByteBuffer getBuffer() {
        return listener.getBuffer();
    }

    @Override
    public void onDisplayedOrder(DisplayedOrder order, long timestamp) {
        ITCHOrderCommand cmd = messages.getITCHOrderCommand(getBuffer());
        cmd.setSecurityID(order.getSecurityID());
        cmd.setTimestamp(timestamp);
        cmd.setOrderID(order.getExternalOrderID());
        cmd.setPrice(order.getPrice());
        cmd.setQty(QTY_MULTIPLIER * order.getRemainingQty());
        cmd.setSide(order.isBuy() ? Buy : Sell);

        listener.onMessage(cmd);
    }

    @Override
    public void onDisplayedReduced(DisplayedOrder order, long oldPrice, int qtyReduced, boolean dead, long timestamp) {
        ITCHOrderCancelCommand cmd = messages.getITCHOrderCancelCommand(getBuffer());
        cmd.setSecurityID(order.getSecurityID());
        cmd.setTimestamp(timestamp);
        cmd.setOrderID(order.getExternalOrderID());
        cmd.setQtyCanceled(QTY_MULTIPLIER * qtyReduced);

        listener.onMessage(cmd);
    }

    @Override
    public void onDisplayedFill(DisplayedOrder order, int fillQty, long fillPrice, int matchID, long timestamp) {
        ITCHOrderExecutedCommand cmd = messages.getITCHOrderExecutedCommand(getBuffer());
        cmd.setSecurityID(order.getSecurityID());
        cmd.setTimestamp(timestamp);
        cmd.setOrderID(order.getExternalOrderID());
        cmd.setPrice(fillPrice);
        cmd.setQty(QTY_MULTIPLIER * fillQty);
        cmd.setMatchID(matchID);

        listener.onMessage(cmd);
    }
}
