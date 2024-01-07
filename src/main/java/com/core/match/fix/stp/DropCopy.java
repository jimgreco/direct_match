package com.core.match.fix.stp;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.MatchCommandSender;
import com.core.match.fix.orders.FIXOrderOutput;
import com.core.match.fix.orders.FIXQtyMode;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

import java.io.IOException;

/**
 * User: jgreco
 */
public class DropCopy extends FixSTP implements OrderServiceRejectListener<FIXSTPOrder> {
	protected final Connector connector;
    protected final FIXOrderOutput<FIXSTPOrder> fixOutput;

    @AppConstructor
	public DropCopy(Log log,
					TCPSocketFactory socketFactory,
					FileFactory fileFactory,
					TimerService timeFactory,
					TimeSource timeSource,
					TraderService<Trader> traders,
					SecurityService<BaseSecurity> securityService,
					AccountService<Account> accountService,
					Dispatcher dispatcher,
					MatchCommandSender sender,
					MatchMessages messages,
					FixServerTcpConnectorFactory fixServerTcpConnectorFactory,
					Connector connector,
					MatchBBOBookService refBboBookService,
					@Param(name = "Port") int port,
					@Param(name = "SenderCompId") String senderCompID,
					@Param(name = "TargetCompId") String targetCompID,
					@Param(name = "Accounts") String accounts) throws IOException {
		super(log, socketFactory, fileFactory, timeFactory, timeSource, traders, securityService, accountService, dispatcher, sender, messages,refBboBookService, fixServerTcpConnectorFactory,port, 4, senderCompID, targetCompID, accounts);

		this.connector = connector;
        this.fixOutput = getFIXOutput();

		orders.addRejectListener(this);
	}

	protected FIXOrderOutput<FIXSTPOrder> getFIXOutput() {
		return new FIXOrderOutput<>(parser, store, FIXQtyMode.Notional, traders, accounts, securities);
	}

	@Override
	public void onOrder(FIXSTPOrder order, MatchOrderEvent msg) {
        fixOutput.writeAcceptedExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq());
	}

	@Override
	public void onCancel(FIXSTPOrder order, MatchCancelEvent msg) {
        fixOutput.writeCanceledExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq(), msg.getOrigClOrdID());
	}

	@Override
	public void onReplace(FIXSTPOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
		fixOutput.writeReplacedExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq(), msg.getOrigClOrdID());
	}

	@Override
	public void onFill(FIXSTPOrder order, MatchFillEvent msg) {
        fixOutput.writeFilledExecutionReport(msg.getTimestamp(), order, msg.getMatchID(), msg.getQty(), msg.getPrice());

        // send the Trade Capture Report
        super.onFill(order, msg);
	}

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
		fixOutput.writeOrderReject(msg, connector.getCurrentSeq());
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
		fixOutput.writeClientOrderReject(msg, connector.getCurrentSeq());
    }

	@Override
	public void onCancelReplaceReject(FIXSTPOrder order, MatchCancelReplaceRejectEvent msg) {
		fixOutput.writeCancelReplaceReject(order, msg);
	}

	@Override
	public void onClientCancelReplaceReject(FIXSTPOrder order, MatchClientCancelReplaceRejectEvent msg) {
		fixOutput.writeClientCancelReplaceReject(order, msg);
	}
}
