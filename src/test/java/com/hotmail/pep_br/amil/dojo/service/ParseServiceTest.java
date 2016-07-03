package com.hotmail.pep_br.amil.dojo.service;

import com.hotmail.pep_br.amil.dojo.entity.Match;
import com.hotmail.pep_br.amil.dojo.entity.Player;
import com.hotmail.pep_br.amil.dojo.enums.LogLineType;
import com.hotmail.pep_br.amil.dojo.enums.TrophyTypeEnum;
import com.hotmail.pep_br.amil.dojo.exception.MatchParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParseServiceTest {

    private final static String NEW_MATCH = "23/04/2013 15:34:22 - New match 11348965 has started";
    private final static String KILL1 = "23/04/2013 15:36:04 - Roman killed Nick using M16";
    private final static String KILL2 = "23/04/2013 15:36:33 - <WORLD> killed Nick by DROWN";
    private final static String END_OF_MATCH = "23/04/2013 15:39:22 - Match 11348965 has ended";

    private ParseService parseService;

    @Before
    public void init() {
        parseService = new ParseService();
    }

    @Test
    public void newMatchTestSuccess() throws Exception {
        assertTrue("Line should be of type NEW_MATCH", parseService.typeOf(NEW_MATCH) == LogLineType.NEW_MATCH);
    }

    @Test
    public void killVariation1TestSuccess() throws Exception {
        assertTrue("Line should be of type KILL", parseService.typeOf(KILL1) == LogLineType.KILL);
    }

    @Test
    public void killVariation2TestSuccess() throws Exception {
        assertTrue("Line should be of type KILL", parseService.typeOf(KILL2) == LogLineType.KILL);
    }

    @Test
    public void endOfMatchTestSuccess() throws Exception {
        assertTrue("Line should be of type END_OF_MATCH", parseService.typeOf(END_OF_MATCH) == LogLineType.END_OF_MATCH);
    }

    @Test(expected = MatchParseException.class)
    public void newMatchTestFail() throws Exception {
        parseService.typeOf("This one should fail");
    }

    @Test
    public void newMatchGetMatchTestSuccess() throws Exception {
        parseService.parseNewMatch(NEW_MATCH);
        parseService.parseEndOfMatch(END_OF_MATCH);
        Match match = parseService.getMatch();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        assertTrue("Match start date should be 23/04/2013 15:34:22", "23/04/2013 15:34:22".equals(sdf.format(match.getStartDate().getTime())));
        assertTrue("Match end date should be 23/04/2013 15:39:22", "23/04/2013 15:39:22".equals(sdf.format(match.getEndDate().getTime())));
    }

    @Test(expected = MatchParseException.class)
    public void newMatchGetMatchTestFail() throws Exception {
        parseService.parseNewMatch(KILL2);
    }


    @Test
    public void killGetMatchTestSuccess() throws Exception {
        parseService.parseKill(KILL1);
        parseService.parseKill(KILL2);

        Match match = parseService.getMatch();
        assertTrue("Match must contain 2 players", match.getPlayers().size() == 2);

        Player roman = match.getPlayers().get("Roman");
        Player nick = match.getPlayers().get("Nick");
        assertNotNull("One player should be Roman", roman);
        assertNotNull("One player should be Nick", nick);

        assertTrue("Roman must have 1 kill", roman.getKills() == 1);
        assertTrue("Roman must have 0 deaths", roman.getDeaths() == 0);
        assertTrue("Nick must have 0 kill", nick.getKills() == 0);
        assertTrue("Nick must have 2 deaths", nick.getDeaths() == 2);

        assertTrue("Roman must have 1 gun", roman.getGunKills().size() == 1);
        assertTrue("Roman must have 1 kill with gun M16", roman.getGunKills().get("M16") == 1);

    }

    @Test
    public void immortalTrophyGetMatchTestSuccess() throws Exception {
        File file = new File (this.getClass().getClassLoader().getResource("Immortal.txt").getFile());
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            parseService.parseKill(line);
        }
        parseService.computeGameData();

        Match match = parseService.getMatch();
        assertTrue("Match must contain 3 players", match.getPlayers().size() == 3);

        Player roman = match.getPlayers().get("Roman");
        Player nick = match.getPlayers().get("Nick");
        Player jack = match.getPlayers().get("Jack");
        assertNotNull("One player should be Roman", roman);
        assertNotNull("One player should be Nick", nick);
        assertNotNull("One player should be Jack", jack);

        assertTrue("Roman must have 5 kills", roman.getKills() == 5);
        assertTrue("Roman must have 0 deaths", roman.getDeaths() == 0);
        assertTrue("Roman must have best streak of 5", roman.getBestStreak() == 5);
        assertTrue("Nick must have 0 kill", nick.getKills() == 0);
        assertTrue("Nick must have 3 deaths", nick.getDeaths() == 3);
        assertTrue("Nick must have best streak of 0", nick.getBestStreak() == 0);
        assertTrue("Jack must have 0 kill", jack.getKills() == 0);
        assertTrue("Jack must have 2 deaths", jack.getDeaths() == 2);
        assertTrue("Jack must have best streak of 0", jack.getBestStreak() == 0);

        assertTrue("Roman must have 1 gun", roman.getGunKills().size() == 1);
        assertTrue("Roman must have 5 kills with gun M16", roman.getGunKills().get("M16") == 5);

        assertTrue("Roman must have 2 Trophies", roman.getTrophies().size() == 2);
        assertTrue("Roman Trophy must be Immortal", roman.getTrophies().get(TrophyTypeEnum.IMMORTAL) == 1);
        assertTrue("Roman Trophy must be Fast Killer", roman.getTrophies().get(TrophyTypeEnum.FAST_KILLER) == 1);

    }

    @Test
    public void fastKillerAndTheWalkingDeadTrophyGetMatchTestSuccess() throws Exception {
        File file = new File (this.getClass().getClassLoader().getResource("FastKiller2TheWalkingDead2.txt").getFile());
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            parseService.parseKill(line);
        }
        parseService.computeGameData();

        Match match = parseService.getMatch();
        assertTrue("Match must contain 3 players", match.getPlayers().size() == 3);

        Player roman = match.getPlayers().get("Roman");
        Player nick = match.getPlayers().get("Nick");
        Player jack = match.getPlayers().get("Jack");
        assertNotNull("One player should be Roman", roman);
        assertNotNull("One player should be Nick", nick);
        assertNotNull("One player should be Jack", jack);

        assertTrue("Roman must have 10 kills", roman.getKills() == 10);
        assertTrue("Roman must have 5 deaths", roman.getDeaths() == 5);
        assertTrue("Roman must have best streak of 5", roman.getBestStreak() == 5);
        assertTrue("Nick must have 1 kill", nick.getKills() == 1);
        assertTrue("Nick must have 7 deaths", nick.getDeaths() == 7);
        assertTrue("Nick must have best streak of 1", nick.getBestStreak() == 1);
        assertTrue("Jack must have 2 kill", jack.getKills() == 2);
        assertTrue("Jack must have 5 deaths", jack.getDeaths() == 5);
        assertTrue("Jack must have best streak of 2", jack.getBestStreak() == 2);

        assertTrue("Roman must have 2 guns", roman.getGunKills().size() == 2);
        assertTrue("Roman must have 9 kills with gun M16", roman.getGunKills().get("M16") == 9);
        assertTrue("Roman must have 1 kill with gun MAGNUM 765", roman.getGunKills().get("MAGNUM 765") == 1);

        assertTrue("Roman must have 2 Trophies", roman.getTrophies().size() == 2);
        assertTrue("Roman Trophy must be Fast Killer", roman.getTrophies().get(TrophyTypeEnum.FAST_KILLER) == 2);
        assertTrue("Roman Trophy must be The Walking Dead", roman.getTrophies().get(TrophyTypeEnum.THE_WALKING_DEAD) == 1);

        assertTrue("Nick must have 1 Trophy", nick.getTrophies().size() == 1);
        assertTrue("Nick Trophy must be The Walking Dead", nick.getTrophies().get(TrophyTypeEnum.THE_WALKING_DEAD) == 1);

        assertTrue("Jack must have 1 Trophy", jack.getTrophies().size() == 1);
        assertTrue("Jack Trophy must be The Walking Dead", jack.getTrophies().get(TrophyTypeEnum.THE_WALKING_DEAD) == 1);

        parseService.displayGameResults();
    }
}
