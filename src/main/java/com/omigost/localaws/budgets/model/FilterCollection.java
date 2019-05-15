package com.omigost.localaws.budgets.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Entity(name = "filter_collection_entry")
@Builder
@AllArgsConstructor
public class FilterCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column
    @ElementCollection
    List<String> values;

    @Transient
    @Getter
    private String key;

    public FilterCollection() {
        values = new ArrayList<>();
        key = null;
    }

}
