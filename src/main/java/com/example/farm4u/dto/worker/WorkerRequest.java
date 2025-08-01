package com.example.farm4u.dto.worker;

import com.example.farm4u.entity.Worker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class WorkerRequest {
    private String name;
    private Worker.Gender gender;
    private Date birth;
    private String address;
    private String activeArea;
    private Worker.WorkType workType;
    private Set<Worker.WorkDay> workDays;
    private Boolean hasFarmExp;
    private Set<Worker.FarmExpType> farmExpTypes;
    private Set<Worker.FarmExpTask> farmExpTasks;
    private Worker.WorkIntensity workIntensity;
}