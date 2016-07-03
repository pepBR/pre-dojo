package com.hotmail.pep_br.amil.dojo.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hotmail.pep_br.amil.dojo.entity.Activity;
import com.hotmail.pep_br.amil.dojo.entity.Match;
import com.hotmail.pep_br.amil.dojo.entity.Player;
import com.hotmail.pep_br.amil.dojo.enums.ActivityType;
import com.hotmail.pep_br.amil.dojo.enums.LogLineType;
import com.hotmail.pep_br.amil.dojo.enums.TrophyTypeEnum;
import com.hotmail.pep_br.amil.dojo.exception.MatchParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseService {

    private final String NEW_MATCH = "^(\\d{2}\\/\\d{2}\\/\\d{2,4})\\s([0-2]?\\d\\:\\d{0,2}:\\d{0,2})\\s-\\sNew match (\\d+) has started$";
    private final String END_OF_MATCH = "^(\\d{2}\\/\\d{2}\\/\\d{2,4})\\s([0-2]?\\d\\:\\d{0,2}:\\d{0,2})\\s-\\sMatch (\\d+) has ended$";
    private final String KILL = "^(\\d{2}\\/\\d{2}\\/\\d{2,4})\\s([0-2]?\\d\\:\\d{0,2}:\\d{0,2})\\s-\\s(.+?)\\skilled\\s(.+?)\\s(using|by)\\s(.+?)$";
    private final String WORLD = "<WORLD>";

    private File file;
    private Match match = new Match();

    protected ParseService() { }

    public ParseService(File file) {
        this.file = file;
    }

    public Match getMatch() {
        return match;
    }

    public void doParse() throws FileNotFoundException, ParseException, MatchParseException {
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();

            switch (typeOf(line)) {
                case NEW_MATCH:
                    parseNewMatch(line);
                    break;
                case END_OF_MATCH:
                    parseEndOfMatch(line);
                    break;
                case KILL:
                    parseKill(line);
                    break;
            }
        }
        computeGameData();
        displayGameResults();
    }

    protected void displayGameResults() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Match.class, new StringPlayerMapSerializer());
        mapper.registerModule(module);
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(match));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println(match);
        }
    }

    protected void computeGameData() {
        Iterator<Player> players = match.getPlayers().values().iterator();
        while (players.hasNext()) {
            Player player = players.next();
            Calendar startKillStreak = null;
            int killStreak = 0;
            int deathStreak = 0;
            if (player.getDeaths() == 0){
                player.addTrophy(TrophyTypeEnum.IMMORTAL);
            }

            for (Activity activity : player.getUserActivity()) {
                if (activity.getType() == ActivityType.DIED) {
                    if (killStreak > player.getBestStreak()) {
                        player.setBestStreak(killStreak);
                    }
                    startKillStreak = null;
                    killStreak = 0;
                    deathStreak++;
                    if (deathStreak == 5) {
                        player.addTrophy(TrophyTypeEnum.THE_WALKING_DEAD);
                    }
                } else if (activity.getType() == ActivityType.KILLED) {
                    if (startKillStreak == null) {
                        startKillStreak = Calendar.getInstance();
                    }
                    killStreak++;
                    deathStreak = 0;
                    if (killStreak == 5) {
                        player.addTrophy(TrophyTypeEnum.FAST_KILLER);
                    }
                }
            }
            if (killStreak > player.getBestStreak()) {
                player.setBestStreak(killStreak);
            }
            int deaths = player.getDeaths() == 0 ? 1 : player.getDeaths();
            player.setKDRatio(new BigDecimal(player.getKills()).divide(new BigDecimal(deaths), 2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
        }
    }

    protected void parseKill(String line) throws MatchParseException, ParseException {
        Pattern pattern = Pattern.compile(KILL);
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new MatchParseException("Error parsing kill");
        }

        Calendar time = parseTime(matcher.group(1) + " " + matcher.group(2));
        String killerName = matcher.group(3);
        String killedName = matcher.group(4);
        String gunName = ("by".equalsIgnoreCase(matcher.group(5)) ? null : matcher.group(6));

        if (!WORLD.equalsIgnoreCase(killerName)) {
            Player killer = match.getPlayers().get(killerName);
            if (killer == null) {
                killer = new Player(killerName);
            }
            killer.addKill();
            killer.addActivity(new Activity(time, ActivityType.KILLED));
            if (gunName != null) {
                killer.addGunKill(gunName);
            }
            match.getPlayers().put(killerName, killer);
        }

        Player killed = match.getPlayers().get(killedName);
        if (killed == null) {
            killed = new Player(killedName);
        }
        killed.addDeath();
        killed.addActivity(new Activity(time, ActivityType.DIED));
        match.getPlayers().put(killedName, killed);
    }


    protected void parseNewMatch(String line) throws ParseException, MatchParseException {
        Pattern pattern = Pattern.compile(NEW_MATCH);
        Matcher matcher = pattern.matcher(line);

        if (! matcher.find()) {
            throw new MatchParseException("Error parsing new match");
        }
        long matchId = Long.parseLong(matcher.group(3));
        Calendar startTime = parseTime(matcher.group(1) + " " + matcher.group(2));
        startTime.setTime(startTime.getTime());

        match.setId(matchId);
        match.setStartDate(startTime);
    }

    protected void parseEndOfMatch(String line) throws ParseException, MatchParseException {
        Pattern pattern = Pattern.compile(END_OF_MATCH);
        Matcher matcher = pattern.matcher(line);

        if (! matcher.find()) {
            throw new MatchParseException("Error parsing end of match");
        }

        long matchId = Long.parseLong(matcher.group(3));
        if (matchId != match.getId()) {
            throw new MatchParseException("Inconsistent match id");
        }

        Calendar endTime = parseTime(matcher.group(1) + " " + matcher.group(2));
        endTime.setTime(endTime.getTime());

        match.setEndDate(endTime);
    }

    private Calendar parseTime (String time) throws ParseException {
        Calendar startTime = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        startTime.setTime(sdf.parse(time));
        return startTime;
    }

    protected LogLineType typeOf(String line) throws MatchParseException {
        if (Pattern.matches(NEW_MATCH, line)){
            return LogLineType.NEW_MATCH;
        } else if (Pattern.matches(KILL, line)) {
            return LogLineType.KILL;
        } else if (Pattern.matches(END_OF_MATCH, line)){
            return LogLineType.END_OF_MATCH;
        }
        throw new MatchParseException("Cannot parse \"" + line + "\"");
    }

    private class StringPlayerMapSerializer extends JsonSerializer<Match> {
        @Override
        public void serialize(Match match, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("id", match.getId());
            jsonGenerator.writeStringField("startDate", match.getStartDate() == null ? "" : sdf.format(match.getStartDate().getTime()));
            jsonGenerator.writeStringField("endDate", match.getEndDate() == null ? "" : sdf.format(match.getEndDate().getTime()));
            jsonGenerator.writeObjectField("players", match.getPlayers().values());
            jsonGenerator.writeEndObject();
        }
    }
}
