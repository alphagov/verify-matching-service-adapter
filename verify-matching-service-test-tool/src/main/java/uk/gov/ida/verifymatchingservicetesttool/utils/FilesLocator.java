package uk.gov.ida.verifymatchingservicetesttool.utils;

import java.io.File;
import java.util.stream.Stream;

public interface FilesLocator {

    Stream<File> getFiles(FolderName folderName);
}
