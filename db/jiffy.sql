#########################################################
# Script to create a jiffy database
#
# Michael Abernethy
# 2/1/2014
#########################################################

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

