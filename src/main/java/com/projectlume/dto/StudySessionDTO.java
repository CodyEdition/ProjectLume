package com.projectlume.dto;

import java.time.LocalDateTime;

public class StudySessionDTO {
    private final LocalDateTime date;
    private final String name;
    private final int studyCount;
    private final String formattedAccuracy;

    public StudySessionDTO(LocalDateTime date, String name, int studyCount, String formattedAccuracy) {
        this.date = date;
        this.name = name;
        this.studyCount = studyCount;
        this.formattedAccuracy = formattedAccuracy;
    }

    public LocalDateTime getDate() {
        return date;
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