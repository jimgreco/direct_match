package com.core.nio;

/**
 * User: jgreco
 */
interface SelectorHandler {
    void onAccept();
    void onConnect();
    void onRead();
    void onWrite();
}
