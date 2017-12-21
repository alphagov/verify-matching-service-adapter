package uk.gov.ida.verifymatchingservicetesttool.utils;

import uk.gov.ida.verifymatchingservicetesttool.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ScenarioFilesLocator implements FilesLocator {

    private final static String DEFAULT_EXAMPLES_FOLDER_NAME = "examples";
    private String customExamplesFolderLocation;

    public ScenarioFilesLocator(String customExamplesFolderLocation) {
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
        return new File(getCustomExamplesFolderLocation() + File.separator + folderName.getValue());
    }

    private String getCustomExamplesFolderLocation() {
        return customExamplesFolderLocation != null ? customExamplesFolderLocation : getDefaultExamplesFolderLocation();
    }

    private String getDefaultExamplesFolderLocation() {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path)
                .getParentFile()
                .getParentFile()
                .getAbsolutePath() + File.separator + DEFAULT_EXAMPLES_FOLDER_NAME;
    }
}
