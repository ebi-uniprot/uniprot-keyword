package uk.ac.ebi.uniprot.uniprotkeyword.services;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.import_data.CombineKeywordReferenceCount;
import uk.ac.ebi.uniprot.uniprotkeyword.repositories.KeywordRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeywordService {
    private final KeywordRepository keywordRepository;

    public KeywordService(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    public void importKeywordEntriesFromFileIntoDb(final String pathToKeywordFile) {
        importKeywordAndReferenceCountFromFilesIntoDb(pathToKeywordFile, "reference count file not given");
    }

    @Transactional
    public void importKeywordAndReferenceCountFromFilesIntoDb(final String pathToKeywordFile, final String
            pathToReferenceFile) {
        CombineKeywordReferenceCount obj = new CombineKeywordReferenceCount();
        final List<Keyword> keywords = obj.readFileImportAndCombine(pathToKeywordFile, pathToReferenceFile);
        keywordRepository.saveAll(keywords);
    }

    @Transactional(readOnly = true)
    public Keyword findByAccession(final String accession) {
        final List<Keyword> result = keywordRepository.findByAccession(accession);
        return result.isEmpty() ? null : result.get(0);
    }

    @Transactional(readOnly = true)
    public Keyword findByIdentifier(final String identifier) {
        final List<Keyword> result = keywordRepository.findByIdentifier(identifier);
        return result.isEmpty() ? null : result.get(0);
    }

    @Transactional(readOnly = true)
    public Collection<Keyword> findByIdentifierIgnoreCaseLike(final String identifier) {
        return keywordRepository.findByIdentifierIgnoreCaseLike("*" + identifier + "*");
    }

    @Transactional(readOnly = true)
    public Collection<Keyword> findAllByKeyWordSearch(final String input) {
        Set<String> words =
                Stream.of(input.split("\\s+")).map(s -> "*" + s.toLowerCase() + "*").collect(Collectors.toSet());
        // Database will be embedded so we can query multiple times with minimum performance hit
        // We could build dynamic query to save DB hits, but that will increase code also load on DB to scan for huge
        // set
        return words.stream()
                .flatMap(i -> keywordRepository
                        .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                                i, i, i, i).stream()).collect(Collectors.toSet());
    }

}
