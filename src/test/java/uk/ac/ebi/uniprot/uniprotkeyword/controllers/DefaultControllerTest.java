package uk.ac.ebi.uniprot.uniprotkeyword.controllers;

import uk.ac.ebi.uniprot.uniprotkeyword.domains.Keyword;
import uk.ac.ebi.uniprot.uniprotkeyword.services.KeywordService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DefaultController.class)
@ExtendWith(SpringExtension.class)
class DefaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeywordService keywordService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Keyword l = new Keyword("Developmental protein", "KW-0217");
        final Keyword o = new Keyword("Developmental stage", "KW-9996");
        final Keyword t = new Keyword("Diabetes insipidus", "KW-0218");

        given(keywordService.findByAccession("KW-0217")).willReturn(l);

        given(keywordService.findByIdentifier("Developmental stage")).willReturn(o);

        given(keywordService.findByIdentifierIgnoreCaseLike("singleWord"))
                .willReturn(Arrays.asList(l, o, t));

        given(keywordService.findAllByKeyWordSearch("any string OR any"))
                .willReturn(Arrays.asList(l, o, t));
    }

    @Test
    void testAccessionEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/accession/{accession}", "KW-0217"))
                .andExpect(status().isOk())
                .andReturn();

        Keyword response = mapper.readValue(rawRes.getResponse().getContentAsString(), Keyword.class);
        assertThat(response.getAccession()).isEqualTo("KW-0217");
        assertThat(response.getIdentifier()).isEqualTo("Developmental protein");
    }

    @Test
    void testIdentifierEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/identifier/{identifier}", "Developmental stage"))
                .andExpect(status().isOk())
                .andReturn();

        Keyword response = mapper.readValue(rawRes.getResponse().getContentAsString(), Keyword.class);
        assertThat(response.getIdentifier()).isEqualTo("Developmental stage");
        assertThat(response.getCategory()).isNull();
    }

    @Test
    void testIdentifierAllEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/identifier/all/{singleWord}", "singleWord"))
                .andExpect(status().isOk())
                .andReturn();

        List<Keyword> retList = mapper.readValue(rawRes.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, Keyword.class));
        assertThat(retList.size()).isEqualTo(3);
        List<String> accessions = Arrays.asList("KW-0217", "KW-9996", "KW-0218");
        assertThat(retList.get(0).getAccession()).isIn(accessions);
        assertThat(retList.get(1).getAccession()).isIn(accessions);
        assertThat(retList.get(2).getAccession()).isIn(accessions);
    }

    @Test
    public void testSearchEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/search/{wordSeperatedBySpace}", "any string OR any"))
                .andExpect(status().isOk())
                .andReturn();

        List<Keyword> retList = mapper.readValue(rawRes.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, Keyword.class));

        assertThat(retList.size()).isEqualTo(3);
    }

}
