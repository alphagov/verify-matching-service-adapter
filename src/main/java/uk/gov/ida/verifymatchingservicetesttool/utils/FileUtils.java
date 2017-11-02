package uk.gov.ida.verifymatchingservicetesttool.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class FileUtils {

    public String readFromResources(String fileName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(fileName)))
                .lines().collect(Collectors.joining("\n"));
    }
}
