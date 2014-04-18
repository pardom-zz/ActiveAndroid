
package com.activeandroid.test.parser;

import com.activeandroid.test.ActiveAndroidTestCase;
import com.activeandroid.test.R;
import com.activeandroid.util.SqlParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class ParserTest extends ActiveAndroidTestCase {

    private final String sql1    = "CREATE TABLE Entity1 ( Id INTEGER AUTOINCREMENT PRIMARY KEY NOT NULL, Column1 INTEGER )";
    private final String sql2    = "CREATE TABLE Entity2 ( Id INTEGER AUTOINCREMENT PRIMARY KEY NOT NULL, Column1 INTEGER )";

    private final String invalid = "CREATE TABLE Entity1 ( Id INTEGER AUTOINCREMENT PRIMARY KEY NOT NULL, */ Column1 INTEGER )";

    private InputStream getStream(int id) {
        return this.getContext().getResources().openRawResource(id);
    }

    /**
     * Should be able to parse a script with two multi-line statments, even if the last statement
     * is not terminated by a semicolon.
     * @throws IOException
     */
    public void testTwoStatements() throws IOException {

        final InputStream stream = this.getStream(R.raw.two_statements);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(2, commands.size());
        assertEquals(sql1, commands.get(0));
        assertEquals(sql2, commands.get(1));
    }

    /**
     * Should reduce unnecessary whitespace.
     * @throws IOException
     */
    public void testWhitespace() throws IOException {

        final InputStream stream = this.getStream(R.raw.whitespace);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to parse a multi-line statement that has an embedded line comment.
     * @throws IOException
     */
    public void testLineComment() throws IOException {

        final InputStream stream = this.getStream(R.raw.line_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to handle a line comment that contains string tokens.
     * @throws IOException
     */
    public void testLineCommentWithString() throws IOException {

        final InputStream stream = this.getStream(R.raw.line_comment_with_string);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to handle a line comment that contains a semicolon.
     * @throws IOException
     */
    public void testLineCommentWithSemicolon() throws IOException {

        final InputStream stream = this.getStream(R.raw.line_comment_with_semicolon);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should ignore a block comment end token inside a line comment.
     * @throws IOException
     */
    public void testLineAndBlockEndComment() throws IOException {

        final InputStream stream = this.getStream(R.raw.line_comment_and_block_end);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to handle a block comment.
     * @throws IOException
     */
    public void testBlockComment() throws IOException {

        final InputStream stream = this.getStream(R.raw.block_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to handle a block comment that contains string tokens.
     * @throws IOException
     */
    public void testBlockCommentWithString() throws IOException {

        final InputStream stream = this.getStream(R.raw.block_comment_with_string);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to handle a block comment that contains a semicolon.
     * @throws IOException
     */
    public void testBlockCommentWithSemicolon() throws IOException {

        final InputStream stream = this.getStream(R.raw.block_comment_with_semicolon);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should ignore a line comment token inside a block comment.
     * @throws IOException
     */
    public void testBlockAndLineComment() throws IOException {

        final InputStream stream = this.getStream(R.raw.block_with_line_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql1, commands.get(0));
    }

    /**
     * Should be able to parse a script that incorrectly closes a block comment twice. The
     * resulting script is not expected to run, but the parser shouldn't choke on it.
     * @throws IOException
     */
    public void testInvalidBlockComment() throws IOException {

        final InputStream stream = this.getStream(R.raw.invalid_block_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(invalid, commands.get(0));
    }

    /**
     * Should ignore a line comment token inside a string.
     * @throws IOException
     */
    public void testStringWithLineComment() throws IOException {
        final String sql = "INSERT INTO Entity ( Id, Column1, Column2 ) VALUES ( 1, '-- some text', 'some text' )";

        final InputStream stream = this.getStream(R.raw.string_with_line_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql, commands.get(0));
    }

    /**
     * Should ignore block comment tokens inside strings.
     * @throws IOException
     */
    public void testStringWithBlockComment() throws IOException {
        final String sql = "INSERT INTO Entity ( Id, Column1, Column2 ) VALUES ( 1, '/* some text', 'some text */' )";

        final InputStream stream = this.getStream(R.raw.string_with_block_comment);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql, commands.get(0));
    }

    /**
     * Should ignore semicolons inside strings.
     * @throws IOException
     */
    public void testStringWithSemicolon() throws IOException {
        final String sql = "INSERT INTO Entity ( Id, Column1, Column2 ) VALUES ( 1, 'some ; text', 'some ; text' )";

        final InputStream stream = this.getStream(R.raw.string_with_semicolon);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql, commands.get(0));
    }

    /**
     * Should not clobber whitespace in strings.
     * @throws IOException
     */
    public void testStringWithWhitespace() throws IOException {
        final String sql = "INSERT INTO Entity ( Id, Column1, Column2 ) VALUES ( 1, 'some\t\t\ttext', 'some    text' )";

        final InputStream stream = this.getStream(R.raw.string_with_whitespace);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(1, commands.size());
        assertEquals(sql, commands.get(0));
    }

    /**
     * Should be able to handle a script that contains anything nasty I can thing of right now.
     * @throws IOException 
     */
    public void testComplex() throws IOException {
        final String sql1 = "CREATE TABLE Entity2 ( Id INTEGER AUTO_INCREMENT PRIMARY KEY, Column TEXT NOT NULL, Column2 INTEGER NULL )";
        final String sql2 = "INSERT INTO Entity2 ( Id, Column, Column2 ) SELECT Id, Column, 0 FROM Entity";
        final String sql3 = "DROP TABLE Entity";
        final String sql4 = "ALTER TABLE Entity2 RENAME TO Entity";
        final String sql5 = "INSERT INTO Entity2 ( Id, Column, Column2) VALUES ( 9001 , 42, 'string /* string */ -- string' )";

        final InputStream stream = this.getStream(R.raw.complex);
        List<String> commands = SqlParser.parse(stream);

        assertEquals(5, commands.size());
        assertEquals(sql1, commands.get(0));
        assertEquals(sql2, commands.get(1));
        assertEquals(sql3, commands.get(2));
        assertEquals(sql4, commands.get(3));
        assertEquals(sql5, commands.get(4));
    }
}
