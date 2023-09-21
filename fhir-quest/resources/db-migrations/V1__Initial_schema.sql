CREATE TABLE patient_age_group (
  patient_id TEXT NOT NULL PRIMARY KEY,
  age_group TEXT NOT NULL
);

CREATE TABLE patient_language (
  patient_id TEXT NOT NULL PRIMARY KEY,
  language TEXT NOT NULL
);

CREATE TABLE patient_marital_status (
  patient_id TEXT NOT NULL PRIMARY KEY,
  status TEXT NOT NULL
);
