package com.core.match.sequencer;

import com.core.GenericTest;
import com.core.connector.mold.StubMold64UDPEventSender;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchTestMessages;
import com.core.match.services.security.SecurityType;
import com.core.match.util.MatchPriceUtils;
import com.core.util.store.IndexedStore;
import com.core.util.time.TimerServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/22/15.
 */
public class HandlerTestBase extends GenericTest {
    protected MatchTestMessages messages;
    protected StubMold64UDPEventSender sender;
    protected SequencerBookService books;
    protected SequencerSecurityService securities;
    protected SequencerTraderService traders;
    protected SequencerAccountService accounts;
    protected SequencerContributorService contribs;
    protected SequencerMarketHoursService marketService;
    protected OrderCommandHandler orderCmdHandler;
    protected MiscCommandHandler miscCommandHandler;
    protected SequencerEventHandler eventHandler;
    protected StaticsCommandHandler staticsCommandHandler;
    protected IndexedStore storage;
    protected BackupEventQueueController backupQueue;
    protected StubMold64UDPEventSender backupSenderQueue;

    protected OrderCommandHandler orderEventHandler;

    public HandlerTestBase() {
        super();
        MatchConstants.QTY_MULTIPLIER = 1000;
    }

    // so we don't mess up other tests
    @After
    public void after() {
    	MatchConstants.QTY_MULTIPLIER = 1000000;
    }
    
    @Before
    public void before() {
    	MatchConstants.QTY_MULTIPLIER = 1000;
        this.books = Mockito.mock(SequencerBookService.class);
        this.sender = new StubMold64UDPEventSender();
        this.backupSenderQueue = new StubMold64UDPEventSender();

        this.backupQueue = Mockito.mock(BackupEventQueueController.class);

        this.messages = new MatchTestMessages();
        this.storage = Mockito.mock(IndexedStore.class);

        this.securities = new SequencerSecurityService(log, books);
        this.traders = new SequencerTraderService(log);
        this.accounts = new SequencerAccountService(log);
        this.contribs = new SequencerContributorService(log);

        this.marketService = new SequencerMarketHoursService(this.timeSource, new TimerServiceImpl(log, this.timeSource), new SequencerMarketHoursServiceListener() {
            @Override
            public boolean onOpen() {
                return false;
            }

            @Override
            public boolean onClose() {
                return false;
            }
        }, "00:00", "23:59");

        long spreadTickSize = MatchPriceUtils.toLong(0.001);

        this.securities.addSecurity("2Y", 100000, 1000, SecurityType.BOND.getValue(),0);
        this.securities.addSecurity("3Y", 100000, 1000, SecurityType.BOND.getValue(),0);
        this.securities.addSecurity("10Y2Y", spreadTickSize, 1000, SecurityType.DISCRETE_SPREAD.getValue(),4);

        this.securities.setDisabled(2);

        this.accounts.addAccount("JOHN");
        this.accounts.addAccount("JIM");
        this.accounts.setDisabled(2);

        this.traders.addTrader("JOHN", (short) 1);
        this.traders.addTrader("JIM", (short) 1);
        this.traders.setDisabled(2);

        this.contribs.addContributor("SEQ01");
        this.contribs.addContributor("FOO01");

        this.marketService.forceOpen();

        orderCmdHandler = new OrderCommandHandler(
                log,
                timeSource,
                messages,
                sender,
                contribs,
                securities,
                accounts,
                traders,
                books,
                marketService);

        orderEventHandler = Mockito.mock(OrderCommandHandler.class);
        miscCommandHandler = new MiscCommandHandler(
                log,
                timeSource,
                messages,
                sender,
                contribs,
                securities);

        staticsCommandHandler = new StaticsCommandHandler(
                log,
                timeSource,
                messages,
                sender,
                contribs,
                securities,
                accounts,
                traders
        );

        eventHandler = new SequencerEventHandler(
                log,
                storage,
                books,
                contribs,
                orderEventHandler, backupQueue);

        Mockito.verify(books, new Times(3)).addBook();


    }

    protected int size() {
        return sender.size();
    }

    protected <T> T getFirstMessage(Class<T> cls) {
        ByteBuffer pop = sender.pop();
        return messages.get(cls, pop);
    }
}
