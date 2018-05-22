package uk.ac.ebi.uniprot.uniprotkeyword.services;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.repositories.KeywordRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {
    @Mock
    private KeywordRepository repo;
    private KeywordService keywordService;
    private static List<Keyword> data;

    @BeforeAll
    static void data() {
        final Keyword l = new Keyword("Developmental protein", "KW-0217");
        final Keyword o = new Keyword("Developmental stage", "KW-9996");
        final Keyword t = new Keyword("Diabetes insipidus", "KW-0218");
        data = Arrays.asList(l, o, t);
    }

    @BeforeEach
    void setup() {
        keywordService = new KeywordService(repo);
    }

    @Test
    void findByAccessionShouldReturnFirstResultInRepoList() {
        when(repo.findByAccession(anyString())).thenReturn(data);
        Keyword found = keywordService.findByAccession("KW-0217");
        assertNotNull(found);
        assertEquals("Developmental protein", found.getIdentifier());
    }

    @Test
    void findByIdentifierShouldReturnFirstResultInRepoList() {
        when(repo.findByIdentifier(anyString()))
                .thenReturn(Arrays.asList(data.get(2), data.get(0), data.get(1)));

        Keyword found = keywordService.findByIdentifier("Diabetes insipidus");
        assertNotNull(found);
        assertEquals("KW-0218", found.getAccession());
    }

    @Test
    void findByIdentifierIgnoreCaseLikeShouldAddStarInParam() {
        when(repo.findByIdentifierIgnoreCaseLike(eq("*i*"))).thenReturn(data);
        Collection<Keyword> retCol = keywordService.findByIdentifierIgnoreCaseLike("i");
        assertNotNull(retCol);
        assertEquals(3, retCol.size());
    }

    @Test
    void importEntriesNullPassNoException() {
        doAnswer(returnsFirstArg()).when(repo).saveAll(anyCollection());
        keywordService.importKeywordEntriesFromFileIntoDb(null);
    }

    @Test
    public void testKeywordSearch() {
        String input = "*inner*";
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input));

        input = "*man*";
        doReturn(data.subList(1, 2)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input));

        input = "*outer*";
        doReturn(data.subList(2, 3)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input));

        input = "*not*";
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input));

        Collection<Keyword> retCol = keywordService.findAllByKeyWordSearch("inNer Outer INNER man noT");
        assertNotNull(retCol);
        assertEquals(3, retCol.size());

        verify(repo, times(4))
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        anyString(), anyString(), anyString(), anyString());
    }
}
