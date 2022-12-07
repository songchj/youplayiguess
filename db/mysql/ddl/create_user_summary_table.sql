CREATE TABLE IF NOT EXISTS `user_summary`(
    `username` varchar(8) NOT NULL,
    `total_score` int NOT NULL,
    `total_game_amount` int NOT NULL,
    `perform_correct_amount` int NOT NULL,
    `guess_correct_amount` int NOT NULL,
    PRIMARY KEY ( `username` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;