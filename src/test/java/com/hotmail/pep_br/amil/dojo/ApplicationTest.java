package com.hotmail.pep_br.amil.dojo;


import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class ApplicationTest {

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest() throws Exception {
        Application.main(new String[]{});
    }

    @Test(expected = FileNotFoundException.class)
    public void fileNotFoundExceptionTest() throws Exception {
        Application.main(new String[]{"this/file/doesnt/exist"});
    }

    @Test
    public void validationsPassed() throws Exception {
        File file = new File (this.getClass().getClassLoader().getResource("basicMatch.txt").getFile());
        Application.main(new String[]{file.getAbsolutePath()});
    }
}
