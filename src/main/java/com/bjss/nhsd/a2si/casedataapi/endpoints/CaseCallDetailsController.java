package com.bjss.nhsd.a2si.casedataapi.endpoints;

import com.bjss.nhsd.a2si.casedataapi.domain.DispoGroupByTimePeriod;
import com.bjss.nhsd.a2si.casedataapi.exceptions.AuthenticationException;
import com.bjss.nhsd.a2si.casedataapi.services.IdtCaseDataService;
import com.bjss.nhsd.a2si.idt.casedata.IdtCaseCallDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class CaseCallDetailsController {


    private static final Logger logger = LoggerFactory.getLogger(CaseCallDetailsController.class);

    private static final String caseDataApiUsernameHttpHeaderName = "casedata.api.username";
    private static final String caseDataApiPasswordHttpHeaderName = "casedata.api.password";

    @Value("${casedata.api.username}")
    private String caseDataApiUsername;

    @Value("${casedata.api.password}")
    private String caseDataApiPassword;

    private IdtCaseDataService idtCaseDataService;

    @Autowired
    public CaseCallDetailsController(IdtCaseDataService idtCaseDataService) {
        this.idtCaseDataService = idtCaseDataService;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/idt-case-data-for-location/{from}/{to}/{locationCCG}")
    public List<DispoGroupByTimePeriod> getDispoGroupForSpecificLocationCCGs(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable String locationCCG) {


//        public List<DispoGroupByTimePeriod> getDispoGroupForAllLocationCCGs(
//                @PathVariable String from,
//                @PathVariable String to,
//                @RequestHeader(caseDataApiUsernameHttpHeaderName) String apiUsername,
//                @RequestHeader(caseDataApiPasswordHttpHeaderName) String apiPassword) {

        //validateApiCredentials(apiUsername, apiPassword);

        logger.debug("Getting DispoGroupByTimePeriod objects between {} and {} for Location CCG", from, to, locationCCG);

        List<DispoGroupByTimePeriod> dispoGroupByTimePeriodList =
                idtCaseDataService.getForSpecificLocationCCG(from, to, locationCCG);

        logger.debug("Returning Dispo Group by Time Period List with value of {}", dispoGroupByTimePeriodList);

        return dispoGroupByTimePeriodList;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/idt-case-data/{from}/{to}")
    public List<DispoGroupByTimePeriod> getDispoGroupForAllLocationCCGs(
            @PathVariable String from,
            @PathVariable String to) {


//        public List<DispoGroupByTimePeriod> getDispoGroupForAllLocationCCGs(
//                @PathVariable String from,
//                @PathVariable String to,
//                @RequestHeader(caseDataApiUsernameHttpHeaderName) String apiUsername,
//                @RequestHeader(caseDataApiPasswordHttpHeaderName) String apiPassword) {

        //validateApiCredentials(apiUsername, apiPassword);

        logger.debug("Getting DispoGroupByTimePeriod objects between  {} and {} for ALL location CCGs", from, to);

        List<DispoGroupByTimePeriod> dispoGroupByTimePeriodList =
                idtCaseDataService.getForAllLocationCCGs(from, to);

        logger.debug("Returning Dispo Group by Time Period List with valie of {}", dispoGroupByTimePeriodList);

        return dispoGroupByTimePeriodList;
    }


    @PostMapping(value = "/idt-case-data")
    public void getCapacityInformation(
            @RequestHeader(caseDataApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(caseDataApiPasswordHttpHeaderName) String apiPassword,
            @RequestBody List<IdtCaseCallDetails> idtCaseCallDetailsList) {

        validateApiCredentials(apiUsername, apiPassword);

        logger.debug("Storing IDT Case Data List {}", idtCaseCallDetailsList);

        idtCaseDataService.createIdtCaseCallDetails(idtCaseCallDetailsList);

        logger.debug("Stored IDT Case Data List {}", idtCaseCallDetailsList);
    }

    private void validateApiCredentials(String apiUsername, String apiPassword) {

        if (!caseDataApiUsername.equals(apiUsername) || !caseDataApiPassword.equals(apiPassword)) {
            throw new AuthenticationException("Username and Password could not be authenticated");
        }

    }
}
