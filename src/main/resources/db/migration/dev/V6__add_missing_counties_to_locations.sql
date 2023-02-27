-- for some reason these values are not provided by the LocationIQ API so we add them extra here ...

UPDATE location_tbl
SET county = 'Hlavní město Praha'
WHERE region = 'Praha';

UPDATE location_tbl
SET county = 'Středočeský kraj'
WHERE region = 'Střední Čechy';


