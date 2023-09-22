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

CREATE TABLE query (
  id TEXT NOT NULL PRIMARY KEY,
  description TEXT NOT NULL,
  chart_type TEXT NOT NULL,
  data_json BLOB
);

INSERT INTO
  query (id, description, chart_type)
  VALUES
    ("encounter-duration-avg", "Encounter duration average", "scalar"),
    ("patient-encounter-duration-groups", "Distribution of patients by their average encounter durations", "histogram"),
    ("patient-age-group", "Distribution of patients by age groups", "histogram"),
    ("patient-language", "Distribution of patients by communication languages", "histogram"),
    ("patient-marital-status", "Distribution of patients by marital status", "histogram");
