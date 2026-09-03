CREATE TABLE IF NOT EXISTS obec (
    kod   BIGINT PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS cast_obce (
    kod      BIGINT PRIMARY KEY,
    nazev    VARCHAR(255) NOT NULL,
    obec_kod BIGINT NOT NULL,
    CONSTRAINT fk_cast_obce_obec FOREIGN KEY (obec_kod) REFERENCES obec (kod)
);

CREATE INDEX IF NOT EXISTS idx_cast_obce_obec_kod ON cast_obce (obec_kod);
