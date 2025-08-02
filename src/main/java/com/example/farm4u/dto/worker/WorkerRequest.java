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
    private String gender;
    private String birth;
    private String address;
    private String activeArea;
    private String workType;
    private Set<String> workDays;
    private Boolean hasFarmExp;
    private Set<String> farmExpTypes;
    private Set<String> farmExpTasks;
    private String workIntensity;
}