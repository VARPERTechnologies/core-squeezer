create user if not exists user_it identified by 'pass_it';

CREATE DATABASE if not exists vpt_it_database
   CHARACTER SET utf8mb4
   COLLATE utf8mb4_unicode_520_ci;
   
grant select on vpt_it_database.* to user_it;
