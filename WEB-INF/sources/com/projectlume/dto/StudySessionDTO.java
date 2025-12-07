package com.projectlume.dto;

public class StudySessionDTO {
    private final String formattedDate;
    private final String name;
    private final int studyCount;
    private final String formattedAccuracy;

    public StudySessionDTO(String formattedDate, String name, int studyCount, String formattedAccuracy) {
        this.formattedDate = formattedDate;
        this.name = name;
        this.studyCount = studyCount;
        this.formattedAccuracy = formattedAccuracy;
    }

    public String getDate() {
        return formattedDate;
    }

    public String getName() {
        return name;
    }

    public int getStudyCount() {
        return studyCount;
    }

    public String getFormattedAccuracy() {
        return formattedAccuracy;
    }

}