package uk.gov.ida.verifymatchingservicetesttool.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ScenarioFilesLocator implements FilesLocator {

    private final String datasetType;
    private final String customExamplesFolderLocation;

    public ScenarioFilesLocator() {
        this("legacy", null);
    }

    public ScenarioFilesLocator(String datasetType, String customExamplesFolderLocation) {
        this.datasetType = datasetType;
        this.customExamplesFolderLocation = customExamplesFolderLocation;
    }

    @Override
    public Stream<File> getFiles(FolderName folderName) {
        try {
            return Files.list(Paths.get(getScenariosFolder(folderName).toURI()))
                    .map(item -> new File(item.toUri()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getScenariosFolder(FolderName folderName) {
        return Paths.get(getExamplesFolderLocation(), folderName.getValue()).toFile();
    }

    private String getExamplesFolderLocation() {
        return customExamplesFolderLocation;
    }
}
