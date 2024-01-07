package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUISecurity;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityType;
import com.core.util.BinaryUtils;
import com.core.util.PriceUtils;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.time.LocalDate;

/**
 * Created by jgreco on 2/23/16.
 */
public class GUISecurityTest {
    private void updateSecurity(GUISecurity security, BaseSecurity sec) {
        security.setTickSize(sec.getTickSize());
        security.setLotSize(sec.getLotSize());
        security.setSecType(sec.getType().getName());

        if (sec.isBond()) {
            Bond bond = (Bond) sec;
            security.setCusip(bond.getCUSIP());
            security.setMatDate(TimeUtils.toDateInt(bond.getMaturityDate()));
            security.setCoupon(bond.getCoupon());
        }
        else {
            security.setCusip("");
            security.setMatDate(0);
            security.setCoupon(0);
        }
    }

    @Test
    public void testSecurity() {
        Bond security1 = new Bond((short) 1, "10Y");
        security1.setLotSize(100);
        security1.setTickSize(PriceUtils.getPlus(9));
        security1.setType(SecurityType.NOTE);
        security1.setMaturityDate(LocalDate.of(2016,1,15));
        security1.setCoupon(9 * PriceUtils.getPriceMultiplier(9));
        security1.setCUSIP("FOO");

        GUISecurity security = new GUISecurity(1, security1.getName());
        updateSecurity(security, security1);

        Assert.assertEquals(1, security.getId());
        Assert.assertEquals("10Y", security.getSec());
        Assert.assertEquals("TreasuryNote", security.getSecType());
        Assert.assertEquals(100, security.getLotSize());
        Assert.assertEquals(PriceUtils.getPlus(9), security.getTickSize());
        Assert.assertEquals("FOO", security.getCusip());
        Assert.assertEquals(20160115, security.getMatDate());
        Assert.assertEquals(9 * PriceUtils.getPriceMultiplier(9), security.getCoupon());
    }

    @Test
    public void testUpdateSecurity() {
        Bond security1 = new Bond((short) 1, "10Y");
        security1.setLotSize(100);
        security1.setTickSize(PriceUtils.getPlus(9));
        security1.setType(SecurityType.NOTE);
        security1.setMaturityDate(LocalDate.of(2016,1,15));
        security1.setCoupon(9 * PriceUtils.getPriceMultiplier(9));
        security1.setCUSIP("FOO");

        GUISecurity security = new GUISecurity(1, security1.getName());
        updateSecurity(security, security1);

        security1.setLotSize(1000);
        security1.setTickSize(PriceUtils.getQuarter(9));
        security1.setType(SecurityType.BOND);
        security1.setMaturityDate(LocalDate.of(2016,2,15));
        security1.setCoupon(5 * PriceUtils.getPriceMultiplier(9));
        security1.setCUSIP("BAR");

        updateSecurity(security, security1);

        Assert.assertEquals(1, security.getId());
        Assert.assertEquals("10Y", security.getSec());
        Assert.assertEquals("TreasuryBond", security.getSecType());
        Assert.assertEquals(1000, security.getLotSize());
        Assert.assertEquals(PriceUtils.getQuarter(9), security.getTickSize());
        Assert.assertEquals("BAR", security.getCusip());
        Assert.assertEquals(20160215, security.getMatDate());
        Assert.assertEquals(5 * PriceUtils.getPriceMultiplier(9), security.getCoupon());
    }

    @Test
    public void testJSON() {
        Bond security1 = new Bond((short) 1, "10Y");
        security1.setLotSize(100);
        security1.setTickSize(PriceUtils.getPlus(9));
        security1.setType(SecurityType.NOTE);
        security1.setMaturityDate(LocalDate.of(2016,2,15));
        security1.setCoupon(5 * PriceUtils.getPriceMultiplier(9));
        security1.setCUSIP("BAR");

        GUISecurity security = new GUISecurity(1, security1.getName());
        updateSecurity(security, security1);
        security.setVer(3);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        security.write(buf, "20160223AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"security\",\"sec\":\"10Y\",\"secType\":\"TreasuryNote\",\"lotSize\":100,\"tickSize\":15625000,\"cusip\":\"BAR\",\"matDate\":20160215,\"coupon\":5000000000,\"ses\":\"20160223AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":3,\"time\":0}\n", s);
    }
}
