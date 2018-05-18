package uk.ac.ebi.uniprot.uniprotkeyword.dto;

import java.util.Objects;

public class KeywordReferenceCount {
    private final String accession;
    private Long swissProtCount;
    private Long tremblCount;

    public KeywordReferenceCount(String accession) {
        this.accession = accession;
        swissProtCount = 0L;
        tremblCount = 0L;
    }

    public String getAccession() {
        return accession;
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
        if (!(o instanceof KeywordReferenceCount)) {
            return false;
        }
        KeywordReferenceCount that = (KeywordReferenceCount) o;
        return Objects.equals(accession, that.accession);
    }

    @Override public int hashCode() {

        return Objects.hash(accession);
    }
}
