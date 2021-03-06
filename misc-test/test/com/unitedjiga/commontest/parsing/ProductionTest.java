package com.unitedjiga.commontest.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unitedjiga.common.parsing.NonTerminalSymbol;
import com.unitedjiga.common.parsing.ParsingException;
import com.unitedjiga.common.parsing.Production;
import com.unitedjiga.common.parsing.SingletonSymbol;
import com.unitedjiga.common.parsing.Symbol;
import com.unitedjiga.common.parsing.SymbolVisitor;
import com.unitedjiga.common.parsing.TerminalSymbol;
import com.unitedjiga.common.parsing.Token;
import com.unitedjiga.common.parsing.Tokenizer;
import com.unitedjiga.common.parsing.TokenizerFactory;
import com.unitedjiga.common.util.Lexer;

import static com.unitedjiga.common.parsing.Production.of;
import static com.unitedjiga.common.parsing.Production.oneOf;

class ProductionTest {

    static SymbolVisitor<Void, String> printer = new SymbolVisitor<Void, String>() {

        @Override
        public Void visitTerminal(TerminalSymbol s, String p) {
            System.out.print(p);
            System.out.println(s + ":" + s.getKind());
            return null;
        }

//		@Override
//		public Void visitSingleton(SingletonSymbol s, String p) {
//			System.out.print(p);
//			System.out.println(s.getOrigin()  + ":" + s.getKind() + "=\"" + s + "\"");
//			s.forEach(e -> e.accept(this, "  " + p));
//			return null;
//		}

        @Override
        public Void visitNonTerminal(NonTerminalSymbol s, String p) {
            System.out.print(p);
            System.out.println(s.getOrigin() + ":" + s.getKind() + "=\"" + s + "\"");
            s.forEach(e -> e.accept(this, "  " + p));
            return null;
        }
    };

    static void testSuccess(Production p, String in) {
        assertTrue(p.asPattern().matcher(in).matches());

        TokenizerFactory tf = TokenizerFactory.newInstance();
        try (Tokenizer tzer = tf.createTokenizer(new StringReader(in))) {
            Symbol s = p.parseRemaining(tzer);
            s.accept(printer, "+-");
        }
    }

    static void testFailure(Production p, String in) {
        assertFalse(p.asPattern().matcher(in).matches());

        TokenizerFactory tf = TokenizerFactory.newInstance();
        try (Tokenizer tzer = tf.createTokenizer(new StringReader(in))) {
            assertThrows(ParsingException.class, () -> {
                try {
                    p.parseRemaining(tzer);
                } catch (ParsingException e) {
                    System.out.println(e);
                    throw e;
                }
            });
        }
    }

    static void testInit(Production p) {
        System.out.println(p);
        assertEquals(p.toString(), p.asPattern().pattern());
    }

    @BeforeEach
    void beforeTest() {
        System.out.println("_________________________________________________");
    }

    @Test
    void test1_1() throws IOException {
        Production A = Production.of();
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
        testFailure(A, "0");
    }

    @Test
    void test1_2() throws IOException {
        Production A = Production.of("1");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "1");
        testFailure(A, "0");
        testFailure(A, "11");
    }

    @Test
    void test1_3() throws IOException {
        Production A = Production.of("1", "0");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "10");
        testFailure(A, "1");
        testFailure(A, "0");
        testFailure(A, "01");
        testFailure(A, "100");
    }

    @Test
    void test1_4() throws IOException {
        Production A = Production.of("1|0");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "1");
        testSuccess(A, "0");
        testFailure(A, "10");
        testFailure(A, "01");
    }

    @Test
    void test2_1() throws IOException {
        Production A = Production.oneOf();
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
    }

    @Test
    void test2_2() throws IOException {
        Production A = Production.oneOf("1");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "1");
        testFailure(A, "11");
    }

    @Test
    void test2_3() throws IOException {
        Production A = Production.oneOf("1", "0");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "1");
        testSuccess(A, "0");
        testFailure(A, "11");
        testFailure(A, "00");
        testFailure(A, "10");
    }

    @Test
    void test3_1() throws IOException {
        Production A = Production.of().opt();
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
    }

    @Test
    void test3_2() throws IOException {
        Production A = Production.of("1").opt();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "1");
        testFailure(A, "0");
        testFailure(A, "11");
    }

    @Test
    void test3_3() throws IOException {
        Production A = Production.of("1", "0").opt();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "10");
        testFailure(A, "1");
        testFailure(A, "0");
    }

    @Test
    void test4_1() throws IOException {
        Production A = Production.of().repeat();
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
    }

    @Test
    void test4_2() throws IOException {
        Production A = Production.oneOf().repeat();
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
    }

    @Test
    void test4_3() throws IOException {
        Production A = Production.of("1").repeat();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "1");
        testSuccess(A, "11");
        testSuccess(A, "111");
        testFailure(A, "0");
        testFailure(A, "10");
    }

    @Test
    void test4_4() throws IOException {
        Production A = Production.of("1", "0").repeat();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "10");
        testSuccess(A, "1010");
        testSuccess(A, "101010");
        testFailure(A, "0");
        testFailure(A, "1");
        testFailure(A, "01");
        testFailure(A, "101");
    }

    @Test
    void test4_5() throws IOException {
        Production A = Production.oneOf("1").repeat();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "1");
        testSuccess(A, "11");
        testSuccess(A, "111");
        testFailure(A, "0");
        testFailure(A, "10");
    }

    @Test
    void test4_6() throws IOException {
        Production A = Production.oneOf("1", "0").repeat();
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "1");
        testSuccess(A, "0");
        testSuccess(A, "11");
        testSuccess(A, "10");
        testSuccess(A, "01");
        testSuccess(A, "00");
        testSuccess(A, Integer.toBinaryString(12));
        testFailure(A, "2");
    }

    @Test
    void test1_001() throws IOException {
        Production B = Production.of();
        Production A = Production.of(B);
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
        testFailure(A, "0");
    }

    @Test
    void test1_002() throws IOException {
        Production B = Production.of("1");
        Production A = Production.of(B);
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "1");
        testFailure(A, "0");
        testFailure(A, "11");
    }

    @Test
    void test1_003() throws IOException {
        Production B = Production.of("1", "0");
        Production A = Production.of(B);
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "10");
        testFailure(A, "1");
        testFailure(A, "0");
        testFailure(A, "01");
    }

    @Test
    void test1_004() throws IOException {
        Production C = Production.of();
        Production B = Production.of(C);
        Production A = Production.of(B);
        testInit(A);

        testSuccess(A, "");
        testFailure(A, "1");
        testFailure(A, "0");
    }

    @Test
    void test1_005() throws IOException {
        Production C = Production.of("1");
        Production B = Production.of(C.opt());
        Production A = Production.of(B, "0");
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "10");
        testSuccess(A, "0");
        testFailure(A, "1");
    }

    @Test
    void test1_006() throws IOException {
        Production C = Production.of("0");
        Production B = Production.of("1", C.opt());
        Production A = Production.of("0", B.repeat());
        testInit(A);

        testFailure(A, "");
        testSuccess(A, "0");
        testSuccess(A, "01");
        testSuccess(A, "010");
        testSuccess(A, "0101");
        testSuccess(A, "010110");
        testFailure(A, "1");
    }

    @Test
    void test1_007() throws IOException {
        Production C = Production.of("0");
        Production B = Production.of("1");
        Production A = Production.of(B.opt(), C.opt());
        testInit(A);

        testSuccess(A, "");
        testSuccess(A, "1");
        testSuccess(A, "0");
        testSuccess(A, "10");
        testFailure(A, "11");
    }

    @Test
    void test1_XXX1() throws IOException {
        /*
         * file = [header CRLF] record *(CRLF record) [CRLF] header = name *(COMMA name)
         * record = field *(COMMA field) name = field field = (escaped / non-escaped)
         * escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE non-escaped =
         * *TEXTDATA COMMA = %x2C CR = %x0D ;as per section 6.1 of RFC 2234 [2] DQUOTE =
         * %x22 ;as per section 6.1 of RFC 2234 [2] LF = %x0A ;as per section 6.1 of RFC
         * 2234 [2] CRLF = CR LF ;as per section 6.1 of RFC 2234 [2] TEXTDATA = %x20-21
         * / %x23-2B / %x2D-7E
         */
        Production TEXTDATA = Production.oneOf("[\\x20-\\x21]", "[\\x23-\\x2B]", "[\\x2D-\\x7E]");
        Production LF = Production.of("\\x0A");
        Production DQUOTE = Production.of("\\x22");
        Production CR = Production.of("\\x0D");
        Production CRLF = Production.of(CR, LF);
        Production COMMA = Production.of("\\x2C");
        Production non_escaped = Production.of(TEXTDATA.repeat());
        Production escaped = Production.of(DQUOTE,
                Production.oneOf(TEXTDATA, COMMA, CR, LF, Production.of(DQUOTE, DQUOTE)).repeat(), DQUOTE);
        Production field = Production.oneOf(escaped, non_escaped);
        Production name = field;
        Production record = Production.of(field, Production.of(COMMA, field).repeat());
        Production header = Production.of(name, Production.of(COMMA, name).repeat());
//		Production file = Production.of(
//				Production.of(header, CRLF).opt(),
//				record,
//				Production.of(CRLF, record).repeat(),
//				CRLF.opt());
        Production file = Production.of(Production.of(header, CRLF).opt(), Production.of(record, CRLF.opt()).repeat());

        StringBuilder sb = new StringBuilder();
        sb.append("TITLE,RELEASED").append("\r\n");
        sb.append("Please Please Me,22 March 1963").append("\r\n");
        sb.append("With the Beatles,22 November 1963").append("\r\n");
        sb.append("Introducing... The Beatles,10 January 1964").append("\r\n");
        sb.append("Meet the Beatles!,20 January 1964").append("\r\n");
//		sb.append("Meet the Beatles!,20 January 1964");
        testSuccess(file, sb.toString());
    }

    @Test
    void test1_XXX2() throws IOException {
        Production CR = of("\\r");
        Production LF = of("\\n");
        Production lineTerminator = oneOf(LF, of(CR, LF.opt()));
        Production commentHead = of("/", oneOf("/", "\\*"));
        Production commentTail = of("\\*", of("/").opt());
        Production comment = oneOf(commentHead, commentTail);
        Production input = oneOf(lineTerminator, comment, ".").repeat();

        StringBuilder sb = new StringBuilder();
        sb.append("//");
        sb.append("\n");
        sb.append("/*");
        sb.append("\r");
        sb.append("*");
        sb.append("\r");
        sb.append("\n");
        sb.append("*/");
        testSuccess(input, sb.toString());
    }

    @Test
    void test1_XXX3() {
        class TestTokenizerFactory implements TokenizerFactory {
            // Phase1
            // LINE_TERMINATORS
            Production LINE_TERMINATOR = oneOf(of("\\n"), of(of("\\r"), of("\\n").opt()));
            // WHITE_SPACES
            Production WHITE_SPACE = oneOf("\\x20", "\\t", "\\f", LINE_TERMINATOR);
            // COMMENTS
            Production COMMENT = oneOf(of("/", oneOf("/", "\\*")), of("\\*", of("/").opt()));
            // OPERATORS
            Production OPERATOR = oneOf(of("=", of("=").opt()), of(">", of("=").opt()), of("<", of("=").opt()));
            // IDENTIFIERS
            Production IDENTIFIER = of("\\p{Alpha}", of("\\p{Alnum}").repeat());
            // INPUT
            Production INPUT = oneOf(WHITE_SPACE, COMMENT, OPERATOR, IDENTIFIER);// .repeat();

            // Phase2
            Production END_OF_LINE_COMMENT = of("//", of(".+").repeat());
            Production TRADITIONAL_COMMNET = of("/\\*", of("(?!\\*/)(?s:.+)").repeat(), "\\*/");
            Production INPUT2 = oneOf(END_OF_LINE_COMMENT, TRADITIONAL_COMMNET, "(?s:.+)");// .repeat();

            class TestTokenizer implements Tokenizer {
                private Tokenizer tzer;
                private Production p;
                private Token t;

                public TestTokenizer(Tokenizer tzer, Production p) {
                    this.tzer = tzer;
                    this.p = p;
                }

                @Override
                public boolean hasNext() {
                    if (t != null) {
                        return true;
                    }
                    return tzer.hasNext();
                }

                @Override
                public Token next() {
                    try {
                        return peek();
                    } finally {
                        t = null;
                    }
                }

                @Override
                public Token peek() {
                    if (t == null) {
                        Symbol s = p.parse(tzer);
                        t = s.asToken();
                    }
                    return t;
                }

                @Override
                public void close() {
                    tzer.close();
                }

                @Override
                public String toString() {
                    return tzer.toString();
                }
            }

            @Override
            public Tokenizer createTokenizer(Reader r) {
                TokenizerFactory tf = TokenizerFactory.newInstance();
                Tokenizer tzer = tf.createTokenizer(r);
                tzer = new TestTokenizer(tzer, INPUT);
                tzer = new TestTokenizer(tzer, INPUT2);
                return tzer;
            }

        }

        String in = "abc=D12\r\n" + "= == < <= > >=\r" + "//Comment line\n" + "/*\n" + " *Comment block\n" + " */";
        TokenizerFactory tf = new TestTokenizerFactory();
        try (Tokenizer tzer = tf.createTokenizer(new StringReader(in))) {
            tzer.forEachRemaining(t -> System.out.println("|" + t.getValue()));
        }
    }
}
