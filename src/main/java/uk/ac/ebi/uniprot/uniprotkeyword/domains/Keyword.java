package uk.ac.ebi.uniprot.uniprotkeyword.domains;

import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;

public class Keyword {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Index(unique = true)
    private String identifier;

    @NotNull
    @Index
    private String accession;

    private String definition;
    private List<String> synonyms;
    private List<GeneOntology> goMappings;
    private List<Keyword> hierarchy;
    private List<String> sites;
    private Keyword category;

    private Long swissProtCount;
    private Long tremblCount;

    /**
     * No argument constructor is mandatory for OGM (Object Graph Model) to construct object. Making it
     * private because according to business knowledge keyword must have identifier and accession
     */
    private Keyword() {
    }

    public Keyword(@NotNull String identifier, @NotNull String accession) {
        this.identifier = identifier;
        this.accession = accession;
        swissProtCount = 0L;
        tremblCount = 0L;
    }

    public boolean isCategoryMarker() {
        return category == null;
    }

    public Long getUniprotkbCount() {
        return swissProtCount + tremblCount;
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAccession() {
        return accession;
    }

    public String getDefinition() {
        return definition;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public List<GeneOntology> getGoMappings() {
        return goMappings;
    }

    public List<Keyword> getHierarchy() {
        return hierarchy;
    }

    public List<String> getSites() {
        return sites;
    }

    public Keyword getCategory() {
        return category;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setGoMappings(List<GeneOntology> goMappings) {
        this.goMappings = goMappings;
    }

    public void setHierarchy(List<Keyword> hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    public void setCategory(Keyword category) {
        this.category = category;
    }

    public Long getSwissProtCount() {
        return swissProtCount;
    }

    public void setSwissProtCount(Long swissProtCount) {
        this.swissProtCount = swissProtCount;
    }

    public Long getTremblCount() {
        return tremblCount;
    }

    public void setTremblCount(Long tremblCount) {
        this.tremblCount = tremblCount;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Keyword)) {
            return false;
        }
        Keyword keyword = (Keyword) o;
        return Objects.equals(identifier, keyword.identifier) &&
                Objects.equals(accession, keyword.accession);
    }

    @Override public int hashCode() {

        return Objects.hash(identifier, accession);
    }
}
