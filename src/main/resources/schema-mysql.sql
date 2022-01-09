--https://thecodinglog.github.io/spring/security/2018/05/25/spring-security-2.html
create table users(
	username varchar(50) not null primary key,
	password varchar(50) not null,
	enabled boolean not null
);
create table authorities (
	username varchar(50) not null,
	authority varchar(50) not null,
	constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index ix_auth_username on authorities (username,authority);

insert into users(username, password, enabled) values('user','password',1);
insert into authorities(username, authority) values('user','USER');
insert into authorities(username, authority) values('user','ADMIN');
insert into authorities(username, authority) values('user','DBA');