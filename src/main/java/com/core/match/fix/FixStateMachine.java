package com.core.match.fix;

import com.core.app.CommandException;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.ContributorDefinedListener;
import com.core.fix.FixWriter;
import com.core.fix.connector.FixConnector;
import com.core.fix.msgs.FixBusinessRejectListener;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixHeartbeatListener;
import com.core.fix.msgs.FixLogoutListener;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixRejectListener;
import com.core.fix.msgs.FixResendRequestListener;
import com.core.fix.msgs.FixTags;
import com.core.fix.msgs.FixTestRequestListener;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.fix.tags.FixTagCreator;
import com.core.fix.util.HeartbeatSessionListener;
import com.core.fix.util.HeartbeatSessionManager;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchInboundCommand;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchInboundListener;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOutboundCommand;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchOutboundListener;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.log.Logger;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class FixStateMachine implements
		FixMachine {
	private final ByteBuffer reqID = ByteBuffer.wrap("DM".getBytes());
	private final ByteBuffer temp = ByteBuffer.allocateDirect(32);
	private final MatchMessages messages;

	private final Log log;
	private final FixDispatcher dispatcher;
	private final MatchCommandSender sender;
	final FixStore store;
	final FixConnector connector;
	private final HeartbeatSessionManager heartbeatSessionManager;

	private final TimerService timerService;

	private final FixTag text;
	private final FixTag msgTypeTag;
	private final FixTag msgSeqNum;
	private final FixTag gapFillFlag;
	private final FixTag testReqID;
	private final FixTag heartBIntFlag;
	private final FixTag resetSeqNumFlag;
	private final FixTag encryptMethod;
	private final FixTag beginSeqNo;
	private final FixTag endSeqNo;
	private final FixTag newSeqNo;
	private final FixTag refMsgType;
	private final FixTag sessionRejectReason;
	private final FixTag bizRejectReason;
	private final FixTag refSeqNum;
	private final FixTag refTagID;
	private final FixTag beginString;
	private final FixTag senderCompID;
	private final FixTag targetCompID;

	private final String beginStringValue;
	private final String senderCompIDValue;
	private final String targetCompIDValue;

	private boolean loggedIn;

	private short myContribId;
	private int nextInboundSeqNum = 1;

	int requestTimer;
	int resendTimer;

	int nextResendSeqNo;

	private HeartbeatNumberField nextInboundMonitor;
	private HeartbeatNumberField nextOutboundMonitor;
	private HeartbeatBooleanField loggedInMonitor;
	private HeartbeatBooleanField resendingMonitor;
	private final boolean resetOnLogin;
	private final boolean client;

	public FixStateMachine(Log log, FixDispatcher dispatcher, TimerService timerService, MatchCommandSender sender, FixTagCreator parser, FixStore store, FixConnector connector, MatchMessages msgs, boolean resetOnLogin, boolean client, int minorVersion, String senderCompID, String targetCompID) {
		this.log = log;
		this.dispatcher = dispatcher;
		this.sender = sender;
		this.store = store;
		this.timerService = timerService;
		this.messages = msgs;
		this.resetOnLogin = resetOnLogin;
		this.client = client;

		this.beginStringValue = "FIX.4." + minorVersion;
		this.senderCompIDValue = senderCompID;
		this.targetCompIDValue = targetCompID;

		this.heartbeatSessionManager = new HeartbeatSessionManager(timerService, this);
		this.connector = connector;
		this.connector.addListener(this.heartbeatSessionManager);

		this.testReqID = parser.createReadWriteFIXTag(FixTags.TestReqID);
		this.text = parser.createReadWriteFIXTag(FixTags.Text);
		this.msgTypeTag = parser.createReadWriteFIXTag(FixTags.MsgType);
		this.msgSeqNum = parser.createReadWriteFIXTag(FixTags.MsgSeqNum);
		this.gapFillFlag = parser.createReadWriteFIXTag(FixTags.GapFillFlag);
		this.beginSeqNo = parser.createReadWriteFIXTag(FixTags.BeginSeqNo);
		this.endSeqNo = parser.createReadWriteFIXTag(FixTags.EndSeqNo);
		this.heartBIntFlag = parser.createReadWriteFIXTag(FixTags.HeartBtInt);
		this.refSeqNum = parser.createReadWriteFIXTag(FixTags.RefSeqNum);
		this.refMsgType = parser.createReadWriteFIXTag(FixTags.RefMsgType);
		this.refTagID = parser.createReadWriteFIXTag(FixTags.RefTagID);
		this.senderCompID = parser.createReadWriteFIXTag(FixTags.SenderCompID);
		this.targetCompID = parser.createReadWriteFIXTag(FixTags.TargetCompID);
		this.beginString = parser.createReadWriteFIXTag(FixTags.BeginString);
		this.sessionRejectReason = parser.createReadWriteFIXTag(FixTags.SessionRejectReason);
		this.bizRejectReason = parser.createReadWriteFIXTag(FixTags.BusinessRejectReason);

		this.newSeqNo = parser.createReadWriteFIXTag(FixTags.NewSeqNo);
		this.encryptMethod = parser.createWriteOnlyFIXTag(FixTags.EncryptMethod);
		this.resetSeqNumFlag = parser.createWriteOnlyFIXTag(FixTags.ResetSeqNumFlag);

		this.dispatcher.subscribe(this);
	}

	@Override
	public FixMessageResult onFixMessage() {

		if (!doBasicChecks()) {
			return FixMessageResult.Disconnect;
		}

		char msgType = msgTypeTag.getValueAsChar();
		int seqNum = msgSeqNum.getValueAsInt();

		if (seqNum <= 0) {
			log.error(log.log().add("Fix invalid MsgSeqNum<34>. Disconnecting."));
			return FixMessageResult.Disconnect;
		}
		if (!isLoggedIn()) {
			log.debug(log.log().add("Fix not logged in. Handling login message."));
			return handleLogin(seqNum, msgType);
		}

		if (msgType == FixMsgTypes.SequenceReset) {
			log.debug(log.log().add("Fix SequenceReset<4>-Gap Fill Request."));
			return handleGapFill();
		}

		if (seqNum < nextInboundSeqNum) {
			log.error(log.log().add("Fix MsgSeqNum<34> too low. Disconnecting. Expected=").add(nextInboundSeqNum).add(", Recv=").add(seqNum));
			return FixMessageResult.Disconnect;
		}

		if (seqNum > nextInboundSeqNum) {
			return handleSeqNumTooHigh(msgType, seqNum);
		}


		clearRequestTimer();

		if (!dispatcher.onMessage(msgType)) {
			temp.clear();
			BinaryUtils.copy(temp, "Unhandled MsgType ");
			temp.put((byte) msgType).flip();
			reject(temp, FixTags.MsgType, FixConstants.SessionRejectReason.InvalidMsgType);
			return FixMessageResult.Complete;
		}

		return FixMessageResult.Complete;
	}

	private boolean doBasicChecks() {
		if (!msgTypeTag.isPresent()) {
			log.error(log.log().add("FIX missing MsgType<35>. Disconnecting."));
			return false;
		}

		if (!msgSeqNum.isPresent()) {
			log.error(log.log().add("FIX missing MsgSeqNum<34>. Disconnecting."));
			return false;
		}

		if (!beginString.isPresent()) {
			log.error(log.log().add("FIX missing BeginString<8>. Disconnecting."));
			return false;
		}
		if (!BinaryUtils.compare(beginString.getValue(), beginStringValue)) {
			log.error(log.log().add("FIX BeginString<49> does not match. Expected=").add(beginStringValue).add(", Recv=").add(beginString.getValue()));
			return false;
		}

		if (!senderCompID.isPresent()) {
			log.error(log.log().add("FIX missing SenderCompID<49>. Disconnecting."));
			return false;
		}
		if (!BinaryUtils.compare(senderCompID.getValue(), senderCompIDValue)) {
			log.error(log.log().add("FIX SenderCompID<49> does not match. Expected=").add(senderCompIDValue).add(", Recv=").add(senderCompID.getValue()));
			return false;
		}

		if (!targetCompID.isPresent()) {
			log.error(log.log().add("FIX missing TargetCompID<56>. Disconnecting."));
			return false;
		}
		if (!BinaryUtils.compare(targetCompID.getValue(), targetCompIDValue)) {
			log.error(log.log().add("FIX TargetCompID<56> does not match. Expected=").add(targetCompIDValue).add(", Recv=").add(targetCompID.getValue()));
			return false;
		}

		return true;
	}

	public void reject(String msg, int tag, char reason) {
		temp.clear();
		BinaryUtils.copy(temp, msg).flip();
		reject(temp, tag, reason);
	}

	public void reject(ByteBuffer rejectMessage, int tag, char reason) {
		log.error(log.log().add("FIX Session Reject<3>: ").add(rejectMessage));
		sender.init();

		// need to account for the sequence number
		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.Heartbeat);
		sender.add(inbound);

		MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
		outbound.setFixMsgType(FixMsgTypes.Reject);
		outbound.setRefSeqNum(msgSeqNum.getValueAsInt());
		outbound.setText(rejectMessage);
		outbound.setRefTagID((short) tag);
		outbound.setRefMsgType(msgTypeTag.getValueAsChar());
		outbound.setSessionRejectReason(reason);
		sender.add(outbound);

		sender.send();
	}

	public void incInboundSeqNo() {
		nextInboundSeqNum++;
	}

	private FixMessageResult handleSeqNumTooHigh(char msgType, int seqNum) {
		if (msgType == FixMsgTypes.Logout) {
			log.info(log.log().add("FIX Logout<5> with an invalid SeqNum. Disconnecting."));
			return FixMessageResult.Disconnect;
		}

		if (msgType == FixMsgTypes.ResendRequest) {
			int beginSeqNoInt = this.beginSeqNo.getValueAsInt();
			int endSeqNoInt = this.endSeqNo.getValueAsInt();

			if (beginSeqNoInt <= 0) {
				log.info(log.log().add("FIX ResendRequest<2> with invalid BeginSeqNo<7>=").add(beginSeqNoInt));
				return FixMessageResult.Disconnect;
			}
			if (endSeqNoInt < 0) {
				log.info(log.log().add("FIX ResendRequest<2> with invalid EndSeqNo<16>=").add(endSeqNoInt));
				return FixMessageResult.Disconnect;
			}

			int resendResult = store.resend(beginSeqNoInt, endSeqNoInt);
			scheduleResendTimer(resendResult);
			return resendResult == 0 ? FixMessageResult.NextMessage : FixMessageResult.Complete;
		}

		if (requestTimer == TimerService.NULL_VALUE) {
			log.info(log.log().add("FIX MsgSeqNum<34> too high. Requesting gap fill. Expected=").add(nextInboundSeqNum).add(", Recv=").add(seqNum));
			MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.ResendRequest);
			sender.send(outbound);

			// scheduleResendTimer
			// send complete
		}

		// TODO: Why are we sending next message???
		return FixMessageResult.NextMessage;
	}

	private FixMessageResult handleGapFill() {
		if (!gapFillFlag.isPresent() || gapFillFlag.getValueAsChar() != 'Y' || !newSeqNo.isPresent()) {
			log.error(log.log().add("FIX SequenceReset<4>-Reset<N> is not supported. Must have GapFillFlag=Y and NewSeqNo"));
			return FixMessageResult.Disconnect;
		}

		int newSeqNoVal = newSeqNo.getValueAsInt();
		if (newSeqNoVal < nextInboundSeqNum) {
			log.error(log.log().add("FIX SequenceReset<4>-GapFill<Y> to lower sequence number is not allowed. NextSeqNum=").add(nextInboundSeqNum).add(", NewSeqNo<36>=").add(newSeqNoVal));
			return FixMessageResult.Disconnect;
		}

		log.info(log.log().add("FIX SequenceReset<4>-GapFill<Y>. OldSeqNo=").add(nextInboundSeqNum).add(" => NewSeqNo<36>=").add(newSeqNoVal));
		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.SequenceReset);
		inbound.setEndSeqNo(newSeqNoVal);
		sender.send(inbound);
		return FixMessageResult.Complete;
	}

	private FixMessageResult handleLogin(int seqNum, char msgType) {
		if (msgType != FixMsgTypes.Logon) {
			log.error(log.log().add("Fix Logon<A> message must be first message received. RecvMsgType=").add(msgType));
			return FixMessageResult.Disconnect;
		}

		short heartb = 30;
		if (heartBIntFlag.isPresent()) {
			heartb = (short) heartBIntFlag.getValueAsInt();
			if (heartb <= 0) {
				heartb = 30;
			}
		}

		heartbeatSessionManager.clear(heartb);

		if (seqNum == nextInboundSeqNum) {
			// proceed as normal
			// record login request and send login response
			log.info(log.log().add("FIX Logon<A> success. MsgSeqNo<34>=").add(nextInboundSeqNum));

			sender.init();

			MatchInboundCommand inbound = messages.getMatchInboundCommand();
			inbound.setFixMsgType(FixMsgTypes.Logon);
			sender.add(inbound);

			if( !client )
			{
				MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
				outbound.setFixMsgType(FixMsgTypes.Logon);
				sender.add(outbound);
			}

			loggedIn = true;
			sender.send();
			return FixMessageResult.Complete;
		}
		else if (seqNum > nextInboundSeqNum) {
			// we're missing some messages
			// send login response and request gap fill
			log.info(log.log().add("FIX Logon<A> MsgSeqNum<34> is too high. Requesting gap fill. Expected=").add(nextInboundSeqNum).add(", Recv=").add(seqNum));
			sender.init();

			MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.Logon);
			sender.add(outbound);

			outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.ResendRequest);
			sender.add(outbound);

			scheduleRequestTimer();

			loggedIn = true;
			sender.send();
			return FixMessageResult.Complete;
		}
		else {
			log.error(log.log().add("FIX Logon<A> MsgSeqNum<34> too low. Disconnecting. Expected=").add(nextInboundSeqNum).add(", Recv=").add(seqNum).add(". Disconnecting"));
			return FixMessageResult.Disconnect;
		}
	}

	@Override
	public void onFixHeartbeat() {
		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.Heartbeat);
		sender.send(inbound);
	}

	@Override
	public void onFixLogout() {
		sender.init();

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.Logout);
		sender.add(inbound);

		MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
		outbound.setFixMsgType(FixMsgTypes.Logout);
		sender.add(outbound);

		sender.send();

		Logger msg = this.log.log().add("Logout requested. Disconnecting.");
		if (text.isPresent()) {
			msg.add(" Text=").add(text.getValue());
		}
		log.info(msg);
	}

	@Override
	public void onFixReject() {
		log.error(log.log().add("FIX Reject: ").add(text.getValue())
				.add(". RefSeqNum=").add(refSeqNum.getValue())
				.add(",RefMsgType=").add(refMsgType.getValue()).add("[").add(FixMsgTypes.getMsgTypeName(refMsgType.getValueAsChar()))
				.add("],RefTagID=").add(refTagID.getValue()).add("[").add(FixTags.getTagName(refTagID.getValueAsInt()))
				.add("],SessionRejectReason=").add(sessionRejectReason.getValue()));

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.Reject);
		sender.send(inbound);
	}

	@Override
	public void onFixBusinessReject() {
		log.error(log.log().add("FIX Business Reject: ").add(text.getValue())
				.add(". RefSeqNum=").add(refSeqNum.getValue())
				.add(",RefMsgType=").add(refMsgType.getValue()).add("[").add(FixMsgTypes.getMsgTypeName(refMsgType.getValueAsChar()))
				.add("],BusinessRejectReason=").add(bizRejectReason.getValue()));

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.BusinessReject);
		sender.send(inbound);
	}

	@Override
	public void onFixResendRequest() {
		log.info(log.log().add("Recv FIX resend Request."));
		if (!isFixResendRequestValid()) {
			return;
		}

		int startSequenceNumber = this.beginSeqNo.getValueAsInt();
		int endSequenceNumber = this.endSeqNo.getValueAsInt();

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.ResendRequest);
		inbound.setBeginSeqNo(startSequenceNumber);
		inbound.setEndSeqNo(endSequenceNumber);
		log.info(log.log().add("Send resend Request to sequencer. Begin:").add(startSequenceNumber).add("End:").add(endSequenceNumber));

		sender.send(inbound);
	}

	private boolean isFixResendRequestValid() {
		int problemTag = 0;
		char rejectReasonChar = '0';

		temp.clear();
		if (!beginSeqNo.isPresent()) {
			BinaryUtils.copy(temp, "Missing BeginSeqNo<7>");
			problemTag = FixTags.BeginSeqNo;
			rejectReasonChar = FixConstants.SessionRejectReason.RequiredTagMissing;
		}
		else if (beginSeqNo.getValueAsInt() <= 0) {
			BinaryUtils.copy(temp, "Invalid BeginSeqNo<7>: ");
			TextUtils.writeNumber(temp, beginSeqNo.getValueAsChar());
			problemTag = FixTags.BeginSeqNo;
			rejectReasonChar = FixConstants.SessionRejectReason.IncorrectDataFormatForValue;
		}
		else if (endSeqNo.getValueAsInt() < 0) {
			BinaryUtils.copy(temp, "Invalid EndSeqNo<16>: ");
			TextUtils.writeNumber(temp, endSeqNo.getValueAsInt());
			problemTag = FixTags.EndSeqNo;
			rejectReasonChar = FixConstants.SessionRejectReason.IncorrectDataFormatForValue;
		}

		if (problemTag > 0) {
			temp.flip();
			reject(temp, problemTag, rejectReasonChar);
			return false;
		}
		return true;
	}

	@Override
	public void onFixTestRequest() {
		sender.init();

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(FixMsgTypes.TestRequest);
		sender.add(inbound);

		MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
		outbound.setFixMsgType(FixMsgTypes.Heartbeat);

		if (testReqID.isPresent()) {
			outbound.setReqID(testReqID.getValue());
		}
		sender.add(outbound);
		sender.send();
	}

	@Override
	public void onMatchInbound(MatchInboundEvent msg) {
		if (msg.getContributorID() != myContribId) {
			return;
		}

		if (msg.getFixMsgType() == FixMsgTypes.SequenceReset) {
			nextInboundSeqNum = msg.getEndSeqNo();
			return;
		}

		switch (msg.getFixMsgType()) {
			case FixMsgTypes.ResendRequest:
				int beginSeqNoInt = msg.getBeginSeqNo();
				int endSeqNoInt = msg.getEndSeqNo();
				log.info(log.log().add("FIX Resend Request. Starting resend from seq no: ").add(beginSeqNoInt).add(" to: ").add(endSeqNoInt));
				scheduleResendTimer(store.resend(beginSeqNoInt, endSeqNoInt));
				break;
			default:
				break;
		}

		incInboundSeqNo();
	}

	@Override
	public void onMatchOutbound(MatchOutboundEvent msg) {
		if (msg.getContributorID() != myContribId) {
			return;
		}

		FixWriter writer = null;

		switch (msg.getFixMsgType()) {
			case FixMsgTypes.Reject:
				writer = store.createMessage(msg.getFixMsgType());
				writer.writeNumber(refSeqNum, msg.getRefSeqNum());
				if (msg.hasText()) {
					writer.writeString(text, msg.getText());
				}
				writer.writeNumber(refTagID, msg.getRefTagID());
				writer.writeChar(refMsgType, msg.getRefMsgType());
				// TODO: The Fix enums can only be chars but there are more reasons than '9'
				// Stupid limitation of Direct Match that I don't want to solve now
				writer.writeNumber(sessionRejectReason, (msg.getSessionRejectReason() - '0'));
				store.finalizeAdminMessage();
				break;

			case FixMsgTypes.Logon:
				writer = store.createMessage(msg.getFixMsgType());
				writer.writeNumber(heartBIntFlag, heartbeatSessionManager.getHeartbeatTimeout());
				writer.writeChar(encryptMethod, '0');
				if (resetOnLogin) {
					writer.writeChar(this.resetSeqNumFlag, 'Y');
				}
				store.finalizeAdminMessage();
				break;

			case FixMsgTypes.ResendRequest:
				writer = store.createMessage(msg.getFixMsgType());
				writer.writeNumber(beginSeqNo, nextInboundSeqNum);
				writer.writeChar(endSeqNo, '0');
				store.finalizeAdminMessage();
				break;

			case FixMsgTypes.TestRequest:
				writer = store.createMessage(msg.getFixMsgType());
				writer.writeString(testReqID, msg.getReqID());
				store.finalizeAdminMessage();
				break;

			case FixMsgTypes.Heartbeat:
				writer = store.createMessage(msg.getFixMsgType());
				ByteBuffer reqId = msg.getReqID();
				if (reqId.hasRemaining()) {
					writer.writeString(testReqID, reqId);
				}
				store.finalizeAdminMessage();
				break;
			default:
				break;
		}
	}

	@Override
	public void reset() {
		loggedIn = false;
		heartbeatSessionManager.clear(0);
		clearRequestTimer();
		clearResendTimer();
	}

	private final TimerHandler requestTimerHandler = new TimerHandler() {
		@Override
		public void onTimer(int internalTimerID, int referenceData) {
			requestTimer = TimerService.NULL_VALUE;
		}
	};

	private final TimerHandler resendTimerHandler = new TimerHandler() {
		@Override
		public void onTimer(int internalTimerID, int referenceData) {
			resendTimer = TimerService.NULL_VALUE;
			if (nextResendSeqNo > 0) {
				int result = store.resend(nextResendSeqNo, 0);
				scheduleResendTimer(result);

				if (nextResendSeqNo == 0) {
					connector.resendComplete();
				}
			}
		}
	};

	void scheduleResendTimer(int seq) {
		clearResendTimer();
		nextResendSeqNo = seq;

		if (seq != 0) {
			resendTimer = timerService.scheduleTimer(100 * TimeUtils.NANOS_PER_MILLI, resendTimerHandler);
		}
	}

	private void clearResendTimer() {
		resendTimer = timerService.cancelTimer(resendTimer);
	}

	private void scheduleRequestTimer() {
		clearRequestTimer();
		requestTimer = timerService.scheduleTimer(10 * TimeUtils.NANOS_PER_SECOND, requestTimerHandler);
	}

	private void clearRequestTimer() {
		requestTimer = timerService.cancelTimer(requestTimer);
	}

	public int getNextInboundSeqNo() {
		return nextInboundSeqNum;
	}

	public int getNextOutboundSeqNo() {
		return store.getNextOutboundSeqNo();
	}

	public void setNextInboundSeqNum(int seqNum) {
		this.nextInboundSeqNum = seqNum;
	}

	public void setNextOutboundSeqNo(int seqNo, boolean force) {
		if (!force && seqNo < store.getNextOutboundSeqNo()) {
			throw new CommandException("Cannot reduce SeqNo from " + store.getNextOutboundSeqNo() + " => " + seqNo);
		}

		int nextNo = store.getNextOutboundSeqNo();

		if (nextNo < seqNo) {
			for (int i = nextNo; i < seqNo; i++) {
				store.writeAdminMessage();
			}
		}
		else if (nextNo > seqNo) {
			store.reset(seqNo);
		}
	}

	@Override
	public void sendHeartbeat() {
		if (sender.canSend()) {
			MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.Heartbeat);
			sender.send(outbound);
		}
	}

	@Override
	public void sendTestRequest() {
		if (sender.canSend()) {
			MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.TestRequest);
			outbound.setReqID(reqID);
			sender.send(outbound);
		}
	}
	@Override
	public boolean isResending() {
		return nextResendSeqNo > 0;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	@Override
	public void onContributorDefined(short contribID, String name) {
		this.myContribId = contribID;
	}

	public void addStatus(HeartbeatFieldRegister register) {
		nextInboundMonitor = register.addNumberField("FIX", HeartBeatFieldIDEnum.NextInboundSeq);
		nextOutboundMonitor = register.addNumberField("FIX", HeartBeatFieldIDEnum.NextOutBoundSeq);
		loggedInMonitor = register.addBoolField("FIX", HeartBeatFieldIDEnum.LoggedIn);
		resendingMonitor = register.addBoolField("FIX", HeartBeatFieldIDEnum.IsResending);
	}

	public void updateStatus() {
		nextInboundMonitor.set(getNextInboundSeqNo());
		nextOutboundMonitor.set(getNextOutboundSeqNo());
		loggedInMonitor.set(isLoggedIn());
		resendingMonitor.set(isResending() );
	}

	public void resetSequenceNums() {
		setNextInboundSeqNum(1);
		setNextOutboundSeqNo(1, true);
	}

	public void sendReject() {
		reject("Invalid-Msg-type Reject", 35, FixConstants.SessionRejectReason.InvalidMsgType);
	}

	public enum FixMessageResult {
		Disconnect,
		Complete,
		NextMessage
	}

	public void sendLogonCommand(int heartbeat)
	{
		this.heartbeatSessionManager.clear(heartbeat);
		MatchOutboundCommand logonCommand = messages.getMatchOutboundCommand();
		logonCommand.setFixMsgType(FixMsgTypes.Logon);
		this.sender.send(logonCommand);
	}
}
