package uk.ac.ebi.uniprot.uniprotkeyword.controllers;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.services.KeywordService;

import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DefaultController {

    private final KeywordService keywordService;

    public DefaultController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping("accession/{accession}")
    public Keyword findByAccession(@PathVariable String accession) {
        return keywordService.findByAccession(accession);
    }

    @GetMapping("identifier/{identifier}")
    public Keyword findByIdentifier(@PathVariable String identifier) {
        return keywordService.findByIdentifier(identifier);
    }

    @GetMapping("identifier/all/{identifier}")
    public Collection<Keyword> findByidentifierLikeIgnoreCase(@PathVariable String identifier) {
        return keywordService.findByIdentifierIgnoreCaseLike(identifier);
    }

    @GetMapping("search/{wordSeperatedBySpace}")
    public Collection<Keyword> search(@PathVariable String wordSeperatedBySpace) {
        return keywordService.findAllByKeyWordSearch(wordSeperatedBySpace);
    }
}
