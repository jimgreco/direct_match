package com.core.util.file;

import java.io.IOException;

/**
 * User: jgreco
 */
public interface FileFactory {
    File createFile(String name, String mode) throws IOException;
}
