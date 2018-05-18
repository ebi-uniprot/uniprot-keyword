package uk.ac.ebi.uniprot.uniprotkeyword.import_data;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.GeneOntology;
import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseKeywordLines {
    private static final String SPLIT_SPACES = "   ";
    private static final String CATEGORY_SEPARATOR = ":";
    private static final String HIERARCHY_SEPARATOR = ";";
    private static final Logger LOG = LoggerFactory.getLogger(ParseKeywordLines.class);

    public List<Keyword> parseLines(List<String> lines) {
        List<KeyFileEntry> rawList = convertLinesIntoInMemoryObjectList(lines);
        List<Keyword> list = parseKeywordFileEntryList(rawList);
        updateListWithRelationShips(list, rawList);
        return list;
    }

    private void updateListWithRelationShips(List<Keyword> list, List<KeyFileEntry> rawList) {
        for (KeyFileEntry raw : rawList) {
            //category will not have relationship, so ignore them
            if (raw.hi.isEmpty()) {
                continue;
            }

            //Only getting keywords
            Keyword target = findByIdentifier(list, raw.id);
            assert (target != null);

            //Setting the category
            target.setCategory(findByIdentifier(list, raw.ca));

            final List<String> withOutCategory = raw.hi.stream().map(s -> s.substring(s.indexOf(CATEGORY_SEPARATOR) + 1))
                    .collect(Collectors.toList());

            final Set<String> directRelations =
                    withOutCategory.stream()
                            .map(s -> s.split(HIERARCHY_SEPARATOR))
                            .filter(arr -> arr.length >= 2)
                            .map(arr -> arr[arr.length - 2]).map(this::trimSpacesAndRemoveLastDot)
                            .collect(Collectors.toSet());

            // getting relationships
            final List<Keyword> relations = directRelations.stream().map(s -> findByIdentifier(list, s)).collect
                    (Collectors.toList());
            //Only setting hierarchy if present
            target.setHierarchy(relations.isEmpty() ? null : relations);

        }
    }

    private Keyword findByIdentifier(List<Keyword> list, String id) {
        return list.stream().filter(
                s -> s.getIdentifier().equals(trimSpacesAndRemoveLastDot(id)))
                .findFirst().orElse(null);
    }

    private List<Keyword> parseKeywordFileEntryList(List<KeyFileEntry> rawList) {
        return rawList.stream().map(this::parseKeywordFileEntry).collect(Collectors.toList());
    }

    private Keyword parseKeywordFileEntry(KeyFileEntry entry) {
        final String identifier = entry.id != null ? entry.id : entry.ic;
        final Keyword retObj = new Keyword(trimSpacesAndRemoveLastDot(identifier), entry.ac);

        // definition
        String def = String.join(" ", entry.de);
        retObj.setDefinition(def.isEmpty() ? null : def);

        // Synonyms
        List<String> synList =
                entry.sy.stream().flatMap(s -> Arrays.asList(s.split(";")).stream())
                        .map(this::trimSpacesAndRemoveLastDot)
                        .collect(Collectors.toList());
        retObj.setSynonyms(synList.isEmpty() ? null : synList);

        // GoMapping
        List<GeneOntology> goList = entry.go.stream().map(this::parseGeneOntology).collect(Collectors.toList());
        retObj.setGoMappings(goList.isEmpty() ? null : goList);

        // Sites
        retObj.setSites(entry.ww.isEmpty() ? null : entry.ww);

        return retObj;
    }

    private String trimSpacesAndRemoveLastDot(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        return str.endsWith(".") ? str.substring(0, str.length() - 1) : str;
    }

    private GeneOntology parseGeneOntology(String go) {
        String[] tokens = go.split(";");
        return new GeneOntology(tokens[0], tokens[1].trim());
    }

    private List<KeyFileEntry> convertLinesIntoInMemoryObjectList(List<String> lines) {
        // At the time of writing code there was 1200 entries in file
        List<KeyFileEntry> retList = new ArrayList<>(1250);

        int i = 0;

        // Ignore the header lines and information
        for (; i < lines.size(); i++) {
            String lineIgnore = lines.get(i);
            if (lineIgnore.startsWith("______")) {
                break;
            }
        }

        // Ignore underscore ___ line
        i++;

        // reached entries lines
        KeyFileEntry entry = new KeyFileEntry();

        // create in memory list of objects
        while (i < lines.size()) {
            String line = lines.get(i);

            // For terminating line no need to complete loop
            if (line.equals("//")) {
                retList.add(entry);
                entry = new KeyFileEntry();
                i++;
                continue;
            }

            String[] tokens = line.split(SPLIT_SPACES);
            switch (tokens[0]) {
                case "ID":
                    entry.id = tokens[1];
                    break;
                case "IC":
                    entry.ic = tokens[1];
                    break;
                case "AC":
                    entry.ac = tokens[1];
                    break;
                case "DE":
                    entry.de.add(tokens[1]);
                    break;
                case "SY":
                    entry.sy.add(tokens[1]);
                    break;
                case "HI":
                    entry.hi.add(tokens[1]);
                    break;
                case "GO":
                    entry.go.add(tokens[1]);
                    break;
                case "CA":
                    entry.ca = tokens[1];
                    break;
                case "WW":
                    entry.ww.add(tokens[1]);
                    break;
                default:
                    LOG.info("Unhandle line found while parsing file: {}", line);

            }

            // read and save next line
            i++;
        }
        return retList;
    }

    private class KeyFileEntry {
        String id;
        String ic;
        String ac;
        List<String> de;
        List<String> sy;
        List<String> hi;
        List<String> go;
        List<String> ww;
        String ca;

        KeyFileEntry() {
            de = new ArrayList<>();
            sy = new ArrayList<>();
            hi = new ArrayList<>();
            go = new ArrayList<>();
            ww = new ArrayList<>();
        }
    }
}


