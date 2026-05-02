package org.example.sourceofvoice.DTO.responses.text;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikipediaRandomResponse {

    private Query query;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {
        private List<RandomPage> random;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RandomPage {
        private Long id;
        private Integer ns;
        private String title;
    }
}
