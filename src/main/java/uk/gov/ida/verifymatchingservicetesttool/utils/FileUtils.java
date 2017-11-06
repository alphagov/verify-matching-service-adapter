package uk.gov.ida.verifymatchingservicetesttool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

public class FileUtils {

    public String readFromResources(String fileName) {
        InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(fileName);

        return readFrom(inputStream);
    }

    public String read(File file) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);
        return readFrom(inputStream);
    }

    private String readFrom(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines()
            .collect(joining(lineSeparator()));
    }
}
