package com.hotmail.pep_br.amil.dojo.helper;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DojoLogGenerator {

    private static final String WORLD = "<WORLD>";
    private static final String[] players = new String[] {"Jack", "Nicholas", "Tom", "Stuart", "Geoff", "Rich", "FuriaBasil", WORLD};
    private static final String[] guns = new String[] {"M16", "AK47", "L115", "MTS-225", "Honey Badger", "MTAR-X", ".44 Magnum"};
    private static final String[] worldKills = new String[] {"DROWN", "FALL", "SUICIDE", "BURN"};
    private static final String cannotDie = "FuriaBasil";
    private static final int matchDurationInMinutes = 15;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static final Calendar startOfMatch = Calendar.getInstance();

    public static void main (String[] args) throws Exception {
        new DojoLogGenerator().generate();
    }

    private void generate() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("").getFile() + "real-match-data.txt");
        long matchId = Calendar.getInstance().getTimeInMillis();

        Calendar endOfMatch = Calendar.getInstance();
        endOfMatch.add(Calendar.MINUTE, matchDurationInMinutes);
        System.out.println("Started new Match.");

        FileWriter fw = new FileWriter(file);
        fw.write(getLogText("New match " + matchId + " has started"));

        do {
            startOfMatch.add(Calendar.MILLISECOND, getRandomNumber(100, 3000));
            String killer = getRandomArrayItem(players);
            String killed = getRandomArrayItem(players);
            if (killed.equalsIgnoreCase(killer) || WORLD.equals(killed) || cannotDie.equalsIgnoreCase(killed)) {
                int loopCount = 0;
                int maxAttempts = 10;
                do {
                    if (WORLD.equals(killed)){
                        System.out.println("Killed player cannot be " + WORLD + "...  atempt " + loopCount);
                    } else if (cannotDie.equalsIgnoreCase(killed)) {
                        System.out.println("Player " + cannotDie + " cannot be killed...  atempt " + loopCount);
                    } else {
                        System.out.println("Killed player name conflict. Killer = " + killer + "   Killed = " + killed + "...  atempt " + loopCount);
                    }
                    Thread.sleep(100);
                    loopCount++;
                    killed = getRandomArrayItem(players);
                } while ((killed.equalsIgnoreCase(killer) || WORLD.equals(killed) || cannotDie.equalsIgnoreCase(killed)) && loopCount < maxAttempts);
                if (killed.equalsIgnoreCase(killer) || WORLD.equals(killed)) {
                    System.out.println("Could not resolve killed player name. Exiting match.");
                    break;
                }
            }

            String killStatement = WORLD.equals(killer) ? " by " : " using ";
            String killedBy = WORLD.equals(killer) ? getRandomArrayItem(worldKills) : getRandomArrayItem(guns);

            fw.write(getLogText(killer + " killed " + killed + killStatement + killedBy));
        } while (startOfMatch.before(endOfMatch));

        fw.write(getLogText("Match " + matchId + " has ended"));
        fw.close();

    }

    private String getRandomArrayItem(String[] array) {
        return array[(int) (Math.random() * (array.length - 0)) + 0];
    }

    private int getRandomNumber(int lower, int bigger) {
        return (int) (Math.random() * (bigger - lower)) + lower;
    }

    private String getLogText(String text) {
        return sdf.format(startOfMatch.getTime()) + " - " + text + "\n";
    }
}
