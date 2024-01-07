package com.core.match.sequencer;

import com.core.connector.mold.Mold64UDPEventSender;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelListener;
import com.core.match.msgs.MatchCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectListener;
import com.core.match.msgs.MatchClientOrderRejectCommand;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectListener;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchConstants.OrderRejectReason;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderListener;
import com.core.match.msgs.MatchOrderRejectCommand;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchReplaceListener;
import com.core.match.services.security.SecurityType;
import com.core.sequencer.BaseCommandHandler;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/22/15.
 */
class OrderCommandHandler extends BaseCommandHandler implements
        MatchOrderListener,
        MatchReplaceListener,
        MatchCancelListener,
        MatchClientOrderRejectListener,
        MatchClientCancelReplaceRejectListener,
        SequencerBookServiceListener
{
    private final ByteBuffer temp = ByteBuffer.allocate(256);
    private final MatchMessages messages;
    private final SequencerBookService books;
    private final ByteStringBuffer buffer = new ByteStringBuffer();
    private final MarketHoursService marketHours;
    private final SequencerSecurityService securities;
    private final SequencerTraderService traders;
    private final SequencerAccountService accounts;

    public OrderCommandHandler(Log log,
                               TimeSource timeSource,
                               MatchMessages messages,
                               Mold64UDPEventSender sender,
                               SequencerContributorService contributors,
                               SequencerSecurityService securities,
                               SequencerAccountService accounts,
                               SequencerTraderService traders,
                               SequencerBookService books,
                               MarketHoursService marketHours) {
        super(log, timeSource, sender, contributors);

        this.messages = messages;
        this.books = books;
        this.marketHours = marketHours;
        this.securities = securities;
        this.traders = traders;
        this.accounts = accounts;

    }

    @Override
    public void onMatchOrder(MatchOrderEvent msg) {
        buffer.clear();

        // do validation of the order command
        if (commonEventCheck(msg)) {
            return;
        }

        short traderID = msg.getTraderID();
        short traderAccountID = traders.getAccountID(msg.getTraderID());
        short securityID = msg.getSecurityID();
        long price = msg.getPrice();
        int qty = msg.getQty();
        boolean ioc = msg.getIOC();

        // validate start time
        if (!marketHours.isMarketOpen())
        {
            buffer.add("Market not open");
            sendOrderReject(msg, buffer, OrderRejectReason.TradingSystemClosed);
            return;
        }

        // validate the system has capacity
        if (books.numOrders() >= MatchConstants.MAX_LIVE_ORDERS)
        {
            buffer.add("New order would exceed max live orders ").add(MatchConstants.MAX_LIVE_ORDERS);
            sendOrderReject(msg, buffer, OrderRejectReason.TradingSystemNotAcceptingOrders);
            return;
        }

        // validate trader
        if (!traders.isValid(traderID))
        {
            buffer.add("Invalid TraderID: ").add(traderID);
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidTrader);
            return;
        }

        // validate account
        if (!accounts.isValid(traderAccountID))
        {
            buffer.add("Invalid Account: ").add(traderAccountID);
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidAccount);
            return;
        }

        // validate security
        if (!securities.isValid(securityID))
        {
            buffer.add("Invalid Security: ").add(securityID);
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidSecurity);
            return;
        }

        if (accounts.isDisabled(traderAccountID))
        {
            buffer.add("Account disabled: ").add(traderAccountID);
            sendOrderReject(msg, buffer, OrderRejectReason.AccountDisabled);
            return;
        }

        if (traders.isDisabled(traderID))
        {
            buffer.add("Trader disabled: ").add(traderID);
            sendOrderReject(msg, buffer, OrderRejectReason.TraderDisabled);
            return;
        }

        if (securities.isDisabled(securityID))
        {
            buffer.add("Security disabled: ").add(securityID);
            sendOrderReject(msg, buffer, OrderRejectReason.SecurityDisabled);
            return;
        }

        // validate price
        if (securities.getType(securityID)== SecurityType.BOND.getValue() && price <= 0)
        {
            buffer.add("Price less than 0");
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidPrice);
            return;
        }

        // validate minimum increment for qty spreads
        if (securities.getType(securityID)== SecurityType.DISCRETE_SPREAD.getValue() && msg.getQty()% securities.getMinimumSize(securityID)   !=0)
        {
            buffer.add("Qty is not in minimum increments");
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }

        // validate price is increment of ticksize
        long tickSize = securities.getTickSize(securityID);
        if (price % tickSize != 0)
        {
            buffer.add("Price not multiple of tick size");
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidPrice);
            return;
        }

        // validate lot sizes
        int lotSize = securities.getLotSize(securityID);

        // validate qty
        if (qty <= 0)
        {
            buffer.add("Qty <= 0");
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }
        if (qty % lotSize != 0) {
            buffer.add("Qty not multiple of lot size");
            sendOrderReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }

        // validation done
        // Now the order is official!
        SequencerOrder order = books.buildOrder(msg);

        MatchOrderCommand orderEvent = messages.getMatchOrderCommand(sender.startMessage());
        orderEvent.copy(msg);
        orderEvent.setOrderID(order.getID());
        orderEvent.setExternalOrderID(order.getExternalOrderID());
        orderEvent.setInBook(order.isInBook());

        // broadcast to everyone
        sendMessage(orderEvent, false);

        // do insertion into the book
        books.addOrder(order);

        if (ioc) {
            if (order.getRemainingQty() > 0) {
                MatchCancelCommand cancel = messages.getMatchCancelCommand(sender.startMessage());
                cancel.setOrderID(order.getID());
                sendMsgFromSeq(cancel, false);
            }
            books.deleteOrder(order);
        }
        else if (order.getRemainingQty() <= 0) {
            // regular order that is fully filled
            books.deleteOrder(order);
        }

        // flush any remaining fills
        sender.flush();
    }

    @Override
    public void onMatchReplace(MatchReplaceEvent msg) {
        buffer.clear();

        // do validation of the replace command
        if (commonEventCheck(msg)) {
            return;
        }

        int orderID = msg.getOrderID();
        SequencerOrder order = books.getOrder(orderID);
        // do we even know about the order?
        if (order == null)
        {
            buffer.add("Unknown order on replace. OrderID=").add(orderID);
            sendReplaceReject(msg, buffer, OrderRejectReason.UnknownOrderID);
            return;
        }

        long newPrice = msg.getPrice();
        int newQty = msg.getQty();

        // validate price
        if (newPrice <= 0)
        {
            buffer.add("Price less than 0");
            sendReplaceReject(msg, buffer, OrderRejectReason.InvalidPrice);
            return;
        }

        // validate lot sizes
        int lotSize = this.securities.getLotSize(order.getSecurityID());

        // validate qty
        if (newQty <= 0) {
            buffer.add("Qty less than 0");
            sendReplaceReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }
        if (newQty % lotSize != 0) {
            buffer.add("Qty not multiple of lot size");
            sendReplaceReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }

        // make sure we aren't replacing the qty to less than what's already been filled
        if (newQty <= order.getCumQty())
        {
            buffer.add("Qty <= CumQty. OrderID=").add(orderID);
            sendReplaceReject(msg, buffer, OrderRejectReason.InvalidQuantity);
            return;
        }

        if (newQty == order.getQty() && newPrice == order.getPrice())
        {
            buffer.add("No change in qty or price. OrderID=").add(orderID);
            char reason = newQty == order.getQty() ? OrderRejectReason.InvalidQuantity : OrderRejectReason.InvalidPrice;
            sendReplaceReject(msg, buffer, reason);
            return;
        }

        // validate price is increment of tick size
        long tickSize = this.securities.getTickSize(order.getSecurityID());
        if (newPrice % tickSize != 0)
        {
            buffer.add("Price not multiple of tick size. NewPrice=").addPrice(newPrice, MatchConstants.IMPLIED_DECIMALS)
                    .add(", TickSize=").addPrice(tickSize, MatchConstants.IMPLIED_DECIMALS);
            sendReplaceReject(msg, buffer, OrderRejectReason.InvalidPrice);
            return;
        }

        // validation done
        // Now the replace is official
        //Inserting 0 because we are generating the event
        boolean reinsert = books.buildReplace(order, msg);

        MatchReplaceCommand replace = messages.getMatchReplaceCommand(sender.startMessage());
        replace.copy(msg);
        replace.setExternalOrderID(order.getExternalOrderID());
        replace.setInBook(order.isInBook());
        sendMessage(replace, false);

        // replace in the book
        books.replaceOrder(order, reinsert);

        // send out any remaining fill messages
        sender.flush();
    }

    @Override
    public void onMatchCancel(MatchCancelEvent msg) {
        // do cancel command validation
        buffer.clear();

        if (commonEventCheck(msg)) {
            return;
        }

        int orderID = msg.getOrderID();

        SequencerOrder order = books.getOrder(orderID);
        // do we even know about this order?
        if (order == null) {
            buffer.add("Could not find order for OrderID: ").add(orderID);
            sendCancelReject(msg, buffer, OrderRejectReason.UnknownOrderID);
            return;
        }

        // send cancel event
        MatchCancelCommand cancel = messages.getMatchCancelCommand(sender.startMessage());
        cancel.copy(msg);
        sendMessage(cancel, true);

        // done validating
        // remove from the limit book
        books.cancelOrder(order);
    }

    public boolean forceCancel(int orderID) {
        SequencerOrder order = books.getOrder(orderID);
        // do we even know about this order?
        if (order == null) {
            return false;
        }

        // send cancel event
        MatchCancelCommand cancel = messages.getMatchCancelCommand(sender.startMessage());
        cancel.setOrderID(orderID);
        sendMsgFromSeq(cancel, true);

        // done validating
        // remove from the limit book
        books.cancelOrder(order);
        return true;
    }

    @Override
    public void onMatchClientOrderReject(MatchClientOrderRejectEvent msg)
    {
        buffer.clear();

        if (commonEventCheck(msg)) {
            return;
        }

        // There isn't really validation on a client order reject
        // The client could have sent anything so we just want to echo that back usually
        MatchClientOrderRejectCommand reject = messages.getMatchClientOrderRejectCommand(sender.startMessage());
        reject.copy(msg);

        sendMessage(reject, true);
    }

    @Override
    public void onMatchClientCancelReplaceReject(MatchClientCancelReplaceRejectEvent msg)
    {
        buffer.clear();

        if (commonEventCheck(msg)) {
            return;
        }

        // There isn't really validation on a client replace reject
        // The client could have sent anything so we just want to echo that back usually
        MatchClientCancelReplaceRejectCommand reject = messages.getMatchClientCancelReplaceRejectCommand(sender.startMessage());
        reject.copy(msg);

        sendMessage(reject, true);
    }

    @Override
    public void onMatch(int matchID, int restingOrderID, int aggressiveOrderID, int qty, long price, int fillCount, boolean lastFill, boolean restingInBook, boolean aggressiveInBook) {
        if (fillCount % 10 == 0)
        {
            // we need to make sure we aren't overloading the packet with too many fills
            // if we have more then ten matches (twenty fills) then go ahead and flush and create a new packate
            sender.flush();
        }

        MatchFillCommand fill = messages.getMatchFillCommand(sender.startMessage());
        fill.setOrderID(restingOrderID);
        fill.setQty(qty);
        fill.setPrice(price);
        fill.setMatchID(matchID);
        fill.setLastFill(false);
        fill.setPassive(true);
        fill.setInBook(restingInBook);
        sendMsgFromSeq(fill, false);

        fill = messages.getMatchFillCommand(sender.startMessage());
        fill.setOrderID(aggressiveOrderID);
        fill.setQty(qty);
        fill.setPrice(price);
        fill.setMatchID(matchID);
        fill.setLastFill(lastFill);
        fill.setPassive(false);
        fill.setInBook(aggressiveInBook);
        sendMsgFromSeq(fill, false);
    }

    public void sendOrderReject(MatchOrderEvent msg, ByteStringBuffer buffer, char reason)
    {
        ByteBuffer underlying = buffer.getUnderlyingBuffer();
        log.error(log.log().add(underlying));

        MatchOrderRejectCommand reject = messages.getMatchOrderRejectCommand(sender.startMessage());
        reject.setContributorID(msg.getContributorID());
        reject.setContributorSeq(msg.getContributorSeq());
        reject.setTraderID(msg.getTraderID());
        reject.setClOrdID(msg.getClOrdID());
        reject.setSecurityID(msg.getSecurityID());
        reject.setBuy(msg.getBuy());
        reject.setReason(reason);

        temp.clear();
        BinaryUtils.copy(temp, underlying).flip();

        reject.setText(temp);
        sendMessage(reject, true);
    }

    public void sendCancelReject(MatchCancelEvent msg, ByteStringBuffer buffer, char reason)
    {
        ByteBuffer underlying = buffer.getUnderlyingBuffer();
        log.error(log.log().add(underlying));

        MatchCancelReplaceRejectCommand reject = messages.getMatchCancelReplaceRejectCommand(sender.startMessage());
        reject.setContributorID(msg.getContributorID());
        reject.setContributorSeq(msg.getContributorSeq());
        reject.setOrderID(msg.getOrderID());
        reject.setClOrdID(msg.getClOrdID());
        reject.setOrigClOrdID(msg.getOrigClOrdID());
        reject.setIsReplace(false);
        reject.setText(underlying);
        reject.setReason(reason);

        sendMessage(reject, true);
    }

    public void sendReplaceReject(MatchReplaceEvent msg, ByteStringBuffer buffer, char reason)
    {
        ByteBuffer underlying = buffer.getUnderlyingBuffer();
        log.error(log.log().add(underlying));

        MatchCancelReplaceRejectCommand reject = messages.getMatchCancelReplaceRejectCommand(sender.startMessage());
        reject.setContributorID(msg.getContributorID());
        reject.setContributorSeq(msg.getContributorSeq());
        reject.setOrderID(msg.getOrderID());
        reject.setClOrdID(msg.getClOrdID());
        reject.setOrigClOrdID(msg.getOrigClOrdID());
        reject.setIsReplace(true);
        reject.setReason(reason);
        reject.setText(underlying);
        sendMessage(reject, true);
    }
}
