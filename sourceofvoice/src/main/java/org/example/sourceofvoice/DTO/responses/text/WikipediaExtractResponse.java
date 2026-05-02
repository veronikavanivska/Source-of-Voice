package org.example.sourceofvoice.DTO.responses.text;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikipediaExtractResponse {

    private Query query;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {
        private Map<String, PageExtract> pages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageExtract {
        private Long pageid;
        private Integer ns;
        private String title;
        private String extract;
    }
}
