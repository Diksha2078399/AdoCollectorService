package com.ADO.ADOPersonal.Service;



import com.ADO.ADOPersonal.db.repo.SchedulerInfoRepository;
import com.ADO.ADOPersonal.metadata.SchedulerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchedulerInfoService {

    @Autowired
    SchedulerInfoRepository schedulerRepository;

    public SchedulerInfo save(String toolName, String jiraProjectKey, String projectId, String currentTime, String status) {


        Optional<SchedulerInfo> schedulerInfo = schedulerRepository.findFirstByToolNameAndJiraProjectKey(toolName, jiraProjectKey);
        if (schedulerInfo.isPresent()) {

            SchedulerInfo schedulerInfoExistingObj = schedulerInfo.get();
            schedulerInfoExistingObj.setLastUpdatedDate(currentTime);
            schedulerInfoExistingObj.setStatus(status);
            return schedulerRepository.save(schedulerInfoExistingObj);
        } else {
            SchedulerInfo schedulerInfoNewObj = new SchedulerInfo();
            schedulerInfoNewObj.setToolName(toolName);
            schedulerInfoNewObj.setDesc(toolName + " scheduler info");
            schedulerInfoNewObj.setLastUpdatedDate(currentTime);
            schedulerInfoNewObj.setProjectId(projectId);
            schedulerInfoNewObj.setJiraProjectKey(jiraProjectKey);
            schedulerInfoNewObj.setStatus(status);

            return schedulerRepository.save(schedulerInfoNewObj);
        }
    }

    public List<SchedulerInfo> getAllSchedulerInfo(String jiraProjectKey) {
        return schedulerRepository.findByJiraProjectKey(jiraProjectKey);
    }
}

