package com.example.farm4u.repository;

import com.example.farm4u.dto.admin.DashboardDto;

import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository {
    List<DashboardDto> fetchDashboardData(LocalDate startDate, LocalDate endDate);
}