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

CREATE TABLE encounter_duration_avg (
  id INTEGER NOT NULL PRIMARY KEY DEFAULT 0,
  duration_ms INTEGER NOT NULL,
  encounter_count INTEGER NOT NULL
);

CREATE TABLE subject_encounter_duration_avg (
  subject_id TEXT NOT NULL PRIMARY KEY,
  duration_ms INTEGER NOT NULL,
  encounter_count INTEGER NOT NULL
);
