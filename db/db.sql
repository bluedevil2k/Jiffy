#########################################################
# Script to create a jiffy database
#
# Michael Abernethy
# 2/1/2014
#########################################################

DROP DATABASE IF EXISTS jiffy;
CREATE DATABASE jiffy;
USE jiffy;


#########################################################
DROP TABLE IF EXISTS users;

CREATE TABLE user (
  id 							INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_name 					VARCHAR(50) NOT NULL,
  role	 						VARCHAR(10) NOT NULL,
  password 						VARCHAR(64) NOT NULL,
  first_name 					VARCHAR(50),
  last_name 					VARCHAR(50),
  email 						VARCHAR(100),
  company						VARCHAR(50),
  address1 						VARCHAR(50),  
  address2 						VARCHAR(50),
  city 							VARCHAR(50),
  state 						VARCHAR(10),  
  postal_code 					VARCHAR(20),  
  phone_primary 				VARCHAR(20),  
  phone_alt 					VARCHAR(20),  
  phone_cell 					VARCHAR(20),  
  phone_fax 					VARCHAR(20),
  force_pw_change				BOOLEAN,
  is_frozen						BOOLEAN,
  is_disabled           		BOOLEAN,
  failed_attempts				INT,
  last_logon_ts					DATETIME,
  custom1						VARCHAR(50),
  custom2						VARCHAR(50),
  custom3						VARCHAR(50),
  custom4						VARCHAR(50),
  custom5						VARCHAR(50),
  update_username				VARCHAR(20),
  update_ts						DATETIME NOT NULL,
  INDEX idx_logon 				(user_name,password),
  INDEX idx_user 				(user_name),
  INDEX idx_role				(role)
) TYPE=InnoDB;

#########################################################
DROP TABLE IF EXISTS user_sessions;

CREATE TABLE user_session (
  session_id					VARCHAR(50) NOT NULL,
  user_id						INT UNSIGNED NOT NULL,
  user_name 					VARCHAR(50) NOT NULL,
  role	 						VARCHAR(10) NOT NULL,
  ip_address 					VARCHAR(50),
  logon_time					DATETIME NOT NULL,
  last_user_activity			DATETIME NOT NULL,
  INDEX idx_sessions    		(session_id),
  INDEX idx_sessions2			(user_id)
) TYPE=InnoDB;

