package com.core.match.msgs;

import com.core.app.AppConstructor;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class MatchPrinter implements 
	MatchContributorListener
	, MatchTraderListener
	, MatchSystemEventListener
	, MatchAccountListener
	, MatchSecurityListener
	, MatchOrderListener
	, MatchClientOrderRejectListener
	, MatchOrderRejectListener
	, MatchCancelListener
	, MatchClientCancelReplaceRejectListener
	, MatchCancelReplaceRejectListener
	, MatchReplaceListener
	, MatchFillListener
	, MatchInboundListener
	, MatchOutboundListener
	, MatchQuoteListener
	, MatchMiscRejectListener
{
    private final Connector connector;
    private final ContributorService<Contributor> contributors;
    private final SecurityService<BaseSecurity> securities;
    private final TraderService<Trader> traders;
    private final AccountService<Account> accounts;
	
    private final boolean json;
    private Dropper drop;

    @AppConstructor
    public MatchPrinter(
			Connector connector,
			ContributorService<Contributor> contributors,
			SecurityService<BaseSecurity> securities,
			TraderService<Trader> traders,
			AccountService<Account> accounts,
                        Dropper dropper,
			String style) throws IOException {
	this.connector = connector;
        this.contributors = contributors;
        this.securities = securities;
        this.traders = traders;
        this.accounts = accounts;
		
	this.drop = dropper;
	this.json = style.equalsIgnoreCase("JSON");
    }
	
    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "SourceContributorID", msg.getSourceContributorID());		
	    add(builder, "SourceContributor", msg.hasSourceContributorID() ? contributors.get(msg.getSourceContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "Name", msg.getNameAsString());		
        add(builder, "CancelOnDisconnect", msg.getCancelOnDisconnect());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchTrader(MatchTraderEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "TraderID", msg.getTraderID());		
	    add(builder, "Trader", msg.hasTraderID() ? traders.get(msg.getTraderID()).getName() : "<UNKNOWN>");	
        add(builder, "AccountID", msg.getAccountID());		
	    add(builder, "Account", msg.hasAccountID() ? accounts.get(msg.getAccountID()).getName() : "<UNKNOWN>");		    
        add(builder, "Name", msg.getNameAsString());		
        add(builder, "FatFinger2YLimit", msg.getFatFinger2YLimit());		
        add(builder, "FatFinger3YLimit", msg.getFatFinger3YLimit());		
        add(builder, "FatFinger5YLimit", msg.getFatFinger5YLimit());		
        add(builder, "FatFinger7YLimit", msg.getFatFinger7YLimit());		
        add(builder, "FatFinger10YLimit", msg.getFatFinger10YLimit());		
        add(builder, "FatFinger30YLimit", msg.getFatFinger30YLimit());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchSystemEvent(MatchSystemEventEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "EventType", msg.getEventType());		
		add(builder, "EventType", MatchConstants.SystemEvent.toString(msg.getEventType()));		    
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchAccount(MatchAccountEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "AccountID", msg.getAccountID());		
	    add(builder, "Account", msg.hasAccountID() ? accounts.get(msg.getAccountID()).getName() : "<UNKNOWN>");		    
        add(builder, "Name", msg.getNameAsString());		
        add(builder, "NetDV01Limit", msg.getNetDV01Limit());		
        add(builder, "Commission", msg.getCommissionAs32nd());		
        add(builder, "SSGMID", msg.getSSGMIDAsString());		
        add(builder, "NettingClearing", msg.getNettingClearing());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchSecurity(MatchSecurityEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "SecurityID", msg.getSecurityID());		
		add(builder, "Security", msg.hasSecurityID() ?  securities.get(msg.getSecurityID()).getName() : "<UNKNOWN>");
        add(builder, "Name", msg.getNameAsString());		
        add(builder, "CUSIP", msg.getCUSIPAsString());		
        add(builder, "MaturityDate", msg.getMaturityDate());		
        add(builder, "Coupon", msg.getCouponAs32nd());		
        add(builder, "Type", msg.getType());		
		add(builder, "Type", MatchConstants.SecurityType.toString(msg.getType()));		    
        add(builder, "IssueDate", msg.getIssueDate());		
        add(builder, "CouponFrequency", msg.getCouponFrequency());		
        add(builder, "TickSize", msg.getTickSizeAs32nd());		
        add(builder, "LotSize", msg.getLotSizeAsQty());		
        add(builder, "BloombergID", msg.getBloombergIDAsString());		
        add(builder, "NumLegs", msg.getNumLegs());		
        add(builder, "Leg1ID", msg.getLeg1ID());		
        add(builder, "Leg2ID", msg.getLeg2ID());		
        add(builder, "Leg3ID", msg.getLeg3ID());		
        add(builder, "Leg1Size", msg.getLeg1Size());		
        add(builder, "Leg2Size", msg.getLeg2Size());		
        add(builder, "Leg3Size", msg.getLeg3Size());		
        add(builder, "UnderlyingID", msg.getUnderlyingID());		
        add(builder, "ReferencePrice", msg.getReferencePriceAs32nd());		
		add(builder, "ReferencePrice", msg.getReferencePriceAsDouble());
	  	add(builder, "ReferencePrice32nd", msg.getReferencePriceAs32nd());
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchOrder(MatchOrderEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "Buy", msg.getBuy());		
        add(builder, "SecurityID", msg.getSecurityID());		
		add(builder, "Security", msg.hasSecurityID() ?  securities.get(msg.getSecurityID()).getName() : "<UNKNOWN>");
        add(builder, "Qty", msg.getQtyAsQty());		
        add(builder, "Price", msg.getPriceAs32nd());		
		add(builder, "Price", msg.getPriceAsDouble());
	  	add(builder, "Price32nd", msg.getPriceAs32nd());
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "TraderID", msg.getTraderID());		
	    add(builder, "Trader", msg.hasTraderID() ? traders.get(msg.getTraderID()).getName() : "<UNKNOWN>");	
        add(builder, "IOC", msg.getIOC());		
        add(builder, "ExternalOrderID", msg.getExternalOrderID());		
        add(builder, "InBook", msg.getInBook());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchClientOrderReject(MatchClientOrderRejectEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "Trader", msg.getTraderAsString());		
        add(builder, "Buy", msg.getBuy());		
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "Text", msg.getTextAsString());		
        add(builder, "Reason", msg.getReason());		
        add(builder, "Security", msg.getSecurityAsString());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchOrderReject(MatchOrderRejectEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "TraderID", msg.getTraderID());		
	    add(builder, "Trader", msg.hasTraderID() ? traders.get(msg.getTraderID()).getName() : "<UNKNOWN>");	
        add(builder, "Buy", msg.getBuy());		
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "Text", msg.getTextAsString());		
        add(builder, "Reason", msg.getReason());		
		add(builder, "Reason", MatchConstants.OrderRejectReason.toString(msg.getReason()));		    
        add(builder, "SecurityID", msg.getSecurityID());		
		add(builder, "Security", msg.hasSecurityID() ?  securities.get(msg.getSecurityID()).getName() : "<UNKNOWN>");
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchCancel(MatchCancelEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "OrigClOrdID", msg.getOrigClOrdIDAsString());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchClientCancelReplaceReject(MatchClientCancelReplaceRejectEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "OrigClOrdID", msg.getOrigClOrdIDAsString());		
        add(builder, "Text", msg.getTextAsString());		
        add(builder, "IsReplace", msg.getIsReplace());		
        add(builder, "Reason", msg.getReason());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchCancelReplaceReject(MatchCancelReplaceRejectEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "OrigClOrdID", msg.getOrigClOrdIDAsString());		
        add(builder, "Text", msg.getTextAsString());		
        add(builder, "IsReplace", msg.getIsReplace());		
        add(builder, "Reason", msg.getReason());		
		add(builder, "Reason", MatchConstants.OrderRejectReason.toString(msg.getReason()));		    
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchReplace(MatchReplaceEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "Qty", msg.getQtyAsQty());		
        add(builder, "Price", msg.getPriceAs32nd());		
		add(builder, "Price", msg.getPriceAsDouble());
	  	add(builder, "Price32nd", msg.getPriceAs32nd());
        add(builder, "ClOrdID", msg.getClOrdIDAsString());		
        add(builder, "OrigClOrdID", msg.getOrigClOrdIDAsString());		
        add(builder, "ExternalOrderID", msg.getExternalOrderID());		
        add(builder, "InBook", msg.getInBook());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchFill(MatchFillEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "OrderID", msg.getOrderID());		
        add(builder, "Qty", msg.getQtyAsQty());		
        add(builder, "Price", msg.getPriceAs32nd());		
		add(builder, "Price", msg.getPriceAsDouble());
	  	add(builder, "Price32nd", msg.getPriceAs32nd());
        add(builder, "MatchID", msg.getMatchID());		
        add(builder, "LastFill", msg.getLastFill());		
        add(builder, "Passive", msg.getPassive());		
        add(builder, "InBook", msg.getInBook());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchInbound(MatchInboundEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "FixMsgType", msg.getFixMsgType());		
        add(builder, "BeginSeqNo", msg.getBeginSeqNo());		
        add(builder, "EndSeqNo", msg.getEndSeqNo());		
        add(builder, "ReqID", msg.getReqIDAsString());		
        add(builder, "SecurityID", msg.getSecurityID());		
		add(builder, "Security", msg.hasSecurityID() ?  securities.get(msg.getSecurityID()).getName() : "<UNKNOWN>");
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchOutbound(MatchOutboundEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "FixMsgType", msg.getFixMsgType());		
        add(builder, "ReqID", msg.getReqIDAsString());		
        add(builder, "Text", msg.getTextAsString());		
        add(builder, "RefMsgType", msg.getRefMsgType());		
        add(builder, "RefSeqNum", msg.getRefSeqNum());		
        add(builder, "RefTagID", msg.getRefTagID());		
        add(builder, "SessionRejectReason", msg.getSessionRejectReason());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchQuote(MatchQuoteEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "SecurityID", msg.getSecurityID());		
		add(builder, "Security", msg.hasSecurityID() ?  securities.get(msg.getSecurityID()).getName() : "<UNKNOWN>");
        add(builder, "BidPrice", msg.getBidPriceAs32nd());		
		add(builder, "BidPrice", msg.getBidPriceAsDouble());
	  	add(builder, "BidPrice32nd", msg.getBidPriceAs32nd());
        add(builder, "OfferPrice", msg.getOfferPriceAs32nd());		
		add(builder, "OfferPrice", msg.getOfferPriceAsDouble());
	  	add(builder, "OfferPrice32nd", msg.getOfferPriceAs32nd());
        add(builder, "VenueCode", msg.getVenueCode());		
		add(builder, "VenueCode", MatchConstants.Venue.toString(msg.getVenueCode()));		    
        add(builder, "SourceTimestamp", msg.getSourceTimestampAsTime());		
		end(builder);
		write(builder);
    }
	
    @Override
    public void onMatchMiscReject(MatchMiscRejectEvent msg) {
        StringBuilder builder = start(msg.getMsgName());
        add(builder, "MsgType", msg.getMsgType());		
        add(builder, "ContributorID", msg.getContributorID());		
	    add(builder, "Contributor", msg.hasContributorID() ? contributors.get(msg.getContributorID()).getName() : "<UNKNOWN>");	
        add(builder, "ContributorSeq", msg.getContributorSeq());		
        add(builder, "Timestamp", msg.getTimestampAsTime());		
        add(builder, "RejectedMsgType", msg.getRejectedMsgType());		
        add(builder, "RejectReason", msg.getRejectReason());		
		add(builder, "RejectReason", MatchConstants.MiscRejectReason.toString(msg.getRejectReason()));		    
		end(builder);
		write(builder);
    }
	
	private StringBuilder start(String name) {
		StringBuilder builder = new StringBuilder();
		if (json) {
            builder.append("{\"Seq\":").append(connector.getCurrentSeq());
			builder.append(",\"MsgType\":\"").append(name);
		}
		else {
            builder.append("Seq=").append(connector.getCurrentSeq());
			builder.append(",MsgType=").append(name);
		}
		return builder;
	}
	
	private void end(StringBuilder builder) {
		if (json) {
			builder.append("}");
		}
		builder.append("\n");
	}
	
	private void add(StringBuilder builder, String name, Object val) {
            Class cls = val.getClass();
            boolean isString = cls.equals(String.class) || cls.equals(LocalDate.class) || cls.equals(LocalTime.class) || cls.equals(LocalDateTime.class);	
            if (json) {			
                builder.append(",\"").append(name).append("\":");	    				
		if (isString) {
                    builder.append("\"");
		}
            }
            else {
                builder.append(",").append(name).append("=");
            }
		
            builder.append(val);
		
            if (json) {
                if (isString) {
                    builder.append("\"");
                }
            }
        }

	private void write(StringBuilder builder) {
		drop.add(builder.toString());
	}
    
     public interface Dropper {
        void add(String str);
     }
} 
