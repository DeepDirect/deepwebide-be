package com.deepdirect.deepwebide_be.repository.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@RequiredArgsConstructor
public class AutoStopSchedulerService {
    private final RepositoryRunService repositoryRunService;
    private final TaskScheduler taskScheduler;

    // 10분 후 자동 중지 예약
    public void scheduleAutoStop(Long repositoryId, String uuid, int minutes) {
        taskScheduler.schedule(
                () -> repositoryRunService.stopIfTimeout(repositoryId, uuid),
                new Date(System.currentTimeMillis() + minutes * 60 * 1000L)
        );
    }
}
