package com.disqueprogrammer.app.trackerfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class ItemFilter {
    private String name;
    private boolean selected;

    public ItemFilter() {
        this.selected = false;
    }
}
