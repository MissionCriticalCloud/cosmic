//

//

package com.cloud.agent.resource.virtualnetwork;

public class FileConfigItem extends ConfigItem {
    private String filePath;
    private String fileName;
    private String fileContents;

    public FileConfigItem(final String filePath, final String fileName, final String fileContents) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileContents = fileContents;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileContents() {
        return fileContents;
    }

    public void setFileContents(final String fileContents) {
        this.fileContents = fileContents;
    }

    @Override
    public String getAggregateCommand() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<file>\n");
        sb.append(filePath);

        // Don't use File.pathSeparator here as the target is the unix based systemvm
        if (!filePath.endsWith("/")) {
            sb.append('/');
        }

        sb.append(fileName);
        sb.append('\n');
        sb.append(fileContents);
        sb.append("\n</file>\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FileConfigItem, copying ");
        sb.append(fileContents.length());
        sb.append(" characters to ");
        sb.append(fileName);
        return sb.toString();
    }
}
