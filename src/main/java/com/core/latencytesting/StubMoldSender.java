package com.core.latencytesting;

import com.core.connector.AllCommandsClearedListener;
import com.core.connector.ContributorDefinedListener;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchContributorEvent;

import java.nio.ByteBuffer;

/**
 * Created by hli on 5/1/16.
 */
public class StubMoldSender implements MatchCommandSender {
    @Override
    public boolean send(MatchCommonCommand command) {
        return false;
    }

    @Override
    public void add(MatchCommonCommand command) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public short getContribID() {
        return 0;
    }

    @Override
    public void setContribID(short contributorID) {

    }

    @Override
    public String getDestinationAddress() {
        return null;
    }

    @Override
    public String getSession() {
        return null;
    }

    @Override
    public int getLastSeenContribSeqNum() {
        return 0;
    }

    @Override
    public int getContribSeqNum() {
        return 0;
    }

    @Override
    public int incContribSeqNum() {
        return 0;
    }

    @Override
    public boolean canSend() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isCaughtUp() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public void setActive() {

    }

    @Override
    public void setPassive() {

    }

    @Override
    public void init() {

    }

    @Override
    public void add(ByteBuffer buffer) {

    }

    @Override
    public boolean send() {
        return false;
    }

    @Override
    public void onMyMessage(int contributorSeq) {

    }

    @Override
    public void addAllCommandsClearedListener(AllCommandsClearedListener listener) {

    }

    @Override
    public void addContributorDefinedListener(ContributorDefinedListener listener) {

    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {

    }
}
