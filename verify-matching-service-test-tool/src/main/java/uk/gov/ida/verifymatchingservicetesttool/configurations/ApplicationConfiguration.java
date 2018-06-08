package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;
import uk.gov.ida.verifymatchingservicetesttool.utils.Color;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FolderName;
import uk.gov.ida.verifymatchingservicetesttool.utils.ScenarioFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.validators.JsonValidator;

import java.io.File;
import java.net.URI;

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
    ) throws MsaTestingToolConfigException {
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

    private String getExamplesFolder(String examplesFolderLocation) throws MsaTestingToolConfigException {
        if (examplesFolderLocation != null) {
            File exampleScenariosFolderLocation = new ConfigurationReader().getAbsoluteFilePath(examplesFolderLocation);
            if (ifFileDoesNotExist(exampleScenariosFolderLocation, FolderName.MATCH_FOLDER_NAME) ||
                    ifFileDoesNotExist(exampleScenariosFolderLocation, FolderName.NO_MATCH_FOLDER_NAME)) {

                throw new MsaTestingToolConfigException(String.format(
                        "%sEnsure the specified example scenarios folder '%s' has 'match' and 'no-match' sub-folders%s",
                        Color.YELLOW,
                        exampleScenariosFolderLocation,
                        Color.NONE)
                );
            }
            return exampleScenariosFolderLocation.getAbsolutePath();
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

    private static boolean ifFileDoesNotExist(File fileName, FolderName folderName) {
        return !(new File(fileName, folderName.getValue()).exists());
    }
}
