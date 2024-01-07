package com.core.match.drops.gui;

import com.core.match.GenericAppTest;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUISecurity;
import com.core.match.drops.gui.msgs.GUIStatus;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.services.order.BaseOrder;
import com.core.match.util.MatchPriceUtils;
import com.core.util.PriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 2/24/16.
 */
public class MiscCollectionTest extends GenericAppTest<BaseOrder> {
    private MiscCollection miscCollection;

    public MiscCollectionTest() {
        super(BaseOrder.class);
    }

    @Before
    public void setup() {
        miscCollection = new MiscCollection(securities, systemEventService, new LinearCounter(), new LinearCounter());
    }

    @Test
    public void testEventUnknown() {
        GUIStatus guiStatus = (GUIStatus) miscCollection.getItem(0);
        Assert.assertEquals(0, guiStatus.getEvent());
        Assert.assertEquals(1, guiStatus.getId());
        Assert.assertEquals(1, guiStatus.getVer());
        Assert.assertEquals(0, guiStatus.getTime());
    }

    @Test
    public void testOpen() {
        openMarket();

        GUIStatus guiStatus = (GUIStatus) miscCollection.getItem(0);
        Assert.assertEquals('O', guiStatus.getEvent());
        Assert.assertEquals(1, guiStatus.getId());
        Assert.assertEquals(2, guiStatus.getVer());
    }

    @Test
    public void testClose() {
        openMarket();
        closeMarket();

        GUIStatus guiStatus = (GUIStatus) miscCollection.getItem(0);
        Assert.assertEquals('C', guiStatus.getEvent());
        Assert.assertEquals(1, guiStatus.getId());
        Assert.assertEquals(3, guiStatus.getVer());
    }

    @Test
    public void testSecurity() {
        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID((short)1);
        cmd.setName("10Y");
        cmd.setCUSIP("10_CUSIP");
        cmd.setMaturityDate(20260215);
        cmd.setCoupon(2 * MatchPriceUtils.getPriceMultiplier());
        cmd.setCouponFrequency((byte) 2);
        cmd.setIssueDate(20160215);
        cmd.setTickSize(PriceUtils.getEighth(8));
        cmd.setType(MatchConstants.SecurityType.TreasuryNote);
        cmd.setLotSize(1000);
        dispatch(cmd);

        GUISecurity sec = (GUISecurity) miscCollection.getItem(1);
        Assert.assertEquals("10Y", sec.getSec());
        Assert.assertEquals(1000, sec.getLotSize());
        Assert.assertEquals(PriceUtils.getEighth(8), sec.getTickSize());
        Assert.assertEquals("TreasuryNote", sec.getSecType());
        Assert.assertEquals(2, sec.getId());
        Assert.assertEquals(2, sec.getVer());
    }

    @Test
    public void testUpdateSecurity() {
        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID((short)1);
        cmd.setName("10Y");
        cmd.setCUSIP("10_CUSIP");
        cmd.setMaturityDate(20260215);
        cmd.setCoupon(2 * MatchPriceUtils.getPriceMultiplier());
        cmd.setCouponFrequency((byte) 2);
        cmd.setIssueDate(20160215);
        cmd.setTickSize(PriceUtils.getEighth(8));
        cmd.setType(MatchConstants.SecurityType.TreasuryNote);
        cmd.setLotSize(1000);
        dispatch(cmd);

        cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID((short)1);
        cmd.setName("10Y");
        cmd.setCUSIP("10_CUSIP");
        cmd.setMaturityDate(20260215);
        cmd.setCoupon(2 * MatchPriceUtils.getPriceMultiplier());
        cmd.setCouponFrequency((byte) 2);
        cmd.setIssueDate(20160215);
        cmd.setTickSize(PriceUtils.getPlus(8));
        cmd.setType(MatchConstants.SecurityType.TreasuryBond);
        cmd.setLotSize(100);
        dispatch(cmd);

        GUISecurity sec = (GUISecurity) miscCollection.getItem(1);
        Assert.assertEquals("10Y", sec.getSec());
        Assert.assertEquals(100, sec.getLotSize());
        Assert.assertEquals(PriceUtils.getPlus(8), sec.getTickSize());
        Assert.assertEquals("TreasuryBond", sec.getSecType());
        Assert.assertEquals(2, sec.getId());
        Assert.assertEquals(3, sec.getVer());
    }
}
