package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.utils.Color;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FolderName;
import uk.gov.ida.verifymatchingservicetesttool.utils.ScenarioFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.validators.JsonValidator;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader.getAbsoluteFilePath;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationConfiguration {
    private final static String DEFAULT_EXAMPLES_FOLDER_NAME = "examples";

    private final LocalMatchingServiceConfiguration localMatchingService;
    private final FilesLocator filesLocator;
    private final JsonValidator jsonValidator;
    private final FileUtils fileUtils;

    @JsonCreator
    public ApplicationConfiguration(
        @JsonProperty("localMatchingService") LocalMatchingServiceConfiguration localMatchingService,
        @JsonProperty("examplesFolderLocation") String examplesFolderLocation
    ) {
        this.localMatchingService = localMatchingService;

        this.filesLocator = new ScenarioFilesLocator(getDatasetType(), getExamplesFolder(examplesFolderLocation));
        this.jsonValidator = new JsonValidator(getDatasetType());
        this.fileUtils = new FileUtils(getDatasetType());
    }

    public URI getLocalMatchingServiceMatchUrl() {
        return localMatchingService.getMatchUrl();
    }

    public URI getLocalMatchingServiceAccountCreationUrl() {
        return localMatchingService.getAccountCreationUrl();
    }

    public Boolean getLocalMatchingServiceUsesUniversalDataSet() {
        return localMatchingService.usesUniversalDataset();
    }

    public FilesLocator getFilesLocator() {
        return filesLocator;
    }

    public JsonValidator getJsonValidator() {
        return jsonValidator;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    private String getDatasetType() {
        return localMatchingService.usesUniversalDataset() ? "universal-dataset" : "legacy";
    }

    private String getExamplesFolder(String examplesFolderLocation) {
        if (examplesFolderLocation != null) {
            String exampleScenariosFolderLocation = getAbsoluteFilePath(examplesFolderLocation);
            if (exampleScenariosFolderLocation != null &&
                    checkIfFileDoesNotExist(exampleScenariosFolderLocation, FolderName.MATCH_FOLDER_NAME) &&
                    checkIfFileDoesNotExist(exampleScenariosFolderLocation, FolderName.NO_MATCH_FOLDER_NAME)) {
                System.out.println(String.format("%sTest Run Failed%s", Color.RED, Color.NONE));
                System.out.println(String.format("%sEnsure the specified example scenarios folder '%s' " +
                        "has 'match' and 'no-match' sub-folders%s", Color.YELLOW, exampleScenariosFolderLocation, Color.NONE));
                System.exit(2);
            }
            return exampleScenariosFolderLocation;
        }
        return getDefaultExamplesFolderLocation();
    }

    private String getDefaultExamplesFolderLocation() {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path)
                .getParentFile()
                .getParentFile()
                .getAbsolutePath() + File.separator + DEFAULT_EXAMPLES_FOLDER_NAME
                + File.separator + getDatasetType();
    }

    private static boolean checkIfFileDoesNotExist(String fileName, FolderName folderName) {
        return !Files.exists(Paths.get(fileName, folderName.getValue()));
    }
}
