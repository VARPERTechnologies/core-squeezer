use store_manager_dev;

grant select, insert, update, delete on " + conn.getCatalog() + ".wp_options to " + testUser + "


select * 
from vpt_multiple_tasks_queue q
left join vpt_property_payload p
on q.id = p.task_id
order by q.id desc;


select * from vpt_ml_notifications order by id desc;

select distinct json_extract(content_request, '$.topic') from vpt_ml_notifications
order by id desc;


select * 
from vpt_notification_payload p
order by id desc;




select * 
from vpt_multiple_tasks_queue q
left join vpt_notification_payload p
on q.id = p.task_id
where task_name = 'MLToStoreSynchronizer'
-- and q.id > 120
-- and status != 'finished'
order by q.id desc;





select * 
from vpt_multiple_tasks_queue q
left join vpt_property_payload pp
on q.id = pp.task_id
where task_name = 'StoreToMLSynchronizer'
order by q.id desc;


select owner_id, count(*)
from vpt_multiple_tasks_queue
group by owner_id having count(*) > 1;

select * from vpt_multiple_tasks_queue order by id DESC;

select * from vpt_multiple_tasks_queue
where owner_id = 826;

select json_extract(message, '$.topic'), count(*)
from vpt_multiple_tasks_queue q
left join vpt_notification_payload p
on q.id = p.task_id
where task_name = 'MLToStoreSynchronizer'
group by json_extract(message, '$.topic');

-- update vpt_multiple_tasks_queue 
-- set status = 'new',
--     owner_id = null
-- where task_name = 'MLToStoreSynchronizer' 
-- -- and status = 'ignore'
-- and id BETWEEN 206 and 209;


select *
from vpt_multiple_tasks_queue q
left join vpt_notification_payload p
on q.id = p.task_id
where task_name = 'MLToStoreSynchronizer'
and json_extract(message, '$.topic') = 'items';






select post_id, meta_key, count(*)
from wp_postmeta
group by post_id, meta_key having count(*) > 1;

select * from wp_postmeta where post_id = 18565 and meta_key = '_wp_desired_post_slug';


insert into wp_options (option_name, option_value) values ('wc_url', 'https://varpertechnologies.com') on duplicate key update option_value = 'https://varpertechnologies.com';
insert into wp_options (option_name, option_value) values ('wc_secret_key', 'cs_502fddf9d562268d117e1b0eb2938e83dfd581a4') on duplicate key update option_value = 'cs_502fddf9d562268d117e1b0eb2938e83dfd581a4';
insert into wp_options (option_name, option_value) values ('wc_app_id', 'ck_f41bcacd1037c72ff296a38440e671c6beed8171') on duplicate key update option_value = 'ck_f41bcacd1037c72ff296a38440e671c6beed8171';


select * from wp_options 
where option_name like 'ml_%'
order by option_id desc



select count(*) from wp_posts where post_type = 'product'
select * from wp_posts where id in (select post_id from wp_postmeta where meta_key='ml_product_id' and meta_value = 'MLC565953919')

select * from wp_postmeta where post_id in (select post_id from wp_postmeta where meta_key='ml_product_id' and meta_value = 'MLC566213728')

select * 
from vpt_multiple_tasks_queue q
order by q.id desc;

select * from vpt_ml_notifications;


START transaction;
insert into vpt_multiple_tasks_queue (task_name, status) values ('StoreToMLSynchronizer', 'new');
insert into vpt_property_payload(task_id, post_id, post_property, value) values (410,	367,	'_stock',	'6309');
commit;


select * from wp_posts where post_type = 'product';

select * from wp_postmeta where post_id = 84;

show processlist;


SELECT
    PLUGIN_NAME as Name,
    PLUGIN_VERSION as Version,
    PLUGIN_STATUS as Status
FROM INFORMATION_SCHEMA.PLUGINS
WHERE PLUGIN_TYPE='STORAGE ENGINE';




CREATE TABLE vpt_notification_payload (
  id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  task_id BIGINT(20) NOT NULL,
  message LONGTEXT DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB,
AUTO_INCREMENT = 121,
AVG_ROW_LENGTH = 546,
CHARACTER SET utf8mb4,
COLLATE utf8mb4_unicode_520_ci;