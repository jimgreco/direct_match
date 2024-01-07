package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUIBlotter;
import com.core.match.util.MatchPriceUtils;
import com.core.util.BinaryUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by hli on 2/23/16.
 */
public class GUIBlotterTest {
    @Test
    public void testJSON() {
        GUIBlotter blotter = create("ABC",1,"2Y",true,100, 0,"DM","HLI",1000,1234,1, GUIOrderStatus.New.name(),"Blah");
        blotter.setLastQty(2);
        blotter.setLastPrice(100);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        blotter.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"blotter\",\"account\":\"DM\",\"trader\":\"HLI\",\"security\":\"2Y\",\"side\":\"buy\",\"clOrdID\":\"ABC\",\"updateType\":\"New\",\"rejectReason\":\"Blah\",\"price\":100,\"price32\":0,\"qty\":1,\"cumQty\":0,\"lastQty\":2,\"lastPrice\":100,\"notional\":1000,\"created\":0,\"updated\":0,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":0,\"time\":0}\n", s);
    }

    @Test
    public void testJSON_NORejectReasonAndTraderAndAccount_doesNotCrash() {
        GUIBlotter blotter = create("ABC",1,"2Y",true,100, 0,"","",1000,1234,1, GUIOrderStatus.New.name(),"");

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        blotter.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"blotter\",\"account\":\"\",\"trader\":\"\",\"security\":\"2Y\",\"side\":\"buy\",\"clOrdID\":\"ABC\",\"updateType\":\"New\",\"rejectReason\":\"\",\"price\":100,\"price32\":0,\"qty\":1,\"cumQty\":0,\"lastQty\":0,\"lastPrice\":0,\"notional\":1000,\"created\":0,\"updated\":0,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":0,\"time\":0}\n", s);
    }

    @Test
    public void testRoundQuarter() {
        long percent = MatchPriceUtils.getPriceMultiplier();
        long eighth = percent / 32 / 8;
        long quarter = percent / 32 / 4;
        GUIBlotter blotter = create("ABC", 1, "2Y", true, 100 * percent + eighth, 0, "DM", "HLI", 1000, 1234, 1, GUIOrderStatus.New.name(), "Blah");
        Assert.assertEquals(100 * percent, blotter.getPrice32());

        GUIBlotter blotter2 = create("ABC", 1, "2Y", false, 100 * percent + eighth, 0, "DM", "HLI", 1000, 1234, 1, GUIOrderStatus.New.name(), "Blah");
        Assert.assertEquals(100 * percent + quarter, blotter2.getPrice32());
    }

    @Test
    public void testRoundHalf() {
        long percent = MatchPriceUtils.getPriceMultiplier();
        long half = percent / 32 / 2;
        long quarter = percent / 32 / 4;
        GUIBlotter blotter = create("ABC", 1, "7Y", true, 100 * percent + quarter, 0, "DM", "HLI", 1000, 1234, 1, GUIOrderStatus.New.name(), "Blah");
        Assert.assertEquals(100 * percent, blotter.getPrice32());

        GUIBlotter blotter2 = create("ABC", 1, "7Y", false, 100 * percent + quarter, 0, "DM", "HLI", 1000, 1234, 1, GUIOrderStatus.New.name(), "Blah");
        Assert.assertEquals(100 * percent + half, blotter2.getPrice32());
    }

    private GUIBlotter create(String clOrdID,
                              int orderID,
                              String security,
                              boolean buy,
                              long price,
                              int cumQty,
                              String account,
                              String trader,
                              long notional,
                              long created,
                              int qty,
                              String updateType,
                              String rejectReason) {

        long price32 = price;
        switch (security) {
            case "2Y":
            case "3Y":
            case "5Y":
                price32 = MatchPriceUtils.roundQuarter(price, buy);
                break;
            case "7Y":
            case "10Y":
            case "30Y":
            default:
                price32 = MatchPriceUtils.roundHalf(price, buy);
        }

        GUIBlotter bm = new GUIBlotter(orderID, account, trader, security, buy, rejectReason, created);
        bm.setClOrdID(clOrdID);
        bm.setPrice(price);
        bm.setPrice32(price32);
        bm.setCumQty(cumQty);
        bm.setNotional(notional);
        bm.setQty(qty);
        bm.setUpdateType(updateType);

        return bm;
    }
}