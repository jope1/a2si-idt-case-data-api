CREATE TABLE idt_case_data (
  ID                    INT          NOT NULL AUTO_INCREMENT,
  callSite              VARCHAR(255) NOT NULL,
  locationCCG           VARCHAR(255) NOT NULL,
  callStartDateTime     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  callEndDateTime       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  thirdPartyCaller      BOOLEAN,
  gender                VARCHAR(10)  NOT NULL,
  ageGroup              VARCHAR(40)  NOT NULL,
  dispositionGroup      VARCHAR(60),
  dispositionBroadGroup VARCHAR(40),
  PRIMARY KEY (ID)
);