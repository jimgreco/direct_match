package com.core.match.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.services.limit.LimitBook;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 7/19/15.
 */
class SequencerLimitBookService implements SequencerBookService {
    private final BuyBookComparator buyBookComparator = new BuyBookComparator();
    private final SellBookComparator sellBookComparator = new SellBookComparator();
    private final List<LimitBook<SequencerOrder>> books = new FastList<>();
    private final SequencerOrderService orders;
    private final Log log;
    private SequencerBookServiceListener listener;

    private int nextOrderID = 1;
    private int nextMatchID = 1;
    private int nextExternalOrderID = 1;

    public SequencerLimitBookService(Log log, SequencerOrderService orders) {
        this.orders = orders;
        this.log = log;
    }

    @Override
	public void setListener(SequencerBookServiceListener listener) {
        this.listener = listener;
    }


    @Override
    public SequencerOrder buildOrder(MatchOrderEvent msg) {
        int orderID;
        orderID = nextOrderID++;

        SequencerOrder order = orders.create(orderID);
        order.id = orderID;
        order.buy = msg.getBuy();
        order.qty = msg.getQty();
        order.securityID = msg.getSecurityID();
        order.price = msg.getPrice();
        order.ioc = msg.getIOC();
        order.externalOrderID = nextExternalOrderID++;
        order.inBook = isPassive(order) && !order.isIOC();
        return order;
    }

    @Override
    public boolean buildReplace(SequencerOrder order, MatchReplaceEvent msg) {
        long newPrice = msg.getPrice();
        long oldPrice = order.getPrice();
        int newQty = msg.getQty();
        int oldQty = order.getQty();

        order.price = newPrice;
        order.qty = newQty;

        if (newPrice == oldPrice && newQty <= oldQty) {
            // replace down
            // already passive and externalOrderID doesn't change
            return false;
        }
        else {
            // full refresh and lose q-spot
            order.externalOrderID = nextExternalOrderID++;
            order.inBook = isPassive(order);
            return true;
        }
    }

    @Override
    public void addOrder(SequencerOrder order) {
        LimitBook<SequencerOrder> book = getBook(order.getSecurityID());

        checkMatches(book, order);

        if (!order.isIOC() && order.getRemainingQty() > 0) {
        	// aggressing order had quantity remaining
            book.insertOrder(order);
        }
    }

    protected void checkMatches(LimitBook<SequencerOrder> book, SequencerOrder aggressiveOrder) {
        int matches = 0;
        long aggressivePrice = aggressiveOrder.getPrice();
        LimitBookComparator comparator = aggressiveOrder.isBuy() ? buyBookComparator : sellBookComparator;

        // walk the book of opposite - side prices
        SequencerOrder restingOrder = comparator.getTopOfBook(book);
        while (!aggressiveOrder.isFilled() && restingOrder != null && !comparator.isFirstPriceAggressive(aggressivePrice, restingOrder.getPrice())) {
            // we stop when iterating when
            // a) the aggressive order is filled
            // b) we have exhausted the queue
            // c) we have reached a price level that no longer matches

            long matchPrice = restingOrder.getPrice();
            int matchQty = Math.min(aggressiveOrder.getRemainingQty(), restingOrder.getRemainingQty());
            if( matchQty <= 0 ) {
                log.error(log.log().add("Created match for invalid qty - [ ID1: ").add(aggressiveOrder.getID()).add("], [ ID2: ").add(restingOrder.getID()).add("]"));
                return;
            }

            // either the aggressive order is done or the resting order is done and i'm done at the price level.
            SequencerOrder nextOrder = restingOrder.next();
            boolean aggressiveDone = matchQty >= aggressiveOrder.getRemainingQty();
            boolean doneAtPriceLevel = nextOrder == null || comparator.isFirstPriceAggressive(aggressivePrice, nextOrder.getPrice());
            boolean fillsDone = aggressiveDone || ( matchQty >= restingOrder.getRemainingQty() && doneAtPriceLevel );

            match(aggressiveOrder, restingOrder, matchQty, matchPrice, ++matches, fillsDone);

            if (restingOrder.getRemainingQty() <= 0) {
                cancelOrder(restingOrder);
            }
            restingOrder = nextOrder;
        }
    }

    @Override
    public void cancelOrder(SequencerOrder order)  {
        LimitBook<SequencerOrder> book = getBook(order.getSecurityID());
        book.removeOrder(order);
        deleteOrder(order);
    }

    @Override
    public void replaceOrder(SequencerOrder order, boolean reinsert) {
        if (reinsert) {
            // our method of reinsertion into the book does a full removal (fast) and full insertion (slow)
            LimitBook<SequencerOrder> book = getBook(order.getSecurityID());
            book.removeOrder(order);
            addOrder(order);
        }
        // otherwise if we did a replace down, we keep our q-spot
        // and we don't need to do a reinsertion into the book
    }

    private boolean isPassive(SequencerOrder order) {
        LimitBook<SequencerOrder> book = getBook(order.getSecurityID());
        if (order.isBuy()) {
            // is the bid lower than the best offer?
            long bestOffer = book.getBestOfferPrice();
            return bestOffer == 0 || order.getPrice() < bestOffer;
        }
        else {
            // is the offer higher than the best bid?
            long bestBid = book.getBestBidPrice();
            return bestBid == 0 || order.getPrice() > bestBid;
        }
    }

    private void match(SequencerOrder aggressiveOrder, SequencerOrder restingOrder, int matchQty, long matchPrice, int fillCount, boolean lastFill) {
        restingOrder.cumQty += matchQty;
        aggressiveOrder.cumQty += matchQty;
        restingOrder.inBook = restingOrder.getRemainingQty() > 0;
        aggressiveOrder.inBook = lastFill && !aggressiveOrder.isIOC() && aggressiveOrder.getRemainingQty() > 0;

        listener.onMatch(nextMatchID++,
                restingOrder.getID(),
                aggressiveOrder.getID(),
                matchQty,
                matchPrice,
                fillCount,
                lastFill,
                restingOrder.inBook,
                aggressiveOrder.inBook);
    }

    public int getNextExternalOrderID() {
        return nextExternalOrderID;
    }

    @Override
    public void deleteOrder(SequencerOrder order) {
        if(order.getID() == 0) {
            log.error(log.log().add("Attempting to delete order which was already deleted."));
        }

        orders.remove(order.getID());
        orders.delete(order);
    }

    public boolean add(short id) {
        if (id < MatchConstants.STATICS_START_INDEX || id > size() + MatchConstants.STATICS_START_INDEX) {
            return false;
        }

        if (id == size() + MatchConstants.STATICS_START_INDEX) {
            LimitBook<SequencerOrder> book = new LimitBook<>(MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER);
            books.add(id - MatchConstants.STATICS_START_INDEX, book);
        }
        return true;
    }

    @Override
	public short addBook() {
        short id = (short) (books.size() + MatchConstants.STATICS_START_INDEX);
        add(id);
        return id;
    }

    public LimitBook<SequencerOrder> getBook(int id) {
        return isValid(id) ? books.get(id - MatchConstants.STATICS_START_INDEX) : null;
    }

    @Override
    public SequencerOrder getOrder(int id) {
        return orders.get(id);
    }

    public long size() {
        return books.size();
    }

    public boolean isValid(int id) {
        return id >= MatchConstants.STATICS_START_INDEX && id < size() + MatchConstants.STATICS_START_INDEX;
    }

    public int getNextOrderID() {
        return nextOrderID;
    }

    public long getNextMatchID() {
        return nextMatchID;
    }

    @Override
	public long numOrders() {
        return orders.size();
    }

    public String printBook(short id) {
        LimitBook<SequencerOrder> book = getBook(id);
        if (book != null) {
            return book.toString();
        }
        return "<Unknown Book>";
    }
}
