package com.hotmail.pep_br.amil.dojo;

import com.hotmail.pep_br.amil.dojo.service.ParseService;

import java.io.File;
import java.io.FileNotFoundException;

public class Application {

    public static void main (String[] args) throws Exception {

//        Perform validations before instantiating the parser
        if (args.length == 0) {
            System.out.println("Invalid argument: 1st argument should be the path and file name to be imported.");
            throw new IllegalArgumentException("Invalid argument: 1st argument should be the path and file name to be imported.");
        }

        File file = new File(args[0]);
        if (! file.exists()) {
            System.out.println("File " + args[0] + " not found.");
            throw new FileNotFoundException();
        }
        if (file.isDirectory()) {
            System.out.println("File " + args[0] + " appears to be a directory.");
            throw new FileNotFoundException();
        }

        ParseService parseService = new ParseService(file);
        parseService.doParse();
    }

}
