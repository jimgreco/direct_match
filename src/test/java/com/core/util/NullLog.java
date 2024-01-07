package com.core.util;

import java.nio.ByteBuffer;

import com.core.util.log.Log;
import com.core.util.log.Logger;

public class NullLog implements Log {

	private final NullLogger logger = new NullLogger();
	
	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void setDebug(boolean debug) {
	}

	@Override
	public void debug(Logger log) {
	}

	@Override
	public void info(Logger log) {
	}

	@Override
	public void warn(Logger log) {
	}

	@Override
	public void error(Logger log) {
	}

	@Override
	public Logger log() {
		return logger;
	}

	private class NullLogger implements Logger {

		@Override
		public Logger add(boolean b) {
			return this;
		}

		@Override
		public Logger add(char b) {
			return this;
		}

		@Override
		public Logger add(byte b) {
			return this;
		}

		@Override
		public Logger add(String str) {
			return this;
		}

		@Override
		public Logger add(ByteBuffer buf) {
			return this;
		}

		@Override
		public Logger add(byte[] bytes) {
			return this;
		}

		@Override
		public Logger add(long i) {
			return this;
		}

		@Override
		public Logger add(Throwable e) {
			return this;
		}

		@Override
		public Logger addAsHex(ByteBuffer buf) {
			return this;
		}
		
	}
	
}
