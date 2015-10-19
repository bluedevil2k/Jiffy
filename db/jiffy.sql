#########################################################
# Script to create a jiffy database
#
# Michael Abernethy
# 2/1/2014
#########################################################

#########################################################
DROP TABLE IF EXISTS user_session;

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


#########################################################
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  id 							INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_name 					VARCHAR(50) NOT NULL,
  role	 						VARCHAR(10) NOT NULL,
  password 						VARCHAR(64) NOT NULL,
  email 						VARCHAR(100),
  force_pw_change				BOOLEAN,
  is_frozen						BOOLEAN,
  failed_attempts				INT,
  last_logon_ts					DATETIME,
  INDEX idx_logon 				(user_name,password),
  INDEX idx_user 				(user_name),
  INDEX idx_role				(role)
) TYPE=InnoDB;
