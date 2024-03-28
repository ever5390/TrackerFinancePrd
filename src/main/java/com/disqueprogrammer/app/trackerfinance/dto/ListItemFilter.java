package com.disqueprogrammer.app.trackerfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
public class ListItemFilter {
    private OrderFilterEnum orderFilter;
    private List<ItemFilter> items;

    public ListItemFilter() {
        this.orderFilter = OrderFilterEnum.INCLUDE;
    }
}
