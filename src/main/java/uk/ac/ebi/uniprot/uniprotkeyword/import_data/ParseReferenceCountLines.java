package uk.ac.ebi.uniprot.uniprotkeyword.import_data;

import uk.ac.ebi.uniprot.uniprotkeyword.dto.KeywordReferenceCount;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseReferenceCountLines {

    private static final String SEPARATOR = ",";
    private static final String QOUTES = "\"";
    private static final String EMPTY = "";
    private static final int SWISSPROT = 0;
    private static final int TREMBL = 1;
    private static final Logger LOG = LoggerFactory.getLogger(ParseReferenceCountLines.class);

    public Collection<KeywordReferenceCount> parseLines(List<String> lines) {

        if (lines == null) {
            throw new IllegalArgumentException("List can be empty but not NULL");
        }

        Map<String, KeywordReferenceCount> tempMap = new HashMap<>(600);
        lines.stream();
        for (String line : lines) {
            if (isValidRecord(line)) {
                final String[] ref = line.split(SEPARATOR);
                final String accession = ref[0].trim().replace(QOUTES, EMPTY);
                final int type = Integer.parseInt(ref[1].trim().replace(QOUTES, EMPTY));
                final Long count = Long.valueOf(ref[2].trim().replace(QOUTES, EMPTY));

                tempMap.merge(accession, getKeywordReferenceCountObject(accession, type, count), this::merge);
            } else {
                LOG.info("Ignoring line {} while parsing", line);
            }
        }

        return tempMap.values();
    }

    private KeywordReferenceCount getKeywordReferenceCountObject(final String accession, final int type, final Long
            count) {
        final KeywordReferenceCount retObj = new KeywordReferenceCount(accession);
        if (type == SWISSPROT) {
            retObj.setSwissProtCount(count);
        } else if (type == TREMBL) {
            retObj.setTremblCount(count);
        }
        return retObj;
    }

    private KeywordReferenceCount merge(KeywordReferenceCount old, KeywordReferenceCount val) {
        final KeywordReferenceCount retObj = new KeywordReferenceCount(val.getAccession());
        retObj.setSwissProtCount(Math.max(old.getSwissProtCount(), val.getSwissProtCount()));
        retObj.setTremblCount(Math.max(old.getTremblCount(), val.getTremblCount()));
        return retObj;
    }

    private boolean isValidRecord(String line) {
        line = line == null ? "" : line.trim();

        if (line.isEmpty() || line.startsWith("#") || line.split(SEPARATOR).length < 3) {
            return false;
        }

        String[] tokens = Stream.of(line.split(SEPARATOR)).map(t -> t.replace(QOUTES, EMPTY)).toArray(String[]::new);

        if (tokens[0] == null || tokens[0].trim().isEmpty()) {
            return false;
        } else if (tokens[1] == null || tokens[1].trim().isEmpty() || !("0".equals(tokens[1].trim()) || "1".equals
                (tokens[1].trim()))) {
            return false;
        } else {
            try {
                Long.valueOf(tokens[2].trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
