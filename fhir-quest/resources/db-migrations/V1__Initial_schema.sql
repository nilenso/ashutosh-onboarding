CREATE TABLE patient (
  id TEXT NOT NULL PRIMARY KEY,
  birth_date INTEGER NOT NULL,
  language TEXT NOT NULL,
  marital_status TEXT NOT NULL
);

CREATE TABLE encounter (
  id TEXT NOT NULL PRIMARY KEY,
  subject_id TEXT NOT NULL,
  duration_ms INTEGER NOT NULL
);
