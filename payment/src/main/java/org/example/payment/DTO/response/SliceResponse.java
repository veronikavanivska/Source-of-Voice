package org.example.payment.DTO.response;

import lombok.Data;

import java.util.List;

@Data
public class SliceResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private int numberOfElements;

    public static <T> SliceResponse<T> of(List<T> fetchedItems, int page, int size) {
        boolean hasNext = fetchedItems.size() > size;

        List<T> content = hasNext
                ? fetchedItems.subList(0, size)
                : fetchedItems;

        SliceResponse<T> response = new SliceResponse<>();

        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setFirst(page == 0);
        response.setLast(!hasNext);
        response.setHasNext(hasNext);
        response.setNumberOfElements(content.size());

        return response;
    }
}
