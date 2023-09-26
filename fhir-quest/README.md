# FHIR Quest

FHIR Quest is my onboarding project on performing simple analysis queries on
(generated) medical data conforming to [FHIR R4 data
specifications](http://hl7.org/fhir/R4/).

## Design

### Goals

- **Simple Analysis**: Support a set of predetermined queries on a given
  dataset.

- **User Interface**: Add a functional UI to represent aggregations using
  graphs.

### Non-goals

- **Deep Analysis**: Supporting a versatile engine to perform arbitrary queries
  on a given dataset.

- **Dataset Mutation**: Supporting mutating datasets without reloading the
  entire application.

- **Security**: Supporting user management and access control.

### Architecture

Reading large volumes of data and performing aggregations on it is a
time-consuming task. Doing it on the fly when a user requests the UI will make
the app unresponsive for this duration. Hence, it makes sense to split the
entire pipeline into codependent stages.

1. **Data Processor**: This stage reads the given dataset, performs a set of
   queries and stores their results in a persistent store.

2. **UI and Data Server**: This stage runs a web server and uses the persistent
   store from stage one to serve the aggregated data and an UI to render various
   graphs on user demand.

## Developer Quick Start

### Prerequisites

1. [A JDK](https://openjdk.org/)
2. [Clojure](https://clojure.org/guides/install_clojure)
3. [Leiningen](https://codeberg.org/leiningen/leiningen)

### Generating FHIR Data

Run the `gen-fhir-data` Lein task to run a [script](scripts/gen-fhir-data.sh)
that fetches the latest Synthea Jar and runs it for you with sensible options.

```console
lein gen-fhir-data
```

You can also specify/override [Synthea
args](https://github.com/synthetichealth/synthea/wiki/Basic-Setup-and-Running).

```console
lein gen-fhir-data -p 10000 -a 0-25
```

### Processing FHIR Data

Run the `ingest` app subcommand to process data and export its aggregates to
a SQLite database.

```console
lein run -- ingest
lein run -- -db ./fhir-quest.db ingest -d ./synthea/fhir
```

### Serving UI and Data

- Install NPM dependencies first.

  ```console
  npm install
  ```

- Then run the `serve` app subcommand to start the HTTP server for serving UI
  and data. The `web-ui` Lein profile adds a `prep-task` to compile the UI once
  and serve it using the app.

  ```console
  lein with-profile +web-ui run -- serve
  lein with-profile +web-ui run -- -db ./fhir-quest.db serve -p 8080
  ```

#### Live Reload for the UI

To enable live reload for the UI, run a Shadow CLJS watch alongwith `serve` app
subcommand without the `web-ui` profile.

```console
npx shadow-cljs watch app
lein run -- serve
```

#### REST API

- `GET /api/v1/query`

  Lists all available queries (aggregations).

  Sample Response:

  ```jsonc
  [
    {
      "id": "encounter-duration-avg",
      "description": "Encounter duration average"
    }
    // ...
  ]
  ```

- `GET /api/v1/query/<query-id>/chart`

  Retrieves chart data for a query with the given `query-id`.

  Sample Response:

  ```jsonc
  {
    "type": "histogram",
    "data": [
      { "label": "Very Short", "value": 440 },
      { "label": "Short", "value": 326 },
      { "label": "Medium", "value": 97 },
      { "label": "Long", "value": 96 },
      { "label": "Very Long", "value": 150 }
    ]
  }
  ```
