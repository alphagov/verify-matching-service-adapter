package uk.gov.ida.verifymatchingservicetesttool.utils;

import uk.gov.ida.verifymatchingservicetesttool.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DefaultFilesLocator implements FilesLocator {

    private String examplesFolderName;

    public DefaultFilesLocator(String examplesFolderName) {
        this.examplesFolderName = examplesFolderName;
    }

    public Stream<File> getFiles(FolderName folderName) {
        try {
            return Files.list(Paths.get(getFolder(folderName.getValue()).toURI()))
                .map(item -> new File(item.toUri()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFolder(String folderName) {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(getExamplesFolderLocation(path) + File.separator + folderName);
    }

    private String getExamplesFolderLocation(String path) {
        return new File(path)
            .getParentFile()
            .getParentFile()
            .getAbsolutePath() + File.separator + examplesFolderName;
    }
}
