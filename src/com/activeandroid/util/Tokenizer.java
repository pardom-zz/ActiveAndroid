
package com.activeandroid.util;

import java.io.IOException;
import java.io.InputStream;


public class Tokenizer {

    private final InputStream mStream;

    private boolean           mIsNext;
    private int               mCurrent;

    public Tokenizer(final InputStream in) {
        this.mStream = in;
    }

    public boolean hasNext() throws IOException {

        if (!this.mIsNext) {
            this.mIsNext = true;
            this.mCurrent = this.mStream.read();
        }
        return this.mCurrent != -1;
    }

    public int next() throws IOException {

        if (!this.mIsNext) {
            this.mCurrent = this.mStream.read();
        }
        this.mIsNext = false;
        return this.mCurrent;
    }

    public boolean skip(final String s) throws IOException {

        if (s == null || s.length() == 0) {
            return false;
        }

        if (s.charAt(0) != this.mCurrent) {
            return false;
        }

        final int len = s.length();
        this.mStream.mark(len - 1);

        for (int n = 1; n < len; n++) {
            final int value = this.mStream.read();

            if (value != s.charAt(n)) {
                this.mStream.reset();
                return false;
            }
        }
        return true;
    }
}
