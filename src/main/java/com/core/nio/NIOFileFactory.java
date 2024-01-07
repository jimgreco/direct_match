package com.core.nio;

import com.core.util.file.File;
import com.core.util.file.FileFactory;

import java.io.IOException;

/**
 * Created by jgreco on 2/5/15.
 */
public class NIOFileFactory implements FileFactory {
    @Override
    public File createFile(String name, String mode) throws IOException {
        return new NIOFile(name, mode);
    }
}
