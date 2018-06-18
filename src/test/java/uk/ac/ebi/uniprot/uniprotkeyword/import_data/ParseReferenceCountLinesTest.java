package uk.ac.ebi.uniprot.uniprotkeyword.import_data;

import uk.ac.ebi.uniprot.uniprotkeyword.dto.KeywordReferenceCount;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

public class ParseReferenceCountLinesTest {
    private static final String PROP_SWISSCOUNT = "swissProtCount";
    private static final String PROP_TREMBLCOUNT = "tremblCount";
    private static final String PROP_ACCESSION = "accession";
    private final ParseReferenceCountLines obj = new ParseReferenceCountLines();

    @Test
    void emptyStringAsInputLineParse() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList(""));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void spacesInLineMustIgnoreByParser() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("        "));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void lineStartingWithHashIgnoreByParser() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("# hello this is test"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void lineStartingWithSpaceFollowedByHashIgnoreByParser() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("      #    any line"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void passingNullToParser() {
        assertThrows(IllegalArgumentException.class, () -> obj.parseLines(null));
    }

    @Test
    void lessThenThreeTokensShouldBeIgnore() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("one", "one,two"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void firstTokenAlwaysBeString() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("1,0,0"));
        assertFalse(retCol.isEmpty());
        assertIterableEquals(retCol, Arrays.asList(new KeywordReferenceCount("1")));
    }

    @Test
    void firstTokenEmptyNotValid() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList(",0,0"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void secondTokenShoudBeParseableAsInteger() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("one,two,three"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void secondTokenOtherThanZeroOrOneNotValid() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("one,-1,three", "one,2,three"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void thirdTokenShoudBeParseableAsInteger() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("one,0,three", "one,0,"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void emptyLinesShouldBeIgnore() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("# this is header", "\n"));
        assertTrue(retCol.isEmpty());
    }

    @Test
    void validRecordWithSwissProtCount() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("KW-0001,0,2098"));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).extracting(PROP_SWISSCOUNT).contains(2098L);
    }

    @Test
    void validRecordWithTrEMBLCount() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("KW-0001,1,118900"));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).extracting(PROP_TREMBLCOUNT).contains(118900L);
    }

    @Test
    void validRecordMergeAndCount() {
        final Collection<KeywordReferenceCount> retCol =
                obj.parseLines(Arrays.asList("KW-0001,0,2098", "KW-0001,1,118900"));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).hasSize(1).extracting(PROP_ACCESSION, PROP_SWISSCOUNT, PROP_TREMBLCOUNT).contains(new Tuple
                ("KW-0001", 2098L, 118900L));
    }

    @Test
    void havingDoubleQoutesShouldBeValidRecord() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("\"KW-0005\",\"0\",\"1189\""));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).extracting(PROP_SWISSCOUNT).contains(1189L);
    }

    @Test
    void havingDoubleQoutesInOnEntryShouldBeValid() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("\"KW-0006\",0,1116",
                "KW-0007,\"1\",1117", "KW-0008,0,\"1118\"", "\"KW-0009\",1,\"1119\""));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).hasSize(4);
    }

    @Test
    void havingOneDoubleQoutesWillBeIgnoreAsWell() {
        final Collection<KeywordReferenceCount> retCol = obj.parseLines(Arrays.asList("\"KW-0016,0,1106",
                "KW-0017,1\",1107", "KW-0018,0,\"1108", "KW-0019\",1,1109\""));
        assertFalse(retCol.isEmpty());
        assertThat(retCol).hasSize(4);
        assertThat(retCol).extracting(PROP_ACCESSION).contains("KW-0016", "KW-0017", "KW-0018", "KW-0019");
    }
}
