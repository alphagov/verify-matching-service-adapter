package common.uk.gov.ida.verifymatchingservicetesttool.utils;

import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FolderName;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestsFilesLocator implements FilesLocator {

    private final static String JSON_FILES_FOLDER_NAME = "json";

    private final Map<FolderName, List<String>> testFileNames;

    public TestsFilesLocator(Map<FolderName, List<String>> testFileNames) {
        this.testFileNames = testFileNames;
    }

    @Override
    public Stream<File> getFiles(FolderName folderName) {
        return testFileNames.get(folderName).stream()
            .map(fileName -> getFile(fileName));
    }

    private File getFile(String fileName) {
        try {
            URI uri = this.getClass()
                .getClassLoader()
                .getResource(JSON_FILES_FOLDER_NAME + File.separator + fileName)
                .toURI();
            return new File(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
