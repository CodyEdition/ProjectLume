package com.projectlume.builder;

import com.projectlume.dto.StudySessionDTO;

/**
 * Builder for StudySessionDTO implementing Builder pattern
 * Provides fluent interface for constructing StudySessionDTO objects
 */
public class StudySessionDTOBuilder {
    private String formattedDate;
    private String name;
    private int studyCount;
    private String formattedAccuracy;
    
    public StudySessionDTOBuilder formattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
        return this;
    }
    
    public StudySessionDTOBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public StudySessionDTOBuilder studyCount(int studyCount) {
        this.studyCount = studyCount;
        return this;
    }
    
    public StudySessionDTOBuilder formattedAccuracy(String formattedAccuracy) {
        this.formattedAccuracy = formattedAccuracy;
        return this;
    }
    
    /**
     * Build the StudySessionDTO object
     * @return Constructed StudySessionDTO
     */
    public StudySessionDTO build() {
        return new StudySessionDTO(
            formattedDate,
            name,
            studyCount,
            formattedAccuracy
        );
    }
    
    /**
     * Create a new builder instance
     */
    public static StudySessionDTOBuilder builder() {
        return new StudySessionDTOBuilder();
    }
}

