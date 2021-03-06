/*
 * The MIT License
 *
 * Copyright 2020 Junji Mikami.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.unitedjiga.common.parsing;

import java.util.regex.Pattern;

import com.unitedjiga.common.parsing.impl.Productions;

/**
 * 正規表現ベースの生成規則です。
 * 
 * @author Junji Mikami
 */
public interface Production extends CharSequence {

    static Production of(CharSequence... regex) {
        return Productions.getInstance().of(regex);
    }

    static Production oneOf(CharSequence... regex) {
        return Productions.getInstance().oneOf(regex);
    }

    Symbol parseRemaining(Tokenizer tokenizer);

    Symbol parse(Tokenizer tokenizer);

    Production opt();

    Production repeat();

    Pattern asPattern();

    @Override
    default CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Override
    default char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    default int length() {
        return toString().length();
    }

    @Override
    String toString();
}
