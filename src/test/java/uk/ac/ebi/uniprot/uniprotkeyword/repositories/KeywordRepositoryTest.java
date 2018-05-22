package uk.ac.ebi.uniprot.uniprotkeyword.repositories;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.import_data.CombineKeywordReferenceCount;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataNeo4jTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeywordRepositoryTest {

    private static final String ACCESSION_PROP = "accession";

    @Autowired
    private KeywordRepository repo;

    @BeforeAll
    void setupAll() {
        final CombineKeywordReferenceCount obj = new CombineKeywordReferenceCount();
        final String sampleDataFilePath = ClassLoader.getSystemResource("sample-kw-relation.txt").getPath();
        final List<Keyword> keywordList = obj.readFileImportAndCombine(sampleDataFilePath, null);
        repo.saveAll(keywordList);
    }

    /**
     * Test of findByAccession method, of class KeywordRepository.
     */
    @Test
    void findByAccessionHierarchyDept() {
        final String accession = "KW-0001";
        final List<Keyword> retList = repo.findByAccession(accession);
        assertAll(
                () -> {
                    assertNotNull(retList);
                    Keyword top = retList.get(0);
                    assertAll(
                            () -> assertEquals("KW-0001", top.getAccession()),
                            () -> assertThat(top.getSynonyms()).isNotNull().hasSize(6),
                            () -> assertThat(top.getCategory()).isNotNull().extracting(ACCESSION_PROP)
                                    .contains("KW-9993"),
                            () -> assertThat(top.getGoMappings()).isNotNull().hasSize(1)
                                    .anyMatch(go -> go.getGoId().equals("GO:0051537") && go.getDefinition().equals
                                            ("2 iron, 2 sulfur cluster binding"))
                    );
                    assertAll(
                            () -> assertThat(top.getHierarchy()).isNotNull().hasSize(2),
                            () -> assertThat(top.getHierarchy()).extracting(ACCESSION_PROP)
                                    .contains("KW-0411", "KW-0479")
                    );
                    assertAll(
                            () -> {
                                Keyword parent =
                                        top.getHierarchy().stream().filter(k -> k.getAccession().equals("KW-0411"))
                                                .findFirst().get();
                                assertAll(
                                        () -> assertThat(parent.getCategory()).isNotNull().extracting(ACCESSION_PROP)
                                                .contains("KW-9993"),
                                        () -> assertThat(parent.getGoMappings()).isNotNull().hasSize(1)
                                                .anyMatch(go -> go.getGoId().equals("GO:0051536") &&
                                                        go.getDefinition().equals
                                                                ("iron-sulfur cluster binding"))
                                );
                                assertAll(
                                        () -> assertThat(parent.getHierarchy()).isNotNull().hasSize(2),
                                        () -> assertThat(parent.getHierarchy()).extracting(ACCESSION_PROP)
                                                .contains("KW-0408",
                                                        "KW-0479")
                                );
                            }
                    );
                }
        );
    }

    /**
     * Test of findByIdentifierIgnoreCaseLike method, of class KeywordRepository.
     */
    @Test
    void identifierIgnoringCase() {
        final String id = "*I*";
        final Collection<Keyword> result = repo.findByIdentifierIgnoreCaseLike(id);
        assertThat(result).isNotNull().hasSize(4);
        assertThat(result).extracting(ACCESSION_PROP).doesNotContain("KW-0001");

        //All keyword should be category attached
        result.stream().filter(k -> !k.isCategoryMarker()).forEach(
                keyword -> assertThat(keyword.getCategory()).extracting(ACCESSION_PROP).contains("KW-9993")

        );
    }

    @Test
    void findByIdentifierReturningResultFromDb() {
        final String identifier = "2Fe-2S";
        final List<Keyword> retList = repo.findByIdentifier(identifier);
        //Category is also a keyword
        assertThat(retList).isNotNull().hasSize(5);
    }

    @Test
    void identifierCaseChangeWillNotWork() {
        final String identifier = "2fe-2S";
        final List<Keyword> retList = repo.findByIdentifier(identifier);

        assertThat(retList).isNotNull().hasSize(0);
    }

    @Test
    void searchInIdentifierAAccessionDefinitionSynonyms() {
        final String input = "*sulfur*";
        final Collection<Keyword> retCol = repo
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        input, input, input, input);
        assertNotNull(retCol);
        assertEquals(2, retCol.size());
    }

}
