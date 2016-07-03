package com.hotmail.pep_br.amil.dojo.entity;

import com.hotmail.pep_br.amil.dojo.enums.ActivityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Calendar;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Activity {

    @NonNull
    private Calendar date;

    @NonNull
    private ActivityType type;
}
