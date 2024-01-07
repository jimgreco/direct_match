package com.core.match.fix.md;

import com.core.fix.FixParser;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOutboundCommand;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 11/24/14.
 */
public class MarketDataTags {
    private final ByteBuffer temp = ByteBuffer.allocate(128);

    public final FixTag msgType;
    public final FixTag msgSeqNum;
    public final FixTag mdReqId;
    public final FixTag securityReqId;
    public final FixTag securityReqType;
    public final FixTag securityListReqType;
    public final FixTag subscriptionReqType;
    public final FixTag marketDepth;
    public final FixTag mdUpdateType;
    public final FixTag noMdEntryTypes;
    public final FixTag mdEntryType;
    public final FixTag noRelatedSym;
    public final FixTag symbol;
    public final FixTag refMsgType;
    public final FixTag refSeqNum;
    public final FixTag text;
    public final FixTag securityResponseId;
    public final FixTag securityRequestResult;
    public final FixTag securityType;
    public final FixTag totNoRelatedSym;
    public final FixTag lastFragment;
    public final FixTag securityIdSource;
    public final FixTag couponRate;
    public final FixTag contractMultiplier;
    public final FixTag maturityDate;
    public final FixTag securityId;
    public final FixTag noMdEntries;
    public final FixTag mdEntryPx;
    public final FixTag mdEntrySize;
    public final FixTag mdUpdateAction;
    public final FixTag MDEntryPositionNo;
    public final FixTag businessRejectReason;
    public final FixTag businessRejectRefID;
    public final FixTag securityStatusReqId;
    public final FixTag securityTradingStatus;
    public final FixTag minPriceIncrement;
    public final FixTag minPriceIncrementAmount;

    public final FixTag[] requiredSecurityListRequest;
    public final FixTag[] requiredSecurityDefinitionListRequest;
    public final FixTag[] requiredSecurityStatusRequest;

    public final FixTag[] requiredMarketDataRequest;
    public final FixTag resetSeqNumFlag;
    public final FixTag encryptMethod;
    public final FixTag heartBtInt;
    public FixTag currency;
    public final FixTag priceDisplayType;

    public MarketDataTags(FixParser parser) {
        this.msgType = parser.createReadWriteFIXTag(FixTags.MsgType);
        this.msgSeqNum = parser.createReadWriteFIXTag(FixTags.MsgSeqNum);
        this.text = parser.createWriteOnlyFIXTag(FixTags.Text);
        this.refMsgType = parser.createWriteOnlyFIXTag(FixTags.RefMsgType);
        this.refSeqNum = parser.createWriteOnlyFIXTag(FixTags.RefSeqNum);
        this.currency= parser.createWriteOnlyFIXTag(FixTags.Currency);
        this.mdReqId = parser.createReadWriteFIXTag(FixTags.MDReqID);
        this.subscriptionReqType = parser.createReadWriteFIXTag(FixTags.SubscriptionRequestType);
        this.marketDepth = parser.createReadWriteFIXTag(FixTags.MarketDepth);
        this.mdUpdateType = parser.createReadWriteFIXTag(FixTags.MDUpdateType);
        this.noMdEntryTypes = parser.createReadWriteFIXTag(FixTags.NoMDEntryTypes);
        this.mdEntryType = parser.createReadWriteFIXTag(FixTags.MDEntryType);
        this.noRelatedSym = parser.createReadWriteFIXTag(FixTags.NoRelatedSym);
        this.noMdEntries = parser.createReadWriteFIXTag(FixTags.NoMDEntries);
        this.mdUpdateAction = parser.createReadWriteFIXTag(FixTags.MDUpdateAction);
        this.MDEntryPositionNo = parser.createReadWriteFIXTag(FixTags.MDEntryPositionNo);
        this.mdEntryPx = parser.createReadWriteFIXTag(FixTags.MDEntryPx);
        this.mdEntrySize = parser.createReadWriteFIXTag(FixTags.MDEntrySize);
        this.securityIdSource = parser.createReadWriteFIXTag(FixTags.SecurityIDSource);
        this.securityId = parser.createReadWriteFIXTag(FixTags.SecurityID);
        this.securityStatusReqId = parser.createReadWriteFIXTag(FixTags.SecurityStatusReqID);

        this.securityReqId = parser.createReadWriteFIXTag(FixTags.SecurityReqID);
        this.securityReqType = parser.createReadWriteFIXTag(FixTags.SecurityReqType);
        this.securityListReqType = parser.createReadWriteFIXTag(FixTags.SecurityListRequestType);
        this.symbol = parser.createReadWriteFIXTag(FixTags.Symbol);
        this.securityResponseId = parser.createWriteOnlyFIXTag(FixTags.SecurityResponseID);
        this.securityRequestResult = parser.createWriteOnlyFIXTag(FixTags.SecurityRequestResult);
        this.totNoRelatedSym = parser.createWriteOnlyFIXTag(FixTags.TotNoRelatedSym);
        this.lastFragment = parser.createWriteOnlyFIXTag(FixTags.LastFragment);
        this.securityType = parser.createWriteOnlyFIXTag(FixTags.SecurityType);
        this.couponRate = parser.createWriteOnlyFIXTag(FixTags.CouponRate);
        this.contractMultiplier = parser.createWriteOnlyFIXTag(FixTags.ContractMultiplier);
        this.maturityDate = parser.createWriteOnlyFIXTag(FixTags.MaturityDate);
        this.businessRejectReason = parser.createWriteOnlyFIXTag(FixTags.BusinessRejectReason);
        this.businessRejectRefID = parser.createWriteOnlyFIXTag(FixTags.BusinessRejRefID);
        this.securityTradingStatus = parser.createWriteOnlyFIXTag(FixTags.SecurityTradingStatus);
        this.minPriceIncrement = parser.createWriteOnlyFIXTag(FixTags.MinPriceIncrement);
        this.minPriceIncrementAmount = parser.createWriteOnlyFIXTag(FixTags.MinPriceIncrementAmount);

        this.encryptMethod = parser.createWriteOnlyFIXTag(FixTags.EncryptMethod);
        this.heartBtInt = parser.createWriteOnlyFIXTag(FixTags.HeartBtInt);
        this.resetSeqNumFlag = parser.createWriteOnlyFIXTag(FixTags.ResetSeqNumFlag);
        this.priceDisplayType = parser.createWriteOnlyFIXTag(FixTags.PriceDisplayType);

        this.requiredSecurityListRequest = new FixTag[] {
                securityReqId,
                securityListReqType
        };

        this.requiredSecurityDefinitionListRequest = new FixTag[] {
                securityReqId,
                securityReqType
        };

        this.requiredSecurityStatusRequest = new FixTag[] {
                securityStatusReqId,
                subscriptionReqType,
                symbol,
                securityId
        };


        this.requiredMarketDataRequest = new FixTag[] {
                mdReqId,
                subscriptionReqType,
                marketDepth,
                mdUpdateType,
                noMdEntryTypes,
                mdEntryType,
                noRelatedSym,
                symbol
        };
    }

    public boolean checkFields(MatchCommandSender sender, MatchMessages messages, FixTag[] requiredTags, char fixMsgType) {
        for (FixTag tag : requiredTags) {
            if (!tag.isPresent()) {
                temp.clear();
                BinaryUtils.copy(temp, "Req Tag Missing: ");
                TextUtils.writeNumber(temp, tag.getID());
                temp.flip();

                MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
                outbound.setFixMsgType(fixMsgType);
                outbound.setRefMsgType(msgType.getValueAsChar());
                outbound.setRefSeqNum(msgSeqNum.getValueAsInt());
                outbound.setText(temp);
                outbound.setRefTagID(tag.getID());
                outbound.setSessionRejectReason(FixConstants.BusinessRejectReason.ConditionallyReqMissingField);
                if (mdReqId.isPresent()) {
                    outbound.setReqID(mdReqId.getValue());
                }
                sender.send(outbound);
                return false;
            }
        }
        return true;
    }
}
