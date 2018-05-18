package uk.ac.ebi.uniprot.uniprotkeyword.import_data;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class CombineKeywordReferenceCountTest {
    private final CombineKeywordReferenceCount obj = new CombineKeywordReferenceCount();

    @Test
    void keywordAndReferenceFilesNotExistIgnoreException() {
        final List<Keyword> retList = obj.readFileImportAndCombine("abc", "def");
        assertNotNull(retList);
        assertThat(retList).hasSize(0);
    }

    @Test
    void keywordFileMissingReferenceValidFile() {
        final String referenceCountFilePath = ClassLoader.getSystemResource("sample-reference.txt").getPath();
        final List<Keyword> retList = obj.readFileImportAndCombine(null, referenceCountFilePath);
        assertNotNull(retList);
        assertThat(retList).hasSize(0);
    }

    @Test
    void onlyKeywordsReferenceFileNotFound() throws IOException {
        final String keywordFilePath = ClassLoader.getSystemResource("sample-keywords.txt").getPath();
        final List<Keyword> retList = obj.readFileImportAndCombine(keywordFilePath, "tmp");

        assertAll(
                () -> assertNotNull(retList),
                () -> assertThat(retList).hasSize(4),
                () -> assertThat(retList.get(0)).extracting("accession", "swissProtCount", "tremblCount")
                        .containsExactly("KW-0002", 0L, 0L),
                () -> assertThat(retList.get(3)).extracting("accession", "swissProtCount", "tremblCount")
                        .containsExactly("KW-9990", 0L, 0L)
        );
    }

    @Test
    void updatedObjectMatchCountForKeywordAndCategory() throws IOException {
        final String keywordFilePath = ClassLoader.getSystemResource("sample-keywords.txt").getPath();
        final String referenceCountFilePath = ClassLoader.getSystemResource("sample-reference.txt").getPath();
        final List<Keyword> retList = obj.readFileImportAndCombine(keywordFilePath, referenceCountFilePath);

        assertAll(
                () -> assertNotNull(retList),
                () -> assertThat(retList).hasSize(4),
                () -> assertThat(retList.get(0)).extracting("accession", "swissProtCount", "tremblCount")
                        .containsExactly("KW-0002", 26351L, 18012L),
                () -> assertThat(retList.get(3)).extracting("accession", "swissProtCount", "tremblCount")
                        .containsExactly("KW-9990", 509965L, 90360625L)
        );
    }
}
