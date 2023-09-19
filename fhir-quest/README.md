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

2. **UI Server**: This stage runs a web server and uses the persistent store
   from stage one to render various graphs on user demand.

## Developer Quick Start

### Prerequisites

1. [A JDK](https://openjdk.org/)
2. [Clojure](https://clojure.org/guides/install_clojure)
3. [Leiningen](https://codeberg.org/leiningen/leiningen)

### Generating FHIR data

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
