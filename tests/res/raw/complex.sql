-- Create table for migration
CREATE TABLE Entity2
(
    Id INTEGER AUTO_INCREMENT PRIMARY KEY,
    Column TEXT NOT NULL,
    Column2 INTEGER NULL /* this column is new */
);

-- Migrate data
INSERT INTO Entity2
(
    Id,
    Column, /* --> ; <-- */
    Column2
)
SELECT  Id,
        Column,
        0 -- there's no such value in the old table
        FROM Entity;

-- Rename Entity2 to Entity
DROP TABLE Entity;
ALTER TABLE Entity2 RENAME TO Entity;

/* Add some --sample-- data */
INSERT INTO Entity2
(
    Id, --;'/*;*/--
    Col/*not sure if anyone would ever be insane enough to do this*/umn,
    Column2--,
)
VALUES
(
	9001 /* not -- really */, -- almost forgot that comma
	42,--23, /* I don't know who messed this up
	'string /* string */ -- string'--,
	-- 'test' whoops we don't have that many columns
)