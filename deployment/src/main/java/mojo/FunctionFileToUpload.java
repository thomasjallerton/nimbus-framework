package mojo;

import java.util.Objects;

public class FunctionFileToUpload {

    private final String sourceFilePath;
    private final String targetFilePath;

    public FunctionFileToUpload(String sourceFilePath, String targetFilePath) {
        this.sourceFilePath = sourceFilePath;
        this.targetFilePath = targetFilePath;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionFileToUpload)) return false;
        FunctionFileToUpload that = (FunctionFileToUpload) o;
        return Objects.equals(sourceFilePath, that.sourceFilePath) && Objects.equals(targetFilePath, that.targetFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFilePath, targetFilePath);
    }
}
