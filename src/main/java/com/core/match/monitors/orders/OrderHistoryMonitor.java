package com.core.match.monitors.orders;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.monitors.PagedResult;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.order.*;
import com.core.match.services.security.*;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.services.trades.TradeService;
import com.core.match.services.trades.TradeServiceListener;
import com.core.match.util.MatchPriceUtils;
import com.core.match.util.MessageUtils;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.core.util.file.IndexedFile;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;
import com.gs.collections.impl.map.mutable.primitive.IntLongHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Created by jgreco on 12/2/15.
 */
public class OrderHistoryMonitor extends MatchApplication implements
        OrderServiceListener<OrderHistoryMonitorOrder>,
        OrderServiceRejectListener<OrderHistoryMonitorOrder>,TradeServiceListener<OrderHistoryMonitorOrder> {
    private static final int RESULTS_PER_PAGE = 100;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final String ALL_ORDERS_PARAM_VALUE = "all";

    private final ByteBuffer histBuf = ByteBuffer.allocateDirect(20 * 4);
    private final ByteBuffer orderBuf = ByteBuffer.allocateDirect(18 * 4);
    private final ByteBuffer loc = ByteBuffer.allocate(8);
    private final ByteBuffer str = ByteBuffer.allocate(1024);

    private final ContributorService<Contributor> contributors;
    private final SecurityService<BaseSecurity> securities;
    private final TraderService<Trader> traders;
    private final AccountService<Account> accounts;

    private final File msgFile;
    private final IndexedFile textFile;

    private final IntLongHashMap orderIDStartOfChain = new IntLongHashMap();
    private final IntLongHashMap orderIDEndOfChain = new IntLongHashMap();
    private final IntIntHashMap orderIDFileMap = new IntIntHashMap();

    private int nextRejectOrderID = -1;

    private final IntArrayList results1 = new IntArrayList();
    private final IntArrayList results2 = new IntArrayList();

    private final IntArrayList allOrders;
    private final OrderHistoryMonitorStaticsIndex<BaseSecurity> securityMap;
    private final OrderHistoryMonitorStaticsIndex<Contributor> contributorMap;
    private final OrderHistoryMonitorStaticsIndex<Trader> traderMap;
    private final OrderHistoryMonitorStaticsIndex<Account> accountMap;
    private final OrderHistoryMonitorStaticsIndex[] indexes;
    private final String[] values;

    private final IndexedFile orderStore;

    private final List<OrderHistoryMonitorBlotterItem> blotter = new FastList<>();

    @AppConstructor
    public OrderHistoryMonitor(Log log,
                               FileFactory fileFactory,
                               Dispatcher dispatcher,
                               ContributorService<Contributor> contributors,
                               AccountService<Account> accounts,
                               TraderService<Trader> traders,
                               SecurityService<BaseSecurity> securities,
                               @Param(name="Name") String name) throws IOException {
        super(log);

        this.contributors = contributors;
        this.securities = securities;
        this.traders = traders;
        this.accounts = accounts;

        this.allOrders = new IntArrayList();
        this.securityMap = new OrderHistoryMonitorStaticsIndex<>(securities, "security");
        this.contributorMap = new OrderHistoryMonitorStaticsIndex<>(contributors, "contributor");
        this.traderMap = new OrderHistoryMonitorStaticsIndex<>(traders, "trader");
        this.accountMap = new OrderHistoryMonitorStaticsIndex<>(accounts, "account");
        this.indexes = new OrderHistoryMonitorStaticsIndex[] { securityMap, contributorMap, traderMap , accountMap };
        this.values = new String[indexes.length];

        securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) securityMap.create();
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) securityMap.create();
            }
        });
        contributors.addListener((contributor, msg, isNew) -> {
            if (isNew) contributorMap.create();
        });
        traders.addListener((trader, msg, isNew) -> {
            if (isNew) traderMap.create();
        });
        accounts.addListener((account, msg, isNew) -> {
            if (isNew) accountMap.create();
        });

        OrderServiceWithRejectsAll<OrderHistoryMonitorOrder> rejectService = OrderServiceWithRejectsAll.create(OrderHistoryMonitorOrder.class, log, dispatcher);
        OrderService<OrderHistoryMonitorOrder> orderService = rejectService.getOrders();
        orderService.addListener(this);

        TradeService<OrderHistoryMonitorOrder> trades = new TradeService<>(orderService);
        trades.addListener(this);

        this.msgFile = fileFactory.createFile(name + "Messages.bin", "rw");
        this.textFile = new IndexedFile(fileFactory, log, name + "Text.bin");
        this.orderStore = new IndexedFile(fileFactory, log, name + "Orders");
    }

    @Exposed(name="history")
    public OrderHistoryMonitorHistoryResults getHistory(@Param(name="id") int id) {
        OrderHistoryMonitorHistoryResults results = new OrderHistoryMonitorHistoryResults();
        results.order = readOrderFromFile(id);

        long fileLoc = orderIDStartOfChain.getIfAbsent(id, -1);
        while (true) {
            if (fileLoc == -1) {
                // TODO: Cleanup
                results.setPage(0, Math.max(RESULTS_PER_PAGE, results.getRecords().size()), false);
                return results;
            }

            try {
                histBuf.clear();
                msgFile.read(histBuf, fileLoc);
                histBuf.flip();
            } catch (IOException e) {
                return results;
            }

            fileLoc = histBuf.getLong();
            OrderHistoryMonitorHistoryItem item = readHistoryFromBuffer(histBuf);
            results.getRecords().add(item);
        }
    }

    @Exposed(name = "orders")
    public OrderHistoryMonitorResults getOrders(@Param(name = "params") String params,
                                                @Param(name="page") int page,
                                                @Param(name="newestFirst") boolean newestFirst) {
        IntArrayList results;

        if (params.equals(ALL_ORDERS_PARAM_VALUE)) {
            results = allOrders;
        } else {
            setParams(params);

            results1.clear();
            results2.clear();
            results = results1;
            IntArrayList oldResults = null;

            for (int i=0; i<indexes.length; i++) {
                String value = values[i];
                if (value != null && !value.isEmpty()) {
                    indexes[i].search(value, results, oldResults);

                    if (oldResults == null) {
                        oldResults = results2;
                    }
                    IntArrayList temp = results;
                    results = oldResults;
                    oldResults = temp;
                    results.clear();
                }
            }

            if (oldResults != null && oldResults.size() > 0) {
                results = oldResults;
            }
        }

        OrderHistoryMonitorResults orderResults = new OrderHistoryMonitorResults();
        orderResults.setPage(page, RESULTS_PER_PAGE, newestFirst, results.size());
        for (int i=orderResults.getFirstResult(); i<orderResults.getLastResult(); i++) {
            int orderID = results.get(i);
            OrderHistoryMonitorOrderItem item = readOrderFromFile(orderID);
            orderResults.getRecords().add(item);
        }
        return orderResults;
    }

    @Exposed(name = "blotter")
    public BlotterMonitorHolder getBlotter(@Param(name = "page") int page) {
        BlotterMonitorHolder results = new BlotterMonitorHolder();
        results.setPage(
                page,
                RESULTS_PER_PAGE,
                true,
                blotter.size());
        for (int i=results.getFirstResult(); i<results.getLastResult(); i++) {
            results.getRecords().add(blotter.get(i));
        }
        return results;
    }

    private void setParams(@Param(name = "params") String params) {
        for (int i=0; i<values.length; i++) {
            values[i] = null;
        }

        String[] args = params.split(";");
        for (int i=0; i<args.length; i++) {
            String[] args12 = args[i].split(":");
            if (args12.length != 2) {
                throw new CommandException("Invalid params: " + params);
            }

            String key = args12[0];
            String value = args12[1];

            if (key.equalsIgnoreCase("security")) {
                values[0] = value;
            }
            else if (key.equalsIgnoreCase("contributor")) {
                values[1] = value;
            }
            else if (key.equalsIgnoreCase("trader")) {
                values[2] = value;
            }
            else if (key.equalsIgnoreCase("account")) {
                values[3] = value;
            }
        }
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    @Override
    public void onOrder(OrderHistoryMonitorOrder order, MatchOrderEvent msg) {
        order.setIOC(msg.getIOC());
        order.setContributorID(msg.getContributorID());
        order.setCreated(msg.getTimestamp());
        order.setLive(true);

        int clOrdIDIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
            order.setClOrdIDIndex(clOrdIDIndex);
        }

        allOrders.add(order.getID());
        contributorMap.add(msg.getContributorID(), order.getID());
        securityMap.add(order.getSecurityID(), order.getID());
        traderMap.add(order.getTraderID(), order.getID());
        accountMap.add(traders.get(order.getTraderID()).getAccountID(), order.getID());

        orderIDStartOfChain.put(order.getID(), msgFile.size());
        writeHistoryToFile(
                order,
                msg.getMsgType(),
                msg.getContributorID(),
                clOrdIDIndex,
                (char) 0,
                0,
                0,
                msg.getTimestamp());
        writeOrderToFile(order);
    }

    @Override
    public void onCancel(OrderHistoryMonitorOrder order, MatchCancelEvent msg) {
        order.setUpdated(msg.getTimestamp());
        order.setLive(false);

        int clOrdIDIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
            order.setClOrdIDIndex(clOrdIDIndex);
        }

        writeHistoryToFile(
                order,
                msg.getMsgType(),
                msg.getContributorID(),
                clOrdIDIndex,
                (char) 0,
                0,
                0,
                msg.getTimestamp());
        writeOrderToFile(order);
    }

    @Override
    public void onReplace(OrderHistoryMonitorOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        order.setUpdated(msg.getTimestamp());

        int clOrdIDIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
            order.setClOrdIDIndex(clOrdIDIndex);
        }

        writeHistoryToFile(
                order,
                msg.getMsgType(),
                msg.getContributorID(),
                clOrdIDIndex,
                (char) 0,
                0,
                0,
                msg.getTimestamp());
        writeOrderToFile(order);
    }

    @Override
    public void onFill(OrderHistoryMonitorOrder order, MatchFillEvent msg) {
        order.addNotional(msg.getQty() * msg.getPrice());
        order.setUpdated(msg.getTimestamp());

        if (order.isFilled()) {
            order.setLive(false);
        }

        writeHistoryToFile(order, msg.getMsgType(), msg.getContributorID(), -1, (char) 0, msg.getQty(), msg.getPrice(), msg.getTimestamp());
        writeOrderToFile(order);
    }

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        int clOrdIDIndex = -1;
        int textIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
        }
        if (msg.hasText()) {
            textIndex = textFile.getNextIndex();
            textFile.write(msg.getText());
        }

        writeHistoryToFile(
                msg.getTimestamp(),
                msg.getContributorID(),
                msg.getMsgType(),
                msg.getReason(),
                clOrdIDIndex,
                textIndex,
                nextRejectOrderID,
                0,
                0,
                0,
                0,
                0);
        nextRejectOrderID--;
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        int clOrdIDIndex = -1;
        int textIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
        }
        if (msg.hasText()) {
            textIndex = textFile.getNextIndex();
            textFile.write(msg.getText());
        }

        writeHistoryToFile(msg.getTimestamp(),
                msg.getContributorID(),
                msg.getMsgType(),
                msg.getReason(),
                clOrdIDIndex,
                textIndex,
                nextRejectOrderID,
                0,
                0,
                0,
                0,
                0);
        nextRejectOrderID--;
    }

    @Override
    public void onCancelReplaceReject(OrderHistoryMonitorOrder order, MatchCancelReplaceRejectEvent msg) {
        int clOrdIDIndex = -1;
        int textIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
        }
        if (msg.hasText()) {
            textIndex = textFile.getNextIndex();
            textFile.write(msg.getText());
        }

        if (order != null) {
            writeHistoryToFile(order, msg.getMsgType(), msg.getContributorID(), clOrdIDIndex, msg.getReason(), 0, 0, msg.getTimestamp());
        }
        else {
            writeHistoryToFile(
                    msg.getTimestamp(),
                    msg.getContributorID(),
                    msg.getMsgType(),
                    msg.getReason(),
                    clOrdIDIndex,
                    textIndex,
                    nextRejectOrderID,
                    0,
                    0,
                    0,
                    0,
                    0);
            nextRejectOrderID--;
        }
    }

    @Override
    public void onClientCancelReplaceReject(OrderHistoryMonitorOrder order, MatchClientCancelReplaceRejectEvent msg) {
        int clOrdIDIndex = -1;
        int textIndex = -1;
        if (msg.hasClOrdID()) {
            clOrdIDIndex = textFile.getNextIndex();
            textFile.write(msg.getClOrdID());
        }
        if (msg.hasText()) {
            textIndex = textFile.getNextIndex();
            textFile.write(msg.getText());
        }

        if (order != null) {
            writeHistoryToFile(order, msg.getMsgType(), msg.getContributorID(), clOrdIDIndex, msg.getReason(), 0, 0, msg.getTimestamp());
        }
        else {
            writeHistoryToFile(
                    msg.getTimestamp(),
                    msg.getContributorID(),
                    msg.getMsgType(),
                    msg.getReason(),
                    clOrdIDIndex,
                    textIndex,
                    nextRejectOrderID,
                    0,
                    0,
                    0,
                    0,
                    0);
            nextRejectOrderID--;
        }
    }

    private void writeHistoryToFile(OrderHistoryMonitorOrder order, char eventType, short contributorID, int clOrdIDIndex, char rejectReason, int execQty, long execPrice, long timestamp) {
        writeHistoryToFile(
                timestamp,
                contributorID,
                eventType,
                rejectReason,
                clOrdIDIndex,
                -1,
                order.getID(),
                order.getPrice(),
                order.getQty(),
                order.getCumQty(),
                //order.getSecurityID(),
                //order.getTraderID(),
                //order.isBuy(),
                //order.isIOC(),
                execQty,
                execPrice);
    }

    private void writeHistoryToFile(long timestamp,
                                    short contributorID,
                                    char msgType,
                                    char rejectReason,
                                    int clOrdIDIndex,
                                    int textIDIndex,
                                    int id,
                                    long price,
                                    int qty,
                                    int cumQty,
                                    //short securityID,
                                    //short traderID,
                                    //boolean buy,
                                    //boolean ioc,
                                    int execQty,
                                    long execPrice) {
        long lastPosition = orderIDEndOfChain.getIfAbsent(id, -1);
        if (lastPosition != -1) {
            loc.clear();
            loc.putLong(msgFile.size());
            loc.flip();
            try {
                msgFile.write(loc, lastPosition);
            } catch (IOException e) {
                log.error(log.log().add(e));
            }
        }

        orderIDEndOfChain.put(id, msgFile.size());

        histBuf.clear();

        // 2
        histBuf.putLong((long) -1);

        // 2
        histBuf.putLong(timestamp);

        histBuf.put((byte) msgType);
        histBuf.put((byte) rejectReason);
        //histBuf.put((byte) (buy ? 'B' : 'S'));
        //histBuf.put((byte) (ioc ? 'Y' : 'N'));
        histBuf.putShort(contributorID);
        //histBuf.putShort((short) 0);

        //histBuf.putInt(id);

        // 2
        histBuf.putLong(price);

        histBuf.putInt(qty);

        histBuf.putInt(cumQty);

        //histBuf.putShort(securityID);
        //histBuf.putShort(traderID);

        histBuf.putInt(clOrdIDIndex);

        histBuf.putInt(textIDIndex);

        histBuf.putInt(execQty);

        histBuf.putLong(execPrice);

        histBuf.flip();

        try {
            msgFile.write(histBuf);
        } catch (IOException e) {
            log.error(log.log().add(e));
        }
    }

    private OrderHistoryMonitorHistoryItem readHistoryFromBuffer(ByteBuffer buffer) {
        OrderHistoryMonitorHistoryItem item = new OrderHistoryMonitorHistoryItem();

        item.timestamp = formatTime(buffer.getLong());

        item.msgType = (char)buffer.get();
        item.rejectReason = (char)buffer.get();
        item.contributor = contributors.get(buffer.getShort()).getName();

        item.price = buffer.getLong();

        item.qty = buffer.getInt();

        item.cumQty = buffer.getInt();

        {
            int clOrdIDIndex = buffer.getInt();
            if (clOrdIDIndex < 0) {
                item.clOrdID = "";
            } else {
                str.clear();
                textFile.read(clOrdIDIndex, str);
                str.flip();
                item.clOrdID = BinaryUtils.toString(str);
            }
        }

        {
            int textIndex = buffer.getInt();
            if (textIndex < 0) {
                item.text = "";
            } else {
                str.clear();
                textFile.read(textIndex, str);
                str.flip();
                item.text = BinaryUtils.toString(str);
            }
        }

        item.execQty = buffer.getInt();

        item.execPrice = buffer.getLong();
        return item;
    }

    private String formatTime(long timestamp) {
        return TimeUtils.toLocalDateTime(timestamp, MessageUtils.zoneID()).format(formatter);
    }

    private void writeOrderToFile(OrderHistoryMonitorOrder order) {
        orderBuf.clear();

        orderBuf.putInt(order.getID());

        orderBuf.putInt(order.getClOrdIDIndex());

        // 2
        orderBuf.putLong(order.getCreated());

        // 2
        orderBuf.putLong(order.getUpdated());

        // 2
        orderBuf.putLong(order.getPrice());

        orderBuf.putInt(order.getQty());

        orderBuf.putInt(order.getCumQty());

        // 2
        orderBuf.putLong(order.getNotional());

        orderBuf.putShort(order.getTraderID());
        orderBuf.putShort(order.getSecurityID());

        orderBuf.putShort(order.getContributorID());
        orderBuf.putShort((short) 0); // filled

        orderBuf.put((byte) (order.isBuy() ? 'B' : 'S'));
        orderBuf.put((byte) (order.isIOC() ? 'Y' : 'N'));
        orderBuf.put((byte) (order.isLive() ? 'Y' : 'N'));
        orderBuf.put((byte) 0); // filled

        orderBuf.flip();

        orderIDFileMap.put(order.getID(), orderStore.getNextIndex());
        orderStore.write(orderBuf);
    }

    private OrderHistoryMonitorOrderItem readOrderFromFile(int orderID) {
        int index = orderIDFileMap.getIfAbsent(orderID, -1);
        if (index < 0) {
            return null;
        }

        orderBuf.clear();
        orderStore.read(index, orderBuf);
        orderBuf.flip();

        OrderHistoryMonitorOrderItem item = new OrderHistoryMonitorOrderItem();
        item.id = orderBuf.getInt();

        int clOrdIDIndex = orderBuf.getInt();
        str.clear();
        textFile.read(clOrdIDIndex, str);
        str.flip();
        item.clOrdID = BinaryUtils.toString(str);

        item.created = formatTime(orderBuf.getLong());
        long updated = orderBuf.getLong();

        if (updated != 0) {
            item.updated = formatTime(updated);
        }

        item.price = orderBuf.getLong();

        item.qty = orderBuf.getInt();

        item.cumQty = orderBuf.getInt();

        item.notional = orderBuf.getLong();

        Trader trader = traders.get(orderBuf.getShort());
        if (trader != null) {
            item.trader = trader.getName();
            item.account = accounts.get(trader.getAccountID()).getName();
        }
        BaseSecurity security = securities.get(orderBuf.getShort());
        if (security != null) {
            item.security = security.getName();
        }

        Contributor contributor = contributors.get(orderBuf.getShort());
        if (contributor != null) {
            item.contributor = contributor.getName();
        }
        orderBuf.getShort();

        item.buy = orderBuf.get() == 'B';
        item.ioc = orderBuf.get() == 'Y';
        item.status = orderBuf.get() == 'Y' ? "LIVE" : "DEAD";
        orderBuf.get();

        return item;
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {

    }

    @Override
    public void onTrade(long timestamp, int matchID, long execPrice, int execQty, OrderHistoryMonitorOrder associatedOrder, boolean aggressor) {
        Trader trader = traders.get(associatedOrder.getTraderID());
        Account account = accounts.get(trader.getAccountID());
        BaseSecurity security = securities.get(associatedOrder.getSecurityID());
        double priceDbl = MatchPriceUtils.toDouble(execPrice);
        String price32 = MatchPriceUtils.to32ndPrice(execPrice);
        String time = formatTime(timestamp);

        int clOrdIDIndex = associatedOrder.getClOrdIDIndex();
        str.clear();
        textFile.read(clOrdIDIndex, str);
        str.flip();
        String clOrdID = BinaryUtils.toString(str);

        OrderHistoryMonitorBlotterItem item = new OrderHistoryMonitorBlotterItem(
                associatedOrder.getID(),
                clOrdID,
                account.getName(),
                trader.getName(),
                associatedOrder.isBuy(),
                execQty,
                security.getName(),
                priceDbl,
                price32,
                matchID,
                time,
                aggressor);

        blotter.add(item);
    }

    @Override
    public void onMatch(long timestamp, int matchID, long execPrice, int execQty, short securityID) {

    }

    public static class OrderHistoryMonitorHistoryResults extends PagedResult<OrderHistoryMonitorHistoryItem> {
        private OrderHistoryMonitorOrderItem order;

        public OrderHistoryMonitorOrderItem getOrder() {
            return order;
        }
    }

    public static class OrderHistoryMonitorResults extends PagedResult<OrderHistoryMonitorOrderItem> {
    }

    public static class BlotterMonitorHolder extends PagedResult<OrderHistoryMonitorBlotterItem> {
    }
}
