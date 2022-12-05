CREATE TABLE IF NOT EXISTS `user`(
    `username` varchar(8) NOT NULL,
    `password` varchar(16) NOT NULL,
    PRIMARY KEY ( `username` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;