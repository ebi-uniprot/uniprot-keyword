package uk.ac.ebi.uniprot.uniprotkeyword.import_data;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.dto.KeywordReferenceCount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombineKeywordReferenceCount {
    private static final Logger LOG = LoggerFactory.getLogger(CombineKeywordReferenceCount.class);

    public List<Keyword> readFileImportAndCombine(String keywordFilePath, String referenceCountFilePath) {
        keywordFilePath = keywordFilePath == null ? "" : keywordFilePath.trim();
        referenceCountFilePath = referenceCountFilePath == null ? "" : referenceCountFilePath.trim();

        List<String> allLines = null;
        LOG.debug("File: {} to import keyword data set ", keywordFilePath);
        try {
            allLines = Files.readAllLines(Paths.get(keywordFilePath));
        } catch (IOException e) {
            LOG.error("Exception Handle gracefully: Failed to read file {} ", keywordFilePath, e);
            allLines = Collections.emptyList();
        }
        LOG.debug("total {} lines found in file ", allLines.size());

        final ParseKeywordLines parser = new ParseKeywordLines();
        List<Keyword> keywordList = parser.parseLines(allLines);
        LOG.info("total {} entries found in file ", keywordList.size());

        LOG.debug("File: {} to import keyword data set ", referenceCountFilePath);
        try {
            allLines = Files.readAllLines(Paths.get(referenceCountFilePath));
        } catch (IOException e) {
            LOG.error("Exception Handle gracefully: Failed to read file {} ", referenceCountFilePath, e);
            allLines = Collections.emptyList();
        }
        LOG.debug("total {} lines found in file ", allLines.size());

        final ParseReferenceCountLines refParser = new ParseReferenceCountLines();
        Collection<KeywordReferenceCount> referenceCountList = refParser.parseLines(allLines);
        LOG.info("total {} Reference count found in file ", referenceCountList.size());

        updateKeywordsWithReferenceCount(keywordList, referenceCountList);

        return keywordList;
    }

    private void updateKeywordsWithReferenceCount(List<Keyword> keywordList,
            Collection<KeywordReferenceCount> referenceCountList) {
        // Loop on reference count list including keywords and category
        referenceCountList.forEach(
                // Find the keyword from keyword list
                rc -> keywordList.stream().filter(k -> k.getAccession().equals(rc.getAccession())).findFirst()
                        .ifPresent(
                                //Keyword or category found from list
                                kw -> {
                                    //Updating keyword or category count from reference count object
                                    kw.setSwissProtCount(rc.getSwissProtCount());
                                    kw.setTremblCount(rc.getTremblCount());
                                }
                        )

        );
    }
}
