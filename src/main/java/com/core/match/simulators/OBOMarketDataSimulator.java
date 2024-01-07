package com.core.match.simulators;

import com.core.app.AppConstructor;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.order.AbstractOrder;
import com.core.match.services.order.Order;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchPriceUtils;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/**
 * User: jgreco
 */
public class OBOMarketDataSimulator extends MatchApplication implements
        TimerHandler,
        OrderServiceListener<OBOMarketDataSimulator.MDSimOrder>,
        AllCommandsClearedListener
{
    private final ByteBuffer temp = ByteBuffer.allocate(32);

    private final TimerService timerService;
    private final MatchMessages msgs;

    private final SecurityService<BaseSecurity> securities;
    private final TraderService<Trader> traders;

    private final FastList<Action> actions = new FastList<>(1000000);
    private final Map<String, MDSimOrder> clOrdIDOrderMap = new UnifiedMap<>();
    private final String[] tradersToSendOn;

    private final Random random = new Random();
    private int position = 0;
    private int speedMultiplier;
    private final boolean repeat;
    private int timer;
    private HeartbeatNumberField positionHeartbeat;
    private HeartbeatNumberField speedHeartbeat;

    @AppConstructor
    public OBOMarketDataSimulator(Log log,
                                  TimerService timerService,
                                  Dispatcher dispatcher,
                                  MatchCommandSender sender,
                                  MatchMessages msgs,
                                  SecurityService<BaseSecurity> securities,
                                  TraderService<Trader> traders,
                                  @Param(name = "FileName") String fileName,
                                  @Param(name = "NumOrders") int numOrders,
                                  @Param(name = "SpeedMultiplier") int multiplier,
                                  @Param(name = "Repeat") boolean repeat,
                                  @Param(name = "ValidTraders") String validTraders) throws IOException, ParseException {
        super(log, sender);

        this.tradersToSendOn = validTraders.split(",");
        this.msgs = msgs;
        this.timerService = timerService;

        this.repeat = repeat;
        this.speedMultiplier = multiplier;

        this.securities = securities;
        this.traders = traders;

        OrderService orders = OrderService.create(MDSimOrder.class, log, dispatcher, 10000);
        orders.addListener(this);

        sender.addAllCommandsClearedListener(this);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line;

        // header
        reader.readLine();

        // read in header
        int i=0;
        while ((line = reader.readLine()) != null && (numOrders == 0 || i < numOrders)) {
            String[] components = line.split(",");
            long nanos;


            String time = components[0];
            if(time.equals("0")){
                nanos=System.nanoTime()+random.nextInt(5)*TimeUtils.NANOS_PER_SECOND;
            }else{
                format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                Date parse = format.parse(time);
                nanos = TimeUtils.NANOS_PER_MILLI * parse.getTime();
            }
            i++;

            String security = components[1];
            String actionType = components[2];
            String clOrdId = components[3];
            String price = components[4];
            String qty = components[5];
            String side = components[6];
            String totalQty = components[7];

            addAction(nanos, security, actionType, price, qty, clOrdId, side, totalQty);
        }

        log.info(log.log().add("Read ").add(i).add(" actions"));
    }

    private void addAction(long nanos, String security, String actionType, String price, String qty, String clOrdId, String side, String totalQty) {
        int qtyInt = MatchPriceUtils.roundLotToCoreQty(Integer.parseInt(qty));
        int totalQtyInt = MatchPriceUtils.roundLotToCoreQty(Integer.parseInt(totalQty));

        Action action = new Action();
        action.nanos = nanos;
        action.security = security;
        action.action = ActionType.lookup(actionType);
        action.clOrdID = clOrdId;
        action.price = MatchPriceUtils.toLong(Double.parseDouble(price));
        action.qty = Math.max(qtyInt, totalQtyInt);
        action.buy = side.equalsIgnoreCase("B");

        if (action.action != ActionType.UNKNOWN) {
            actions.add(action);
        }
    }

    @Exposed(name = "speed")
    public void setSpeedMultiplier(@Param(name="multiplier") int mult) {
        speedMultiplier = mult;
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return msg.getContributorID() == getContribID();
    }

    @Override
    public void onOrder(MDSimOrder order, MatchOrderEvent msg) {
        order.setClOrdID(msg.getClOrdIDAsString());

        clOrdIDOrderMap.put(order.getClOrdID(), order);
    }

    @Override
    public void onFill(MDSimOrder order, MatchFillEvent msg) {
        if (order.getRemainingQty() <= 0) {
            clOrdIDOrderMap.put(order.getClOrdID(), null);
        }
    }

    @Override
    public void onReplace(MDSimOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {}

    @Override
    public void onCancel(MDSimOrder order, MatchCancelEvent msg) {
        clOrdIDOrderMap.put(order.getClOrdID(), null);
    }

    @Override
    protected void onActive() {
        onTimer(0, 0);
    }

    @Override
    protected void onPassive() {
        timer = timerService.cancelTimer(timer);
    }

    private void sendOrder(boolean buy, int qty, BaseSecurity security, long price, String clOrdID, short traderID) {
        if (clOrdIDOrderMap.containsKey(clOrdID)) {
            return;
        }


        MatchOrderCommand order = msgs.getMatchOrderCommand();
        order.setTraderID(traderID);
        order.setBuy(buy);
        order.setSecurityID(security.getID());
        order.setQty(qty);
        order.setPrice(price);
        order.setClOrdID(clOrdID);

        send(order);
    }

    private void cancelOrder(Order<MDSimOrder> order) {
        MatchCancelCommand cancel = msgs.getMatchCancelCommand();
        cancel.setOrderID(order.getID());

        send(cancel);
    }

    private void replaceOrder(Order<MDSimOrder> order, int newQty, long newPrice) {
        if (order.getPrice() == newPrice && order.getQty() == newQty) {
            return;
        }

        if (newQty <= order.getCumQty()) {
            return;
        }

        MatchReplaceCommand replace = msgs.getMatchReplaceCommand();
        replace.setOrderID(order.getID());
        replace.setQty(newQty);
        replace.setPrice(newPrice);

        send(replace);
    }

    @Override
    public void onAllCommandsCleared() {
        //onTimer();
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (!canSend()) {
            // could be a lot of reasons we can't send, just set a timer
            timer = timerService.scheduleTimer(TimeUtils.NANOS_PER_MILLI, this);
            return;
        }

        if (position >= actions.size() - 1) {
            cleanup();
            return;
        }

        Action action = actions.get(position++);
        BaseSecurity security = securities.get(action.security);



        if (action.action == ActionType.ORDER) {
            Trader trader = this.traders.get(tradersToSendOn[random.nextInt(tradersToSendOn.length)]);
            if(trader==null){
                log.error(log.log().add("Trader is null, order not sent.  ClOrdID ").add(action.clOrdID));
            } else if(security == null){
                log.error(log.log().add("Security is null, order not sent.  ClOrdID ").add(action.clOrdID));
            } else {
                sendOrder(action.buy, action.qty, security, action.price, action.clOrdID, trader.getID());
            }
        }
        else if (action.action == ActionType.CANCEL) {
            MDSimOrder order = clOrdIDOrderMap.get(action.clOrdID);
            if (order != null) {
                cancelOrder(order);
            }
        }
        else if (action.action == ActionType.REPLACE) {
            MDSimOrder order = clOrdIDOrderMap.get(action.clOrdID);
            if (order != null) {
                replaceOrder(order, action.qty, action.price);
            }
        }

        Action nextAction = actions.get(position);
        long delay = Math.abs(nextAction.nanos - action.nanos);
        timer = timerService.scheduleTimer(delay / speedMultiplier, this);
    }

    private void cleanup() {
        if (!repeat) {
            return;
        }

        timerService.scheduleTimer(TimeUtils.NANOS_PER_MICRO, this);

        if (clOrdIDOrderMap.size() > 0) {
            for (Map.Entry<String, MDSimOrder> entry : clOrdIDOrderMap.entrySet()) {
                MDSimOrder order = entry.getValue();
                if (order == null) {
                    clOrdIDOrderMap.remove(entry.getKey());
                } else {
                    cancelOrder(order);
                    return;
                }
            }
        }
        else {
            position = 0;
            log.debug(log.log().add("Cleaned up old orders.  Starting a new!"));
        }
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister register) {
        positionHeartbeat = register.addNumberField("Sim", HeartBeatFieldIDEnum.SimulatorPosition);
        speedHeartbeat = register.addNumberField("Sim", HeartBeatFieldIDEnum.SimulatorSpeed);

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register) {
        positionHeartbeat.set(position);
        speedHeartbeat.set(speedMultiplier);
    }


    public static class MDSimOrder extends AbstractOrder<MDSimOrder> {
        private String clOrdID;

        public String getClOrdID() {
            return clOrdID;
        }

        public void setClOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
        }
    }

    public class Action {
        public boolean buy;
        public ActionType action;
        public String security;
        public String clOrdID;
        public long price;
        public int qty;
        public long nanos;
    }

    public enum ActionType {
        ORDER,
        CANCEL,
        REPLACE,
        UNKNOWN;

        public static ActionType lookup(String name) {
            if(name.equalsIgnoreCase("A")) {
                return ORDER;
            }
            else if (name.equalsIgnoreCase("X")) {
                return CANCEL;
            }
            else if (name.equalsIgnoreCase("R")) {
                return REPLACE;
            }
            else {
                return UNKNOWN;
            }
        }
    }
}