package com.hotmail.pep_br.amil.dojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Match {

    private Long id;
    private Calendar startDate;
    private Calendar endDate;
    @JsonIgnore
    private Map<String, Player> players = new HashMap<>();

}
