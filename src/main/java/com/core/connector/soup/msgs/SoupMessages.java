package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public interface SoupMessages {
    SoupDebugCommand getSoupDebugCommand();
    SoupDebugCommand getSoupDebugCommand(ByteBuffer buffer);
    SoupDebugEvent getSoupDebugEvent(ByteBuffer buffer);
    SoupLoginAcceptedCommand getSoupLoginAcceptedCommand();
    SoupLoginAcceptedCommand getSoupLoginAcceptedCommand(ByteBuffer buffer);
    SoupLoginAcceptedEvent getSoupLoginAcceptedEvent(ByteBuffer buffer);
    SoupLoginRejectedCommand getSoupLoginRejectedCommand();
    SoupLoginRejectedCommand getSoupLoginRejectedCommand(ByteBuffer buffer);
    SoupLoginRejectedEvent getSoupLoginRejectedEvent(ByteBuffer buffer);
    SoupSequencedDataCommand getSoupSequencedDataCommand();
    SoupSequencedDataCommand getSoupSequencedDataCommand(ByteBuffer buffer);
    SoupSequencedDataEvent getSoupSequencedDataEvent(ByteBuffer buffer);
    SoupServerHeartbeatCommand getSoupServerHeartbeatCommand();
    SoupServerHeartbeatCommand getSoupServerHeartbeatCommand(ByteBuffer buffer);
    SoupServerHeartbeatEvent getSoupServerHeartbeatEvent(ByteBuffer buffer);
    SoupEndOfSessionCommand getSoupEndOfSessionCommand();
    SoupEndOfSessionCommand getSoupEndOfSessionCommand(ByteBuffer buffer);
    SoupEndOfSessionEvent getSoupEndOfSessionEvent(ByteBuffer buffer);
    SoupLoginRequestCommand getSoupLoginRequestCommand();
    SoupLoginRequestCommand getSoupLoginRequestCommand(ByteBuffer buffer);
    SoupLoginRequestEvent getSoupLoginRequestEvent(ByteBuffer buffer);
    SoupUnsequencedDataCommand getSoupUnsequencedDataCommand();
    SoupUnsequencedDataCommand getSoupUnsequencedDataCommand(ByteBuffer buffer);
    SoupUnsequencedDataEvent getSoupUnsequencedDataEvent(ByteBuffer buffer);
    SoupClientHeartbeatCommand getSoupClientHeartbeatCommand();
    SoupClientHeartbeatCommand getSoupClientHeartbeatCommand(ByteBuffer buffer);
    SoupClientHeartbeatEvent getSoupClientHeartbeatEvent(ByteBuffer buffer);
    SoupLogoutRequestCommand getSoupLogoutRequestCommand();
    SoupLogoutRequestCommand getSoupLogoutRequestCommand(ByteBuffer buffer);
    SoupLogoutRequestEvent getSoupLogoutRequestEvent(ByteBuffer buffer);
} 
