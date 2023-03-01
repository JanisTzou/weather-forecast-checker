-- DB size in megabytes
select pg_database_size('weather_app_db')/1024/1024 as db_size_mbs;
