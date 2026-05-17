
package com.yancy.yharness.context;

import java.util.ArrayList;
import java.util.List;

public class LongTermMemory {
    
    private List<MemoryItem> items = new ArrayList<>();

    public LongTermMemory() {
    }

    public List<MemoryItem> getItems() {
        return items;
    }

    public void setItems(List<MemoryItem> items) {
        this.items = items;
    }

    public void addItem(MemoryItem item) {
        this.items.add(item);
    }

    public void clear() {
        this.items.clear();
    }

    public static class MemoryItem {
        private String key;
        private String value;
        private String category;

        public MemoryItem() {
        }

        public MemoryItem(String key, String value, String category) {
            this.key = key;
            this.value = value;
            this.category = category;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
