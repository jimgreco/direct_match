package com.core.match.ouch.controller;

import com.core.match.STPHolder;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchTestMessages;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.msgs.OUCHTestMessages;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchPriceUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.time.LocalDate;

import static com.core.match.db.jdbc.msgs.DBFieldEnum.trader;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class OuchTradeConfirmProcessorTest {
    @Mock
    private TraderService traderService;
    @Mock
    private SpreadPriceProvider priceProvider;
    @Mock
    private OUCHAdapter adaptor;
    private int tradeDate=1234;
    private Bond bond2;
    private Bond bond10;
    private Bond bond30;


    private MultiLegSecurity spread;

    private MultiLegSecurity butterfly;
    private STPHolder<OUCHOrder> holder;
    private MatchTestMessages msgs=new MatchTestMessages();
    private OUCHTestMessages ouchMsgs=new OUCHTestMessages();

    private OUCHOrder ouchOrder;
    private Trader testTrader;
    private Long priceLong=12335L;
    private Long priceLong2=2345L;
    private Long priceLong3=832739L;
    private com.core.match.ouch.msgs.OUCHTradeConfirmationCommand ouchTradeConfirm;


    @Before
    public void setUp() throws Exception {
        bond2=new Bond((short)1,"2Y");
        bond2.setCUSIP("2yrcusip");
        LocalDate date = LocalDate.parse("2018-12-31");
        bond2.setSettlementDate(date);

        bond10=new Bond((short)2,"10Y");
        bond10.setCUSIP("10yrcusip");
        LocalDate date2 = LocalDate.parse("2026-12-31");
        bond10.setSettlementDate(date2);

        bond30=new Bond((short)3,"10Y");
        bond30.setCUSIP("30yrcusip");
        LocalDate date3 = LocalDate.parse("2046-12-31");
        bond30.setSettlementDate(date3);
        spread = new MultiLegSecurity((short)4,"2Y10Y");
        spread.setLeg1(bond2);
        spread.setLeg2(bond10);
        spread.setLeg1Size(4);
        spread.setLeg2Size(1);
        spread.setNumLegs(2);

        butterfly=new MultiLegSecurity((short)5,"2Y10Y30Y");
        butterfly.setLeg1(bond2);
        butterfly.setLeg2(bond10);
        butterfly.setLeg3(bond30);

        butterfly.setLeg1Size(4);
        butterfly.setLeg2Size(4);
        butterfly.setLeg3Size(1);

        butterfly.setNumLegs(3);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(1234L);
        buffer.flip();

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setMatchID(1);
        command.setLastFill(true);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(15);
        ouchOrder=new OUCHOrder();
        ouchOrder.setClOrdID(1234L);
        ouchOrder.setID(1);
        ouchOrder.setSecurityID((short) 1);
        ouchOrder.setTraderID((short) 1);
        holder=new STPHolder<>();
        holder.addFill(ouchOrder,buffer,1234L,command.toEvent());
        ouchTradeConfirm=ouchMsgs.getOUCHTradeConfirmationCommand();

        testTrader=new Trader((short)1,"Trader1");
        when(traderService.get(1)).thenReturn(testTrader);
        when(priceProvider.getPrice(bond2,true)).thenReturn(priceLong);
        when(priceProvider.getPrice(bond10,true)).thenReturn(priceLong2);
        when(priceProvider.getPrice(bond2,true)).thenReturn(priceLong3);
        when(adaptor.getOUCHTradeConfirmationCommand()).thenReturn(ouchTradeConfirm);





    }
    private OuchTradeConfirmProcessor createTarget(){
        return new OuchTradeConfirmProcessor(tradeDate,adaptor,priceProvider,traderService);
    }

    @Test
    public void sendTradeConfirmation_bond_correctFieldsSend()  {
        //Arrange
        OuchTradeConfirmProcessor target=createTarget();
        long timestamp=123456L;

        //Act
        target.sendTradeConfirmation(timestamp,1,holder,bond2);

        //Assert
        assertEquals(timestamp,ouchTradeConfirm.toEvent().getTradeTime());
        assertEquals(15,ouchTradeConfirm.toEvent().getCommissionAmountAsDouble(),0);

        verify(adaptor).send(ouchTradeConfirm);
    }

    @Test
    public void sendTradeConfirmation_spread_correctFieldsSend()  {
        //Arrange
        OuchTradeConfirmProcessor target=createTarget();
        long timestamp=123456L;

        //Act
        target.sendTradeConfirmation(timestamp,1,holder,spread);

        //Assert

        verify(adaptor,times(2)).send(ouchTradeConfirm);
    }

    @Test
    public void sendTradeConfirmation_butterfly_correctFieldsSend()  {
        //Arrange
        OuchTradeConfirmProcessor target=createTarget();
        long timestamp=123456L;

        //Act
        target.sendTradeConfirmation(timestamp,1,holder,butterfly);

        //Assert
        assertEquals((int)15/4,ouchTradeConfirm.toEvent().getCommissionAmountAsDouble(),0);
        assertEquals(15* MatchConstants.QTY_MULTIPLIER/4,ouchTradeConfirm.toEvent().getExecQty(),0);

        verify(adaptor,times(3)).send(ouchTradeConfirm);
    }
}