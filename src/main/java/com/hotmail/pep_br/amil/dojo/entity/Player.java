package com.hotmail.pep_br.amil.dojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hotmail.pep_br.amil.dojo.enums.TrophyTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Player {

    @NonNull
    private String name;
    private Integer kills = 0;
    private Integer deaths = 0;
    private Double kDRatio = 0d;
    private Integer bestStreak = 0;
    private Map<String, Integer> gunKills = new HashMap<>();
    private Map<TrophyTypeEnum, Integer> trophies = new HashMap<>();

    @JsonIgnore
    private List<Activity> userActivity = new ArrayList<>();

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addGunKill(String gunName) {

        Integer kills = gunKills.get(gunName);
        if (kills == null) {
            kills = Integer.valueOf(0);
        }
        kills++;
        gunKills.put(gunName, kills);
    }

    public void addTrophy(TrophyTypeEnum type) {
        Integer kills = trophies.get(type);
        if (kills == null) {
            kills = Integer.valueOf(0);
        }
        kills++;
        trophies.put(type, kills);
    }

    public void addActivity(Activity activity) {
        userActivity.add(activity);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPreferredGun() {
        if (gunKills.size() == 0) {
            return null;
        }
        Comparator<Map.Entry<String, Integer>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue());
        return gunKills.entrySet()
                .stream()
                .sorted(byValue.reversed())
                .findFirst().get().getKey();
    }
}
