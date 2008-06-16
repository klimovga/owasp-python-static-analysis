import logging

database_settings = {
		"host":"127.0.0.1",
		"login":"saadmin",
		"password":"qwerty",
		"database":"servicea"
		}

general_settings = {
		"session_timeout":15,
		"max_news_per_page":5,
		"users_home":"./home",
		"application_home":"."
	}

#logging levels are DEBUG, INFO, WARNING, ERROR, CRITICAL
logging_settings = {
		"logging_level":logging.DEBUG,
		"logging_logfile":"./log/web.log"
	}

#initialize logging
logging.basicConfig(level=logging_settings["logging_level"],
                    format='%(asctime)s %(levelname)s %(message)s',
                    filename=logging_settings["logging_logfile"],
                    filemode='a')
