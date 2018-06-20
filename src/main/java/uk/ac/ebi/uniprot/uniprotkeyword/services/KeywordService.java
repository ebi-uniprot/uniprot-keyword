package uk.ac.ebi.uniprot.uniprotkeyword.services;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.import_data.CombineKeywordReferenceCount;
import uk.ac.ebi.uniprot.uniprotkeyword.repositories.KeywordRepository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

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
        // Bug #DATAGRAPH-1092 in spring data can't use following
        // Pattern.compile("(?i).*\\b"+identifier.trim()+"\\b.*", Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern.compile("(?i).*\\b" + identifier.trim() + "\\b.*");

        return keywordRepository.findByIdentifierRegex(p);
    }

    @Transactional(readOnly = true)
    public Collection<Keyword> findAllByKeyWordSearch(final String input) {
        //Java don't override equals for Pattern
        final Comparator<Pattern> comp = (p1, p2) -> p1.pattern().compareTo(p2.pattern()) + (p1.flags() - p2.flags());

        //Make only unique quries for neo4j
        final Set<Pattern> words =
                Stream.of(input.split("\\s+")).map(String::toLowerCase).map(s -> compile("(?i).*\\b" + s + "\\b.*"))
                        .collect(Collectors.toCollection(() -> new TreeSet<>(comp)));

        //spring data neo4j return relationships as part of collection need to filter it
        final Set<Pattern> javaPatterns = Stream.of(input.split("\\s+")).map(String::toLowerCase)
                .map(s -> compile("\\b" + s + "\\b", Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toCollection(() -> new TreeSet<>(comp)));

        // Database will be embedded so we can query multiple times with minimum performance hit
        // We could build dynamic query to save DB hits, but that will increase code also load on DB to scan for huge
        // set
        return words.stream().flatMap(
                i -> keywordRepository.findByIdentifierRegexOrAccessionRegexOrSynonymsRegexOrDefinitionRegex(i, i, i, i)
                        .stream()).filter(k -> filterRelativeSearch(k, javaPatterns)).collect(Collectors.toSet());
    }

    private boolean filterRelativeSearch(Keyword k, Set<Pattern> patterns) {
        for (Pattern p : patterns) {
            if (stringPatternMatch(k.getIdentifier(), p) || stringPatternMatch(k.getAccession(), p) ||
                    stringPatternMatch(k.getDefinition(), p) || stringPatternMatch(k.getSynonyms(), p)) {
                return true;
            }
        }
        return false;
    }

    private boolean stringPatternMatch(String input, Pattern p) {
        if (p == null) {
            return input == null;
        }
        input = input == null ? "" : input;
        return p.matcher(input).find();
    }

    private boolean stringPatternMatch(List<String> input, Pattern p) {
        final String s = Optional.ofNullable(input).map(Object::toString).orElse(null);
        return stringPatternMatch(s, p);
    }

}
