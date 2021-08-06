package goblinbob.mobends.test.core.util;

import static org.junit.Assert.*;
import org.junit.Test;

import goblinbob.mobends.core.util.WildcardPattern;

public class WildcardPatternTest
{
    @Test
    public void exactPatternMatch()
    {
        WildcardPattern pattern = new WildcardPattern("stringToMatch");
        assertTrue(pattern.matches("stringToMatch"));

        // Invalid strings
        assertFalse(pattern.matches("StringToMatch"));
        assertFalse(pattern.matches(" stringToMatch"));
        assertFalse(pattern.matches("stringToMatch  "));
        assertFalse(pattern.matches(" stringToMatch  "));
    }

    @Test
    public void prefixPatternMatch()
    {
        WildcardPattern pattern = new WildcardPattern("m_*");
        assertTrue(pattern.matches("m_"));
        assertTrue(pattern.matches("m_memberField"));
        assertTrue(pattern.matches("m_MemberField"));
        assertTrue(pattern.matches("m___anything"));

        // Invalid strings
        assertFalse(pattern.matches("m"));
        assertFalse(pattern.matches(" m_memberField"));
        assertFalse(pattern.matches("StringToMatch"));
        assertFalse(pattern.matches(" stringToMatch"));
        assertFalse(pattern.matches("stringToMatch  "));
        assertFalse(pattern.matches(" stringToMatch  "));
    }

    @Test
    public void suffixPatternMatch()
    {
        WildcardPattern pattern = new WildcardPattern("*.txt");
        assertTrue(pattern.matches("file.txt"));
        assertTrue(pattern.matches("File.txt"));
        assertTrue(pattern.matches("m_MemberField.txt"));
        assertTrue(pattern.matches("Nice text file.txt"));

        // Invalid strings
        assertFalse(pattern.matches("m"));
        assertFalse(pattern.matches("file.txt "));
        assertFalse(pattern.matches("File.txt  "));
        assertFalse(pattern.matches("File.TXT"));
    }

    @Test
    public void containsPatternMatch()
    {
        WildcardPattern pattern = new WildcardPattern("*inner*");
        assertTrue(pattern.matches("inner"));
        assertTrue(pattern.matches("Has_inner_content"));
        assertTrue(pattern.matches("innerContent"));
        assertTrue(pattern.matches("something_inner"));

        // Invalid strings
        assertFalse(pattern.matches("m"));
        assertFalse(pattern.matches("INNER"));
    }
}
