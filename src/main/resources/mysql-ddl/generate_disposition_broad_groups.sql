--
-- For the Case Data graph, we need to generate a list of all the disposition broad groups that occurred in time
-- frame for the graph. This stored procedure generates this information into a temporary table and is used in
-- the full query to generate the graph data.
--
DROP PROCEDURE IF EXISTS generate_disposition_broad_groups;
DELIMITER $$
CREATE PROCEDURE generate_disposition_broad_groups(IN fromDate DATETIME, IN toDate DATETIME)
  BEGIN
    -- Create tmp table
    DROP TEMPORARY TABLE IF EXISTS disposition_broad_groups;
    CREATE TEMPORARY TABLE disposition_broad_groups (
      dispositionBroadGroup VARCHAR(40)
    )
      ENGINE = MEMORY;

    INSERT INTO disposition_broad_groups (dispositionBroadGroup)
      SELECT DISTINCT dispositionBroadGroup
      FROM idt_case_data
      WHERE dispositionBroadGroup IS NOT NULL AND dispositionBroadGroup != '' AND
            callEndDateTime BETWEEN fromDate AND toDate
      ORDER BY dispositionBroadGroup;
  END $$
DELIMITER ;

-- CALL generate_disposition_broad_groups('2017-05-07 00:00:00', '2017-05-08 23:59:59');