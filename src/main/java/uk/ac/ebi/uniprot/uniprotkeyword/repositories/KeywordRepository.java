package uk.ac.ebi.uniprot.uniprotkeyword.repositories;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;

import java.util.Collection;
import java.util.List;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends Neo4jRepository<Keyword, Long> {

    // we can use here @depth(-1) but this is more optimised
    @Query("MATCH (n:Keyword{accession:{0}}) WITH n MATCH p=(n)-[*0..]->() RETURN p")
    List<Keyword> findByAccession(String accession);

    @Query("MATCH (n:Keyword) where LOWER(n.identifier) = LOWER({identifier}) WITH n MATCH p=(n)-[*0..]->() RETURN p")
    List<Keyword> findByIdentifier(@Param("identifier") String identifier);

    Collection<Keyword> findByIdentifierIgnoreCaseLike(@Param("identifier") String identifier);

    Collection<Keyword>
    findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrSynonymsIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
            String identifier, String accession, String synonyms, String definition);
}
