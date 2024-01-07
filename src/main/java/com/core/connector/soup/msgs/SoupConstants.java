package com.core.connector.soup.msgs;

//
// THIS FILE IS AUTO-GENERATED
//
public class SoupConstants {

    public static void setParam(String param, String value) {
    }
	
    public static class RejectReason {
        public static final char NotAuthorized = 'A';
        public static final char SessionNotAvailable = 'S';
   
        public static String toString(char code) {
            switch(code) {
                case NotAuthorized:
                    return "NotAuthorized";
                case SessionNotAvailable:
                    return "SessionNotAvailable";
                default:
                    return "Unknown";
            }
        }
    }

    public static class Messages {
        public static final char Debug = '+';
        public static final char LoginAccepted = 'A';
        public static final char LoginRejected = 'J';
        public static final char SequencedData = 'S';
        public static final char ServerHeartbeat = 'H';
        public static final char EndOfSession = 'Z';
        public static final char LoginRequest = 'L';
        public static final char UnsequencedData = 'U';
        public static final char ClientHeartbeat = 'R';
        public static final char LogoutRequest = 'O';

        public static String toString(char code) {
            switch(code) {
                case '+':
                    return "Debug";
                case 'A':
                    return "LoginAccepted";
                case 'J':
                    return "LoginRejected";
                case 'S':
                    return "SequencedData";
                case 'H':
                    return "ServerHeartbeat";
                case 'Z':
                    return "EndOfSession";
                case 'L':
                    return "LoginRequest";
                case 'U':
                    return "UnsequencedData";
                case 'R':
                    return "ClientHeartbeat";
                case 'O':
                    return "LogoutRequest";
                default:
                    return "Unknown";
            }
        }
    }
}
